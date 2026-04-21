package com.andyl.esme.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andyl.esme.data.local.entity.BlockEntity
import com.andyl.esme.data.local.entity.NoteEntity
import com.andyl.esme.data.local.model.NoteWithBlocks
import com.andyl.esme.data.repository.NoteRepository
import com.andyl.esme.domain.model.EsmeBlock
import com.andyl.esme.domain.model.EsmeNote
import com.andyl.esme.domain.model.extractTags
import com.andyl.esme.domain.model.getSearchableText
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
            is HomeIntent.SearchByTag -> {
                _state.update {
                    it.copy(
                        isSearchVisible = true,
                        searchQuery = intent.tag
                    )
                }
            }
            is HomeIntent.OpenTag -> {
                viewModelScope.launch {
                    _effect.send(HomeEffect.NavigateToTag(intent.tag))
                }
            }
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
                val metaTags = notes
                    .flatMap { it.blocks }
                    .flatMap { it.extractTags() }
                    .groupingBy { it }
                    .eachCount()
                    .map {
                        if (it.key.startsWith("#"))
                            MetaTag.Hashtag(it.key, it.value)
                        else
                            MetaTag.Mention(it.key, it.value)
                    }
                    .sortedByDescending { it.count }

                _state.update {
                    it.copy(
                        notes = notes,
                        totalExpenses = total,
                        isLoading = false,
                        metaTags = metaTags
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
    ): List<EsmeNote> {

        if (query.isBlank()) return notes

        val q = query.lowercase()

        return notes.filter { note ->
            note.title.contains(q, true) ||
                    note.blocks.any { block ->
                        block.getSearchableText().contains(q, true) ||
                                block.extractTags().any { it.contains(q, true) }
                    }
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

    private fun extractMetaTags(notes: List<EsmeNote>): List<MetaTag> {
        val hashtagRegex = Regex("#[a-zA-Z0-9_-]+")
        val mentionRegex = Regex("@[a-zA-Z0-9_-]+")

        val contents = notes
            .flatMap { it.blocks }
            .mapNotNull { block ->
                when (block) {
                    is EsmeBlock.Text -> block.content
                    is EsmeBlock.Todo -> block.content
                    is EsmeBlock.Priority -> block.content
                    is EsmeBlock.Quote -> block.content
                    else -> null
                }
            }

        val hashtags = contents
            .flatMap { hashtagRegex.findAll(it).map { it.value.lowercase() } }
            .groupingBy { it }
            .eachCount()
            .map { MetaTag.Hashtag(it.key, it.value) }

        val mentions = contents
            .flatMap { mentionRegex.findAll(it).map { it.value.lowercase() } }
            .groupingBy { it }
            .eachCount()
            .map { MetaTag.Mention(it.key, it.value) }

        return (hashtags + mentions)
            .sortedByDescending { it.count }
    }
}