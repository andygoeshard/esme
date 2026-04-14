package com.andyl.esme.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andyl.esme.data.local.entity.BlockEntity
import com.andyl.esme.data.local.entity.NoteEntity
import com.andyl.esme.data.repository.NoteRepository
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
        viewModelScope.launch {
        observeNotesWithFilter()
        }
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadNotes -> viewModelScope.launch { observeNotesWithFilter() }
            is HomeIntent.AddTestNote -> createNote(intent.title, intent.content)
            is HomeIntent.DeleteNote -> removeNote(intent.note)
            is HomeIntent.ToggleSearch -> {
                _state.update { it.copy(
                    isSearchVisible = intent.isVisible,
                    searchQuery = if (!intent.isVisible) "" else it.searchQuery
                ) }
            }
            is HomeIntent.UpdateSearchQuery -> {
                _state.update { it.copy(searchQuery = intent.query) }
            }
            is HomeIntent.ToggleTask -> toggleTask(intent.block, intent.isChecked)
        }
    }

    private suspend fun observeNotesWithFilter() {
        combine(
            repository.getNotesWithBlocks(),
            _state.map { it.searchQuery }.distinctUntilChanged()
        ) { allNotesWithBlocks, query ->

            val globalExpenses = allNotesWithBlocks.flatMap { it.blocks }
                .filter { it.type == "EXPENSE" }
                .sumOf { it.amount ?: 0.0 }

            val filtered = if (query.isBlank()) {
                allNotesWithBlocks
            } else {
                allNotesWithBlocks.filter { item ->
                    item.note.title.contains(query, ignoreCase = true) ||
                            item.note.content.contains(query, ignoreCase = true)
                }
            }

            filtered to globalExpenses
        }
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { (list, total) ->
                _state.update { it.copy(
                    notes = list,
                    totalExpenses = total,
                    isLoading = false
                ) }
            }
            .catch { t ->
                _state.update { it.copy(error = t.message, isLoading = false) }
                _effect.send(HomeEffect.ShowError("Error al cargar notas"))
            }
            .launchIn(viewModelScope)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun createNote(title: String, content: String) {
        viewModelScope.launch {
            val newId = Uuid.random().toString()

            val newNote = NoteEntity(
                id = newId,
                title = title,
                content = content,
                updatedAt = Clock.System.now().toEpochMilliseconds()
            )

            repository.saveNote(newNote)
            _effect.trySend(HomeEffect.NavigateToEditor(newId))
        }
    }

    private fun toggleTask(block: BlockEntity, isChecked: Boolean) {
        viewModelScope.launch {
            val updatedBlock = block.copy(isChecked = isChecked)
            repository.saveBlocks(listOf(updatedBlock))
        }
        println("TOGGLE: ${block.id} -> $isChecked")
    }

    private fun removeNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
}