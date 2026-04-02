package com.andyl.esme.data.repository

import com.andyl.esme.data.local.dao.BlockDao
import com.andyl.esme.data.local.dao.NoteDao
import com.andyl.esme.data.local.entity.BlockEntity
import com.andyl.esme.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

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

    // --- Bloques (La magia de la Opción B) ---

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