package com.andyl.esme.di

import com.andyl.esme.data.local.database.createRoomDatabase
import com.andyl.esme.data.local.database.getDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single {
        // Inyectamos el builder pasándole el contexto de Android
        val builder = getDatabaseBuilder(androidContext())
        createRoomDatabase(builder)
    }
}