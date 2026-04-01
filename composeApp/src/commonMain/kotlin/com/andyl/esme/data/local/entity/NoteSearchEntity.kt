package com.andyl.esme.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = NoteEntity::class)
@Entity(tableName = "notes_search")
data class NoteSearchEntity(
    val title: String,
    val content: String
)