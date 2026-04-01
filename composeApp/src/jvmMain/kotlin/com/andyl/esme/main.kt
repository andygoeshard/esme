package com.andyl.esme

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.andyl.esme.di.initKoin
import com.andyl.esme.ui.navigation.App

fun main() = application {

    initKoin()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Esme",
    ) {
        App()
    }
}