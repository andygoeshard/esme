package com.andyl.esme.ui.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.andyl.esme.ui.screens.editor.EditorScreen
import com.andyl.esme.ui.screens.home.HomeScreen

@Composable
fun App() {
    val backStack = remember { mutableStateListOf<Route>(HomeRoute) }

    MaterialTheme {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = { route ->
                when (route) {
                    is HomeRoute -> NavEntry<Route>(route) {
                        HomeScreen(
                            onNavigateToEditor = { id -> backStack.add(EditorRoute(id)) }
                        )
                    }
                    is EditorRoute -> NavEntry<Route>(route) {
                        EditorScreen(
                            noteId = route.noteId,
                            onBack = { backStack.removeLastOrNull() },
                            onNavigateToNote = { id -> backStack.add(EditorRoute(id)) }
                        )
                    }
                }
            }
        )
    }
}