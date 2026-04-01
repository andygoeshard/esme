package com.andyl.esme.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andyl.esme.ui.screens.home.components.HomeNoteItem
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToEditor: (String?) -> Unit
) {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color(0xFF0B120E),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onNavigateToEditor(null)
                },
                containerColor = Color(0xFF50C878),
                contentColor = Color.Black
            ) { Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar") }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF50C878)
                )
            } else if (state.notes.isEmpty()) {
                Text(
                    text = "No hay notas todavía...",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Mis Notas",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(state.notes, key = { it.id }) { note ->
                        HomeNoteItem(
                            note = note,
                            onClick = { onNavigateToEditor(note.id) },
                            onDelete = { viewModel.handleIntent(HomeIntent.DeleteNote(note)) }
                        )
                    }
                }
            }
        }
    }
}