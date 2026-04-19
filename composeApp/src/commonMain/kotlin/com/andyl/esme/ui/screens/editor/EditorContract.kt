package com.andyl.esme.ui.screens.editor

import com.andyl.esme.data.local.entity.NoteEntity
import com.andyl.esme.domain.model.EsmeBlock

data class EditorState(
    val id: String? = null,
    val title: String = "",
    val blocks: List<EsmeBlock> = emptyList(),
    val focusedBlockId: String? = null,
    val isSaving: Boolean = false,
    val lastSaved: Long? = null,
    val isReordering: Boolean = false,
    val searchResults: List<NoteEntity> = emptyList(),
    val showSearchSelector: Boolean = false,
    val searchType: SearchType = SearchType.NONE
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
    data class MoveBlock(val fromIndex: Int, val toIndex: Int) : EditorIntent
    data class OpenLink(val title: String) : EditorIntent
    data class SearchHashtag(val tag: String) : EditorIntent
    data class SearchMention(val user: String) : EditorIntent
    data object CloseSearchSelector : EditorIntent
}

sealed interface EditorEffect {
    data object NavigateBack : EditorEffect
    data class ShowToast(val message: String) : EditorEffect
    data class NavigateToNote(val noteId: String) : EditorEffect
}

enum class SearchType { NONE, NOTE, HASHTAG, MENTION }


