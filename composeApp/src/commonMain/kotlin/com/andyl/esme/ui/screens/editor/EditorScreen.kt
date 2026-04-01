package com.andyl.esme.ui.screens.editor

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    noteId: String? = null,
    onBack: () -> Unit
) {
    val viewModel = koinViewModel<EditorViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Cargamos la nota al iniciar si hay ID
    LaunchedEffect(noteId) {
        viewModel.handleIntent(EditorIntent.LoadNote(noteId))
    }

    // Escuchar efectos (como navegar atrás al borrar o guardar)
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                EditorEffect.NavigateBack -> onBack()
                is EditorEffect.ShowToast -> { /* Mostrar algo */ }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0B120E),
        topBar = {
            TopAppBar(
                title = { Text("Editar Nota", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                actions = {
                    if (state.id != null) {
                        IconButton(onClick = { viewModel.handleIntent(EditorIntent.DeleteNote) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color.Red.copy(0.7f))
                        }
                    }
                    IconButton(onClick = { viewModel.handleIntent(EditorIntent.SaveNote) }) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar", tint = Color(0xFF50C878))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Título de la nota
            TextField(
                value = state.title,
                onValueChange = { viewModel.handleIntent(EditorIntent.UpdateTitle(it)) },
                placeholder = { Text("Título...", fontSize = 24.sp, color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color(0xFF50C878),
                    focusedTextColor = Color.White
                ),
                textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )

            HorizontalDivider(color = Color(0xFF50C878).copy(alpha = 0.2f), thickness = 1.dp)

            TextField(
                value = state.content,
                onValueChange = { viewModel.handleIntent(EditorIntent.UpdateContent(it)) },
                placeholder = { Text("Empezá a escribir tu magia...", color = Color.Gray) },
                modifier = Modifier.fillMaxSize().weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color(0xFF50C878),
                    focusedTextColor = Color.White.copy(0.9f)
                ),
                textStyle = TextStyle(fontSize = 18.sp, lineHeight = 26.sp)
            )
        }
    }
}