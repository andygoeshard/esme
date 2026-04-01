package com.andyl.esme.di

import com.andyl.esme.data.local.database.EsmeDatabase
import com.andyl.esme.data.repository.NoteRepository
import com.andyl.esme.ui.screens.editor.EditorViewModel
import com.andyl.esme.ui.screens.home.HomeViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val commonModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    single { get<EsmeDatabase>().noteDao() }

    single { NoteRepository(get()) }

    factory { HomeViewModel(get()) }
    factory { EditorViewModel(get()) }
}

expect val platformModule: Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule, platformModule)
    }