package com.andyl.esme.ui.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andyl.esme.data.local.entity.NoteEntity
import com.andyl.esme.data.repository.NoteRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class EditorViewModel(private val repository: NoteRepository) : ViewModel() {
    private val _state = MutableStateFlow(EditorState())
    val state = _state.asStateFlow()

    private val _effect = Channel<EditorEffect>()
    val effect = _effect.receiveAsFlow()

    fun handleIntent(intent: EditorIntent) {
        when (intent) {
            is EditorIntent.LoadNote -> loadNote(intent.id)
            is EditorIntent.UpdateTitle -> _state.update { it.copy(title = intent.newTitle) }
            is EditorIntent.UpdateContent -> _state.update { it.copy(content = intent.newContent) }
            is EditorIntent.SaveNote -> saveCurrentNote()
            is EditorIntent.DeleteNote -> removeNote()

        }
    }

    private fun loadNote(id: String?) {
        if (id == null) return
        viewModelScope.launch {
            repository.getNoteById(id)?.let { entity ->
                _state.update { it.copy(id = entity.id, title = entity.title, content = entity.content) }
            }
        }
    }

    private fun removeNote() {
        val currentId = _state.value.id ?: return
        viewModelScope.launch {
            repository.getNoteById(currentId)?.let { entity ->
                repository.deleteNote(entity)
                _effect.send(EditorEffect.NavigateBack) // Volvemos a la Home
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun saveCurrentNote() {
        val current = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            repository.saveNote(
                NoteEntity(
                    id = current.id ?: Uuid.random().toString(),
                    title = current.title,
                    content = current.content,
                    updatedAt = Clock.System.now().toEpochMilliseconds()
                )
            )
            _state.update { it.copy(isSaving = false, lastSaved = Clock.System.now().toEpochMilliseconds()) }
        }
    }
}