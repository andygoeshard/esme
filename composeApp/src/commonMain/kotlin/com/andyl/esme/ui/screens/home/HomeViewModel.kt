package com.andyl.esme.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andyl.esme.data.local.entity.BlockEntity
import com.andyl.esme.data.local.entity.NoteEntity
import com.andyl.esme.data.local.model.NoteWithBlocks
import com.andyl.esme.data.repository.NoteRepository
import com.andyl.esme.domain.model.EsmeBlock
import com.andyl.esme.domain.model.EsmeNote
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

class HomeViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val _effect = Channel<HomeEffect>()
    val effect = _effect.receiveAsFlow()

    private var observeJob: Job? = null

    init {
        observeNotes()
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadNotes -> observeNotes()

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

    private fun observeNotes() {
        observeJob?.cancel()

        observeJob = combine(
            repository.getNotesWithBlocks(),
            _state.map { it.searchQuery }
                .distinctUntilChanged()
        ) { notes, query ->

            val filtered = filterNotes(notes, query)
            val totalExpenses = calculateExpenses(notes)

            filtered to totalExpenses
        }
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { (notes, total) ->
                _state.update {
                    it.copy(
                        notes = notes,
                        totalExpenses = total,
                        isLoading = false
                    )
                }
            }
            .catch {
                _state.update { it.copy(isLoading = false) }
                _effect.send(HomeEffect.ShowError("Error cargando notas"))
            }
            .launchIn(viewModelScope)
    }

    // -------------------------
    // 🔍 Lógica separada (clave)
    // -------------------------

    private fun filterNotes(
        notes: List<EsmeNote>,
        query: String
    ): List<EsmeNote>{
        if (query.isBlank()) return notes

        return notes.filter { note ->
            note.title.contains(query, true) ||
                    note.blocks.any { blockMatchesQuery(it, query) }
        }
    }

    private fun blockMatchesQuery(block: EsmeBlock, query: String): Boolean {
        return when (block) {
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

    private fun calculateExpenses(notes: List<EsmeNote>): Double {
        return notes
            .flatMap { it.blocks }
            .filterIsInstance<EsmeBlock.Expense>()
            .sumOf { it.amount }
    }

    // -------------------------
    // ⚙️ Acciones
    // -------------------------

    @OptIn(ExperimentalUuidApi::class)
    private fun createNote(title: String) {
        viewModelScope.launch {
            val id = Uuid.random().toString()

            repository.saveNote(
                NoteEntity(
                    id = id,
                    title = title,
                    content = "",
                    updatedAt = Clock.System.now().toEpochMilliseconds()
                )
            )

            _effect.send(HomeEffect.NavigateToEditor(id))
        }
    }

    private fun toggleTask(block: EsmeBlock, isChecked: Boolean) {
        if (block !is EsmeBlock.Todo) return

        viewModelScope.launch {
            repository.saveBlocks(listOf(block.copy(isChecked = isChecked)))
        }
    }

    private fun removeNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteNoteById(noteId)
        }
    }
}