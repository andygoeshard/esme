package com.andyl.esme.data.local.database

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun getDatabaseBuilder(ctx: Any?): RoomDatabase.Builder<EsmeDatabase> {
    val userHome = System.getProperty("user.home")
    val dbFile = File(userHome, ".emerald/emerald.db")

    if (!dbFile.parentFile.exists()) {
        dbFile.parentFile.mkdirs()
    }

    return Room.databaseBuilder<EsmeDatabase>(
        name = dbFile.absolutePath
    )
}