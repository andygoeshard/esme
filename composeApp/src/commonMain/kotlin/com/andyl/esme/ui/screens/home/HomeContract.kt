package com.andyl.esme.ui.screens.home

import com.andyl.esme.data.local.entity.BlockEntity
import com.andyl.esme.data.local.entity.NoteEntity
import com.andyl.esme.data.local.model.NoteWithBlocks
import com.andyl.esme.domain.model.EsmeBlock
import com.andyl.esme.domain.model.EsmeNote

data class HomeState(
    val notes: List<EsmeNote> = emptyList(),
    val totalExpenses: Double = 0.0,
    val isLoading: Boolean = false,
    val isSearchVisible: Boolean = false,
    val searchQuery: String = "",
    val error: String? = null,
)

sealed interface HomeIntent {
    data object LoadNotes : HomeIntent
    data class AddTestNote(val title: String, val content: String) : HomeIntent
    data class DeleteNote(val noteId: String) : HomeIntent
    data class UpdateSearchQuery(val query: String) : HomeIntent
    data class ToggleSearch(val isVisible: Boolean) : HomeIntent
    data class ToggleTask(val block: EsmeBlock.Todo, val isChecked: Boolean): HomeIntent
}

sealed interface HomeEffect {
    data class ShowError(val message: String) : HomeEffect
    data class  NavigateToEditor(val id: String) : HomeEffect
}