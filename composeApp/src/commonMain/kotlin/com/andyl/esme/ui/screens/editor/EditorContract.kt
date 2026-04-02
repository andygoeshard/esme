package com.andyl.esme.ui.screens.editor

import com.andyl.esme.domain.model.EsmeBlock

data class EditorState(
    val id: String? = null,
    val title: String = "",
    val blocks: List<EsmeBlock> = emptyList(),
    val isSaving: Boolean = false,
    val lastSaved: Long? = null
)

sealed interface EditorIntent {
    data class LoadNote(val id: String?) : EditorIntent
    data class UpdateTitle(val newTitle: String) : EditorIntent
    data class UpdateContent(val blockId: String, val newContent: String) : EditorIntent
    data object SaveNote : EditorIntent
    data object DeleteNote : EditorIntent

    data class UpdateBlock(val block: EsmeBlock) : EditorIntent
    data class AddBlock(val afterBlockId: String) : EditorIntent
    data class DeleteBlock(val blockId: String) : EditorIntent
}

sealed interface EditorEffect {
    data object NavigateBack : EditorEffect
    data class ShowToast(val message: String) : EditorEffect
}