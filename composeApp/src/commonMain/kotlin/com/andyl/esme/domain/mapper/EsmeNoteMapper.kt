package com.andyl.esme.domain.mapper

import com.andyl.esme.data.local.entity.BlockEntity
import com.andyl.esme.data.local.entity.NoteEntity
import com.andyl.esme.data.local.model.NoteWithBlocks
import com.andyl.esme.domain.model.EsmeNote

fun NoteWithBlocks.toDomain(): EsmeNote {
    return EsmeNote(
        id = note.id,
        title = note.title,
        blocks = blocks
            .sortedBy { it.orderIndex }
            .map { EsmeBlockMapper.toDomain(it) }
    )
}

object EsmeNoteMapper {
    fun toDomain(
        note: NoteEntity,
        blocks: List<BlockEntity>
    ): EsmeNote {
        return EsmeNote(
            id = note.id,
            title = note.title,
            blocks = blocks
                .sortedBy { it.orderIndex }
                .map { EsmeBlockMapper.toDomain(it) }
        )
    }
}

