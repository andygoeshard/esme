package com.andyl.esme.di

import com.andyl.esme.data.local.database.createRoomDatabase
import com.andyl.esme.data.local.database.getDatabaseBuilder
import org.koin.dsl.module

actual val platformModule = module {
    single {
        val builder = getDatabaseBuilder()
        createRoomDatabase(builder)
    }
}