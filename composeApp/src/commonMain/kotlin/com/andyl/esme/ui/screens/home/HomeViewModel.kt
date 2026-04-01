package com.andyl.esme.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andyl.esme.data.local.entity.NoteEntity
import com.andyl.esme.data.repository.NoteRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = Channel<HomeEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        handleIntent(HomeIntent.LoadNotes)
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadNotes -> observeNotes()
            is HomeIntent.AddTestNote -> createNote(intent.title, intent.content)
            is HomeIntent.DeleteNote -> removeNote(intent.note)
        }
    }

    private fun observeNotes() {
        repository.getNotes()
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { list ->
                _state.update { it.copy(notes = list, isLoading = false) }
            }
            .catch { t ->
                _state.update { it.copy(error = t.message, isLoading = false) }
                _effect.send(HomeEffect.ShowError("Error al cargar notas"))
            }
            .launchIn(viewModelScope)
    }

    private fun createNote(title: String, content: String) {
        viewModelScope.launch {
            repository.saveNote(NoteEntity(title = title, content = content))
        }
    }

    private fun removeNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
}