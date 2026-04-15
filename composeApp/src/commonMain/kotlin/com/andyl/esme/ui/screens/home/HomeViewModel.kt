package com.andyl.esme.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andyl.esme.data.local.entity.BlockEntity
import com.andyl.esme.data.local.entity.NoteEntity
import com.andyl.esme.data.repository.NoteRepository
import com.andyl.esme.domain.model.EsmeBlock
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class HomeViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = Channel<HomeEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        observeNotesWithFilter()
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadNotes -> observeNotesWithFilter()

            is HomeIntent.AddTestNote -> createNote(intent.title)

            is HomeIntent.DeleteNote -> removeNote(intent.noteId)

            is HomeIntent.ToggleSearch -> {
                _state.update {
                    it.copy(
                        isSearchVisible = intent.isVisible,
                        searchQuery = if (!intent.isVisible) "" else it.searchQuery
                    )
                }
            }

            is HomeIntent.UpdateSearchQuery -> {
                _state.update { it.copy(searchQuery = intent.query) }
            }

            is HomeIntent.ToggleTask -> toggleTask(intent.block, intent.isChecked)
        }
    }
    private fun observeNotesWithFilter() {
        combine(
            repository.getNotesWithBlocks(),
            _state.map { it.searchQuery }.distinctUntilChanged()
        ) { allNotes, query ->

            val globalExpenses = allNotes
                .flatMap { it.blocks }
                .filterIsInstance<EsmeBlock.Expense>()
                .sumOf { it.amount }

            val filtered = if (query.isBlank()) {
                allNotes
            } else {
                allNotes.filter { note ->

                    val matchesTitle = note.title.contains(query, ignoreCase = true)

                    val matchesBlocks = note.blocks.any { block ->
                        when (block) {
                            is EsmeBlock.Text -> block.content.contains(query, true)
                            is EsmeBlock.Todo -> block.content.contains(query, true)
                            is EsmeBlock.Priority -> block.content.contains(query, true)
                            is EsmeBlock.Quote -> block.content.contains(query, true)
                            is EsmeBlock.Bullet -> block.content.contains(query, true)
                            is EsmeBlock.Code -> block.content.contains(query, true)
                            is EsmeBlock.Expense -> block.description.contains(query, true)
                            else -> false
                        }
                    }

                    matchesTitle || matchesBlocks
                }
            }

            filtered to globalExpenses
        }
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { (list, total) ->
                _state.update {
                    it.copy(
                        notes = list,
                        totalExpenses = total,
                        isLoading = false
                    )
                }
            }
            .catch { t ->
                _state.update { it.copy(error = t.message, isLoading = false) }
                _effect.send(HomeEffect.ShowError("Error al cargar notas"))
            }
            .launchIn(viewModelScope)
    }
    @OptIn(ExperimentalUuidApi::class)
    private fun createNote(title: String) {
        viewModelScope.launch {
            val newId = Uuid.random().toString()

            repository.saveNote(
                NoteEntity(
                    id = newId,
                    title = title,
                    content = "", // ⚠️ temporal (lo vamos a matar después)
                    updatedAt = Clock.System.now().toEpochMilliseconds()
                )
            )

            _effect.trySend(HomeEffect.NavigateToEditor(newId))
        }
    }
    private fun toggleTask(block: EsmeBlock, isChecked: Boolean) {
        if (block !is EsmeBlock.Todo) return

        viewModelScope.launch {
            val updated = block.copy(isChecked = isChecked)
            repository.saveBlocks(listOf(updated))
        }
    }
    private fun removeNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteNoteById(noteId)
        }
    }
}