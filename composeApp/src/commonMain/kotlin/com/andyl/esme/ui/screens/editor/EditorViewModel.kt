package com.andyl.esme.ui.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andyl.esme.data.local.entity.NoteEntity
import com.andyl.esme.data.repository.NoteRepository
import com.andyl.esme.domain.mapper.EsmeBlockMapper
import com.andyl.esme.domain.model.EsmeBlock
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, FlowPreview::class)
class EditorViewModel(private val repository: NoteRepository) : ViewModel() {
    private val _state = MutableStateFlow(EditorState())
    val state = _state.asStateFlow()

    private val _effect = Channel<EditorEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            state
                .drop(1)
                .debounce(1000L)
                .distinctUntilChanged { old, new ->
                    old.title == new.title && old.blocks == new.blocks
                }
                .collect {
                    saveCurrentNote()
                }
        }
    }

    fun handleIntent(intent: EditorIntent) {
        when (intent) {
            is EditorIntent.LoadNote -> loadNote(intent.id)
            is EditorIntent.UpdateTitle -> _state.update { it.copy(title = intent.newTitle) }

            // 📝 Actualización de un bloque ya existente
            is EditorIntent.UpdateBlock -> {
                _state.update { currentState ->
                    val newBlocks = currentState.blocks.map {
                        if (it.id == intent.block.id) intent.block else it
                    }
                    currentState.copy(blocks = newBlocks)
                }
            }

            is EditorIntent.UpdateContent -> {
                updateBlockAndCheckMutation(intent.blockId, intent.newContent)
            }

            // ➕ Añadir bloque nuevo (Ej: después de un Enter)
            is EditorIntent.AddBlock -> {
                addNewBlock(intent.afterBlockId)
            }

            // 🗑️ Borrar bloque
            is EditorIntent.DeleteBlock -> {
                deleteBlock(intent.blockId)
            }

            is EditorIntent.SaveNote -> saveCurrentNote()
            is EditorIntent.DeleteNote -> removeNote()
        }
    }

    private fun updateBlockAndCheckMutation(blockId: String, content: String) {
        val noteId = _state.value.id ?: return

        _state.update { currentState ->
            val updatedList = currentState.blocks.map { block ->
                if (block.id == blockId && block is EsmeBlock.Text) {
                    when {
                        content.startsWith("- [ ] ") ->
                            EsmeBlock.Todo(blockId, noteId, block.orderIndex, content.removePrefix("- [ ] "), false)

                        content.startsWith("!!! ") ->
                            EsmeBlock.Priority(blockId, noteId, block.orderIndex, content.removePrefix("!!! "))

                        content.startsWith("$ ") -> {
                            val remaining = content.removePrefix("$ ").trim()
                            val initialAmount = remaining.toDoubleOrNull() ?: 0.0
                            val initialLabel = if (initialAmount == 0.0) remaining else "Gasto"

                            EsmeBlock.Expense(blockId, noteId, block.orderIndex, initialLabel, initialAmount)
                        }

                        content.startsWith("---") ->
                            EsmeBlock.Divider(blockId, noteId, block.orderIndex)

                        content.startsWith("> ") ->
                            EsmeBlock.Quote(blockId, noteId, block.orderIndex, content.removePrefix("> "))

                        else -> block.copy(content = content)
                    }
                } else {
                    block
                }
            }
            currentState.copy(blocks = reindex(updatedList))
        }
    }

    private fun addNewBlock(afterBlockId: String) {
        val noteId = _state.value.id ?: return
        _state.update { currentState ->
            val index = currentState.blocks.indexOfFirst { it.id == afterBlockId }
            val newList = currentState.blocks.toMutableList()

            val newBlock = EsmeBlock.Text(
                id = Uuid.random().toString(),
                noteId = noteId,
                orderIndex = index + 1,
                content = ""
            )

            if (index != -1) newList.add(index + 1, newBlock)
            else newList.add(newBlock)

            currentState.copy(blocks = reindex(newList))
        }
    }
    private fun reindex(list: List<EsmeBlock>): List<EsmeBlock> {
        val noteId = _state.value.id ?: ""
        val mutableList = list.toMutableList()

        // 🔥 Si el último no es texto, agregamos uno vacío de seguridad
        if (mutableList.isEmpty() || mutableList.last() !is EsmeBlock.Text) {
            mutableList.add(
                EsmeBlock.Text(
                    id = Uuid.random().toString(),
                    noteId = noteId,
                    orderIndex = mutableList.size,
                    content = ""
                )
            )
        }

        return mutableList.mapIndexed { idx, block ->
            when (block) {
                is EsmeBlock.Text -> block.copy(orderIndex = idx)
                is EsmeBlock.Todo -> block.copy(orderIndex = idx)
                is EsmeBlock.Priority -> block.copy(orderIndex = idx)
                is EsmeBlock.Expense -> block.copy(orderIndex = idx)
                is EsmeBlock.Divider -> block.copy(orderIndex = idx)
                is EsmeBlock.Quote -> block.copy(orderIndex = idx)
                else -> block
            }
        }
    }

    private fun deleteBlock(blockId: String) {
        _state.update { currentState ->
            if (currentState.blocks.size <= 1) return@update currentState // No dejamos la nota vacía
            val newList = currentState.blocks.filterNot { it.id == blockId }
            currentState.copy(blocks = newList)
        }
    }

    private fun loadNote(id: String?) {
        if (id == null) {
            _state.update { EditorState(id = Uuid.random().toString(), isSaving = false) }
            return
        }

        viewModelScope.launch {
            val entity = repository.getNoteById(id)

            repository.getBlocksForNote(id).first().let { blockEntities ->
                val domainBlocks = blockEntities.map { EsmeBlockMapper.toDomain(it) }

                _state.update {
                    it.copy(
                        id = entity?.id ?: id,
                        title = entity?.title ?: "",
                        blocks = domainBlocks.ifEmpty {
                            listOf(EsmeBlock.Text(Uuid.random().toString(), id, 0, ""))
                        },
                        isSaving = false
                    )
                }
            }
        }
    }

    private fun saveCurrentNote() {
        val current = _state.value
        val noteId = current.id ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            // 1. Guardamos la Nota (el "padre")
            repository.saveNote(
                NoteEntity(
                    id = noteId,
                    title = current.title,
                    content = current.blocks.filterIsInstance<EsmeBlock.Text>()
                        .firstOrNull()?.content ?: "", // Usamos el primer texto como preview
                    updatedAt = Clock.System.now().toEpochMilliseconds()
                )
            )

            // 2. Guardamos los Bloques (los "hijos")
            val entities = current.blocks.map { EsmeBlockMapper.toEntity(it) }
            repository.saveBlocks(entities)

            _state.update {
                it.copy(isSaving = false, lastSaved = Clock.System.now().toEpochMilliseconds())
            }
        }
    }

    private fun removeNote() {
        val currentId = _state.value.id ?: return
        viewModelScope.launch {
            repository.getNoteById(currentId)?.let { entity ->
                repository.deleteNote(entity)
                _effect.send(EditorEffect.NavigateBack)
            }
        }
    }
}