package com.andyl.esme.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(ctx: Any?): RoomDatabase.Builder<EsmeDatabase> {
    val context = ctx as Context
    val dbFile = context.getDatabasePath("emerald.db")
    return Room.databaseBuilder<EsmeDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath
    )
}