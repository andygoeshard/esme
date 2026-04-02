package com.andyl.esme.ui.screens.home

import com.andyl.esme.data.local.entity.NoteEntity

data class HomeState(
    val notes: List<NoteEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface HomeIntent {
    data object LoadNotes : HomeIntent
    data class AddTestNote(val title: String, val content: String) : HomeIntent
    data class DeleteNote(val note: NoteEntity) : HomeIntent
}

sealed interface HomeEffect {
    data class ShowError(val message: String) : HomeEffect
    data class  NavigateToEditor(val id: String) : HomeEffect
}