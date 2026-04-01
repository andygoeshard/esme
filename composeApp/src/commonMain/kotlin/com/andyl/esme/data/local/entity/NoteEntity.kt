package com.andyl.esme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String = Uuid.random().toString(),
    val title: String,
    val content: String,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),
    val isFavorite: Boolean = false,
    val tags: String = "" // Después lo hacemos más pro con TypeConverters
)