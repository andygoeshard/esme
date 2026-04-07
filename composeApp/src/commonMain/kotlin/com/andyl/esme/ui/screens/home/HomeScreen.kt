package com.andyl.esme.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andyl.esme.ui.screens.home.components.HomeNoteItem
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToEditor: (String?) -> Unit
) {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Manejo de Navegación y Errores
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToEditor -> onNavigateToEditor(effect.id)
                is HomeEffect.ShowError -> { /* Podés meter un Toast acá si querés */ }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0B120E),
        topBar = {
            TopAppBar(
                title = {
                    if (state.isSearchVisible) {
                        TextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.handleIntent(HomeIntent.UpdateSearchQuery(it)) },
                            placeholder = { Text("Buscar en el cerebro...", color = Color.Gray, fontSize = 16.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color(0xFF50C878),
                                focusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                    } else {
                        Column {
                            Text(
                                "Mis Notas",
                                color = Color.White,
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-1).sp
                                )
                            )
                            if (state.notes.isNotEmpty()) {
                                Text(
                                    "${state.notes.size} notas",
                                    color = Color(0xFF50C878).copy(0.6f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = {
                        viewModel.handleIntent(HomeIntent.ToggleSearch(!state.isSearchVisible))
                    }) {
                        Icon(
                            imageVector = if (state.isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.handleIntent(HomeIntent.AddTestNote("", "")) },
                containerColor = Color(0xFF50C878),
                contentColor = Color.Black,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nueva Nota", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF50C878)
                )
            } else if (state.notes.isEmpty()) {
                // Estado Vacío / No se encontraron resultados
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (state.searchQuery.isEmpty()) Icons.Default.Add else Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.Gray.copy(0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (state.searchQuery.isEmpty()) "Esme está vacía." else "No se encontró nada.",
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // Grilla de Notas
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(minSize = 170.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp
                ) {
                    items(state.notes, key = { it.id }) { note ->
                        HomeNoteItem(
                            modifier = Modifier.animateItem(), // ✨ Animación de reordenamiento/borrado
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