package com.andyl.esme.ui.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andyl.esme.data.local.entity.NoteEntity
import com.andyl.esme.data.repository.NoteRepository
import com.andyl.esme.domain.engine.EsmeBlockEngine
import com.andyl.esme.domain.helper.processSmartTokens
import com.andyl.esme.domain.model.EsmeBlock
import com.andyl.esme.ui.screens.editor.transformer.EsmeMultiBlockParser
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
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
    private var observeJob: Job? = null


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

            is EditorIntent.UpdateBlock -> {
                _state.update { currentState ->
                    val newBlocks = currentState.blocks.map {
                        if (it.id == intent.block.id) intent.block else it
                    }
                    currentState.copy(blocks = newBlocks)
                }
            }

            is EditorIntent.UpdateContent -> {
                val processedContent = processSmartTokens(intent.newContent)

                if (processedContent.contains("\n")) {
                    handlePaste(intent.blockId, processedContent)
                } else {
                    updateBlock(intent.blockId, processedContent)
                }
            }

            is EditorIntent.AddBlock -> {
                addNewBlock(intent.afterBlockId)
            }

            is EditorIntent.DeleteBlock -> {
                deleteBlock(intent.blockId)
            }

            is EditorIntent.SaveNote -> saveCurrentNote()
            is EditorIntent.DeleteNote -> removeNote()
            is EditorIntent.MoveBlock -> moveBlock(intent.fromIndex, intent.toIndex)
            is EditorIntent.OpenLink -> openLink(intent.title)
        }
    }

    private fun updateBlock(blockId: String, content: String) {
        _state.update { current ->
            val newBlocks = current.blocks.map { block ->
                if (block.id == blockId) {
                    EsmeBlockEngine.process(block, content)
                } else block
            }
            current.copy(blocks = reindex(newBlocks))
        }
    }
    private fun addNewBlock(afterBlockId: String) {
        val noteId = _state.value.id ?: return
        val newBlockId = Uuid.random().toString()

        _state.update { currentState ->
            val index = currentState.blocks.indexOfFirst { it.id == afterBlockId }
            val newList = currentState.blocks.toMutableList()

            val newBlock = EsmeBlock.Text(
                id = newBlockId,
                noteId = noteId,
                orderIndex = index + 1,
                content = ""
            )

            if (index != -1) newList.add(index + 1, newBlock)
            else newList.add(newBlock)

            currentState.copy(
                blocks = reindex(newList),
                focusedBlockId = newBlockId
            )
        }
    }
    private fun moveBlock(fromIndex: Int, toIndex: Int) {
        val blocks = _state.value.blocks
        if (fromIndex !in blocks.indices || toIndex !in blocks.indices || fromIndex == toIndex) return

        _state.update { currentState ->
            val newList = currentState.blocks.toMutableList()
            val item = newList.removeAt(fromIndex)
            newList.add(toIndex, item)

            currentState.copy(
                blocks = reindex(newList),
                focusedBlockId = item.id
            )
        }
    }
    private fun reindex(list: List<EsmeBlock>): List<EsmeBlock> {
        val noteId = _state.value.id ?: ""
        val mutableList = list.toMutableList()

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
            if (currentState.blocks.size <= 1) return@update currentState

            val index = currentState.blocks.indexOfFirst { it.id == blockId }
            val newList = currentState.blocks.filterNot { it.id == blockId }

            val nextFocusId = if (index > 0) newList[index - 1].id else newList.firstOrNull()?.id

            currentState.copy(
                blocks = reindex(newList),
                focusedBlockId = nextFocusId
            )
        }
    }

    private fun observeNote(noteId: String) {
        observeJob?.cancel()

        observeJob = viewModelScope.launch {
            repository.getNote(noteId).collect { note ->

                if (note == null) return@collect

                _state.update {
                    it.copy(
                        id = note.id,
                        title = note.title,
                        blocks = note.blocks.ifEmpty {
                            listOf(
                                EsmeBlock.Text(
                                    Uuid.random().toString(),
                                    note.id,
                                    0,
                                    ""
                                )
                            )
                        },
                        isSaving = false,
                        focusedBlockId = note.blocks.firstOrNull()?.id
                    )
                }
            }
        }
    }
    private fun loadNote(id: String?) {
        if (id == null) {
            _state.update {
                EditorState(id = Uuid.random().toString(), isSaving = false)
            }
            return
        }

        observeNote(id)
    }

    private fun saveCurrentNote() {
        val current = _state.value
        val noteId = current.id ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val note = NoteEntity(
                id = noteId,
                title = current.title,
                content = buildLegacyContent(current.blocks),
                updatedAt = Clock.System.now().toEpochMilliseconds()
            )

            repository.saveNote(note)
            repository.saveBlocks(current.blocks)

            _state.update {
                it.copy(
                    isSaving = false,
                    lastSaved = Clock.System.now().toEpochMilliseconds()
                )
            }
        }
    }

    private fun removeNote() {
        val currentId = _state.value.id ?: return
        viewModelScope.launch {
            repository.getNoteById(currentId)?.let { entity ->
                repository.deleteNoteById(entity.id)
                _effect.send(EditorEffect.NavigateBack)
            }
        }
    }

    private fun buildLegacyContent(blocks: List<EsmeBlock>): String {
        return blocks.joinToString("\n") { block ->
            when (block) {
                is EsmeBlock.Text -> block.content
                is EsmeBlock.Todo ->
                    if (block.isChecked) "- [x] ${block.content}"
                    else "- [ ] ${block.content}"

                is EsmeBlock.Expense ->
                    "$ ${block.amount} ${block.description}"

                is EsmeBlock.Priority ->
                    "!!! ${block.content}"

                is EsmeBlock.Quote ->
                    "> ${block.content}"

                is EsmeBlock.Divider ->
                    "---"

                else -> ""
            }
        }
    }

    private fun handlePaste(blockId: String, raw: String) {
        val noteId = _state.value.id ?: return

        _state.update { current ->

            val index = current.blocks.indexOfFirst { it.id == blockId }
            if (index == -1) return@update current

            val parsed = EsmeMultiBlockParser.parse(noteId, raw)

            val newList = current.blocks.toMutableList().apply {
                removeAt(index)
                addAll(index, parsed)
            }

            current.copy(
                blocks = reindex(newList),
                focusedBlockId = parsed.lastOrNull()?.id
            )
        }
    }

    private fun openLink(title: String) {
        viewModelScope.launch {
            val existing = repository.findNoteByTitle(title.trim())

            val noteId = if (existing != null) {
                existing.id
            } else {
                val newId = Uuid.random().toString()
                repository.saveNote(
                    NoteEntity(
                        id = newId,
                        title = title.trim(),
                        content = "",
                        updatedAt = Clock.System.now().toEpochMilliseconds()
                    )
                )
                newId
            }

            // 3. Mandar el efecto a la UI
            _effect.send(EditorEffect.NavigateToNote(noteId))
        }
    }
}