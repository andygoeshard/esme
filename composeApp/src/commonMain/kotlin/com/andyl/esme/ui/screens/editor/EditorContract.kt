package com.andyl.esme.ui.screens.editor

data class EditorState(
    val id: String? = null,
    val title: String = "",
    val content: String = "",
    val isSaving: Boolean = false,
    val lastSaved: Long? = null
)

sealed interface EditorIntent {
    data class LoadNote(val id: String?) : EditorIntent
    data class UpdateTitle(val newTitle: String) : EditorIntent
    data class UpdateContent(val newContent: String) : EditorIntent
    data object SaveNote : EditorIntent
    data object DeleteNote : EditorIntent
}

sealed interface EditorEffect {
    data object NavigateBack : EditorEffect
    data class ShowToast(val message: String) : EditorEffect
}