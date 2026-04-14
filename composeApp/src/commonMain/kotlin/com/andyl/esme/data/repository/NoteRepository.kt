package com.andyl.esme.data.repository

import com.andyl.esme.data.local.dao.BlockDao
import com.andyl.esme.data.local.dao.NoteDao
import com.andyl.esme.data.local.entity.BlockEntity
import com.andyl.esme.data.local.entity.NoteEntity
import com.andyl.esme.data.local.model.NoteWithBlocks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class NoteRepository(
    private val noteDao: NoteDao,
    private val blockDao: BlockDao
) {
    // --- Notas ---
    fun getNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun getNoteById(id: String): NoteEntity? = noteDao.getNoteById(id)

    suspend fun saveNote(note: NoteEntity) {
        noteDao.upsertNote(note)
    }

    suspend fun deleteNote(note: NoteEntity) {
        noteDao.deleteNote(note)
    }

    fun getNotesWithBlocks(): Flow<List<NoteWithBlocks>> =
        combine(
            noteDao.getAllNotes(),
            blockDao.getAllBlocks()
        ) { notes, blocks ->
            println("FLOW EMIT: ${blocks.size}")
            val blocksByNoteId = blocks.groupBy { it.noteId }

            notes.map { note ->
                NoteWithBlocks(
                    note = note,
                    blocks = blocksByNoteId[note.id] ?: emptyList()
                )
            }
        }

    // --- Bloques  ---

    fun getBlocksForNote(noteId: String): Flow<List<BlockEntity>> {
        return blockDao.getBlocksForNote(noteId)
    }

    suspend fun saveBlocks(blocks: List<BlockEntity>) {
        if (blocks.isNotEmpty()) {
            blockDao.upsertBlocks(blocks)
        }
    }

    suspend fun deleteBlock(block: BlockEntity) {
        blockDao.deleteBlock(block)
    }
}