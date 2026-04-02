package com.andyl.esme.ui.screens.editor

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andyl.esme.domain.model.EsmeBlock
import com.andyl.esme.ui.screens.editor.blocks.EsmeExpenseBlock
import com.andyl.esme.ui.screens.editor.blocks.EsmePriorityBlock
import com.andyl.esme.ui.screens.editor.blocks.EsmeQuoteBlock
import com.andyl.esme.ui.screens.editor.blocks.EsmeTextFieldBlock
import com.andyl.esme.ui.screens.editor.blocks.EsmeTodoBlock
import com.andyl.esme.ui.screens.editor.components.EsmeToolbar
import com.andyl.esme.ui.screens.editor.transformer.EsmeSyntaxTransformer
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun EditorScreen(
    noteId: String? = null,
    onBack: () -> Unit
) {
    val viewModel = koinViewModel<EditorViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val syntaxTransformer = remember { EsmeSyntaxTransformer() }

    LaunchedEffect(noteId) {
        viewModel.handleIntent(EditorIntent.LoadNote(noteId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                EditorEffect.NavigateBack -> onBack()
                is EditorEffect.ShowToast -> { /* Implementar Snackbar si querés */ }
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
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF50C878), strokeWidth = 2.dp)
                    } else {
                        Text("Guardado", style = TextStyle(fontSize = 12.sp, color = Color.White.copy(0.4f)), modifier = Modifier.padding(end = 8.dp))
                    }

                    if (state.id != null) {
                        IconButton(onClick = { viewModel.handleIntent(EditorIntent.DeleteNote) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color.Red.copy(0.7f))
                        }
                    }
                }
            )
        },
        bottomBar = {
            EsmeToolbar(onAction = { token ->
                val lastBlock = state.blocks.lastOrNull()
                val lastId = lastBlock?.id

                if (lastId != null) {
                    if (token == "- [ ] ") {
                        // Forzamos la mutación mandando el trigger al bloque actual
                        viewModel.handleIntent(EditorIntent.UpdateContent(lastId, token))
                    } else {
                        // Si es un token normal, lo concatenamos
                        val currentContent = (lastBlock as? EsmeBlock.Text)?.content ?: ""
                        viewModel.handleIntent(EditorIntent.UpdateContent(lastId, currentContent + token))
                    }
                }
            })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- BLOQUE TÍTULO ---
            item {
                TextField(
                    value = state.title,
                    onValueChange = { viewModel.handleIntent(EditorIntent.UpdateTitle(it)) },
                    placeholder = { Text("Título...", fontSize = 28.sp, color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF50C878),
                        focusedTextColor = Color.White
                    ),
                    textStyle = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)
                )
                HorizontalDivider(color = Color(0xFF50C878).copy(alpha = 0.2f), thickness = 1.dp)
                Spacer(Modifier.height(16.dp))
            }

            // --- RENDERIZADO DINÁMICO DE BLOQUES ---
            items(state.blocks, key = { it.id }) { block ->
                when (block) {
                    is EsmeBlock.Text -> {
                        EsmeTextFieldBlock(
                            content = block.content,
                            onContentChange = { viewModel.handleIntent(EditorIntent.UpdateContent(block.id, it)) },
                            onNextBlock = { viewModel.handleIntent(EditorIntent.AddBlock(block.id)) },
                            onDeleteIfEmpty = { viewModel.handleIntent(EditorIntent.DeleteBlock(block.id)) },
                            transformer = syntaxTransformer
                        )
                    }
                    is EsmeBlock.Todo -> {
                        EsmeTodoBlock(
                            content = block.content,
                            isChecked = block.isChecked,
                            onContentChange = { viewModel.handleIntent(EditorIntent.UpdateBlock(block.copy(content = it))) },
                            onCheckedChange = { viewModel.handleIntent(EditorIntent.UpdateBlock(block.copy(isChecked = it))) },
                            onDelete = { viewModel.handleIntent(EditorIntent.DeleteBlock(block.id)) }
                        )
                    }
                    is EsmeBlock.Divider -> {
                        HorizontalDivider(
                            color = Color(0xFF50C878).copy(0.4f),
                            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(0.6f)
                        )
                    }
                    is EsmeBlock.Priority -> {
                        EsmePriorityBlock(
                            content = block.content,
                            onContentChange = { viewModel.handleIntent(EditorIntent.UpdateBlock(block.copy(content = it))) }
                        )
                    }
                    is EsmeBlock.Quote -> {
                        EsmeQuoteBlock(
                            content = block.content,
                            onContentChange = { viewModel.handleIntent(EditorIntent.UpdateBlock(block.copy(content = it))) }
                        )
                    }
                    is EsmeBlock.Expense -> {
                        EsmeExpenseBlock(
                            label = block.description,
                            amount = block.amount,
                            onLabelChange = { viewModel.handleIntent(EditorIntent.UpdateBlock(block.copy(description = it))) },
                            onAmountChange = { viewModel.handleIntent(EditorIntent.UpdateBlock(block.copy(amount = it))) },
                            onDelete = { viewModel.handleIntent(EditorIntent.DeleteBlock(block.id)) }
                        )
                    }
                    else -> { /* Bloque genérico por ahora */ }
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}