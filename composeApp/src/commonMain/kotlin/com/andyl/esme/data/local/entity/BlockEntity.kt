package com.andyl.esme.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "blocks",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BlockEntity(
    @PrimaryKey val id: String,
    val noteId: String,
    val orderIndex: Int,
    val type: String,

    val content: String? = null,
    val isChecked: Boolean? = null,
    val amount: Double? = null,
    val priorityLevel: String? = null,
    val time: String? = null,
    val timerSeconds: Int? = null,
    val mediaUri: String? = null,
    val caption: String? = null,
    val mediaWidth: Int? = null,
    val mediaHeight: Int? = null,
)