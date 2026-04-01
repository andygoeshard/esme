package com.andyl.esme.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.andyl.esme.data.local.dao.NoteDao
import com.andyl.esme.data.local.entity.NoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [NoteEntity::class], version = 1)
abstract class EsmeDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}

// Esta función es la que llama Koin para armar la DB
fun createRoomDatabase(builder: RoomDatabase.Builder<EsmeDatabase>): EsmeDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

// El "contrato" para cada plataforma
expect fun getDatabaseBuilder(ctx: Any? = null): RoomDatabase.Builder<EsmeDatabase>