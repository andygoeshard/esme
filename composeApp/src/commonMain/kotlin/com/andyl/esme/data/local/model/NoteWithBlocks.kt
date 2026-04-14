package com.andyl.esme.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.andyl.esme.data.local.entity.BlockEntity
import com.andyl.esme.data.local.entity.NoteEntity

data class NoteWithBlocks(
    @Embedded
    val note: NoteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val blocks: List<BlockEntity>
)