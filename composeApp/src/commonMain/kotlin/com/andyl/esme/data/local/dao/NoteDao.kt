package com.andyl.esme.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.andyl.esme.data.local.entity.NoteEntity
import com.andyl.esme.data.local.model.NoteWithBlocks
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteFlowById(id: String): Flow<NoteEntity?>
    @Transaction
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getNotesWithBlocks(): Flow<List<NoteWithBlocks>>

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    @Query("SELECT * FROM notes WHERE title = :title LIMIT 1")
    suspend fun findNoteByTitle(title: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>
}

