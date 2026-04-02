package com.andyl.esme.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.andyl.esme.data.local.entity.BlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBlocks(blocks: List<BlockEntity>)

    @Query("SELECT * FROM blocks WHERE noteId = :noteId ORDER BY orderIndex ASC")
    fun getBlocksForNote(noteId: String): Flow<List<BlockEntity>>

    @Query("DELETE FROM blocks WHERE noteId = :noteId")
    suspend fun deleteBlocksForNote(noteId: String)

    @Delete
    suspend fun deleteBlock(block: BlockEntity)
}