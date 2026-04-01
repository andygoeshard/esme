package com.andyl.esme.data.repository

import com.andyl.esme.data.local.dao.NoteDao
import com.andyl.esme.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: NoteDao
) {
    fun getNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun saveNote(note: NoteEntity) {
        noteDao.upsertNote(note)
    }

    suspend fun getNoteById(id: String): NoteEntity? = noteDao.getNoteById(id)


    suspend fun deleteNote(note: NoteEntity) {
        noteDao.deleteNote(note)
    }
}