package com.andyl.esme.data.repository

import com.andyl.esme.data.local.dao.BlockDao
import com.andyl.esme.data.local.dao.NoteDao
import com.andyl.esme.data.local.entity.BlockEntity
import com.andyl.esme.data.local.entity.NoteEntity
import com.andyl.esme.data.local.model.NoteWithBlocks
import com.andyl.esme.domain.mapper.EsmeBlockMapper
import com.andyl.esme.domain.mapper.EsmeNoteMapper
import com.andyl.esme.domain.model.EsmeBlock
import com.andyl.esme.domain.model.EsmeNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class NoteRepository(
    private val noteDao: NoteDao,
    private val blockDao: BlockDao
) {

    // --- Notas (Domain) ---
    fun getNotesWithBlocks(): Flow<List<EsmeNote>> =
        combine(
            noteDao.getAllNotes(),
            blockDao.getAllBlocks()
        ) { notes, blocks ->

            val blocksByNoteId = blocks.groupBy { it.noteId }

            notes.map { note ->
                EsmeNoteMapper.toDomain(
                    note = note,
                    blocks = blocksByNoteId[note.id] ?: emptyList()
                )
            }
        }

    suspend fun getNoteById(id: String): EsmeNote? {
        val note = noteDao.getNoteById(id) ?: return null
        val blocks = blockDao.getBlocksForNote(id)
        return EsmeNoteMapper.toDomain(note, blocks.first())
    }

    fun getNote(id: String): Flow<EsmeNote?> =
        combine(
            noteDao.getNoteFlowById(id),
            blockDao.getBlocksForNote(id)
        ) { note, blocks ->

            note?.let { it ->
                EsmeNote(
                    id = it.id,
                    title = it.title,
                    blocks = blocks
                        .sortedBy { it.orderIndex }
                        .map { EsmeBlockMapper.toDomain(it) }
                )
            }
        }

    suspend fun saveNote(note: NoteEntity) {
        noteDao.upsertNote(note)
    }

    suspend fun deleteNoteById(id: String) {
        noteDao.deleteNoteById(id)
    }

    suspend fun findNoteByTitle(title: String): NoteEntity?{
        return noteDao.findNoteByTitle(title)
    }

    // --- Bloques (Domain ONLY) ---

    suspend fun saveBlocks(blocks: List<EsmeBlock>) {
        if (blocks.isNotEmpty()) {
            blockDao.upsertBlocks(
                blocks.map { EsmeBlockMapper.toEntity(it) }
            )
        }
    }
}