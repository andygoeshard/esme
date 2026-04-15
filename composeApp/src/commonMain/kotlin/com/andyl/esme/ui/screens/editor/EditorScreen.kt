package com.andyl.esme.ui.screens.editor

import androidx.compose.foundation.background
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import kotlinx.coroutines.delay
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
    val focusRequesters = remember { mutableStateMapOf<String, FocusRequester>() }

    LaunchedEffect(noteId) {
        viewModel.handleIntent(EditorIntent.LoadNote(noteId))
    }

    LaunchedEffect(state.focusedBlockId) {
        val id = state.focusedBlockId
        if (id != null) {
            println("🔥 Intentando dar foco a: $id")
            delay(200)
            focusRequesters[id]?.let { requester ->
                println("✅ Requester encontrado para $id, pidiendo foco...")
                requester.requestFocus()
            } ?: println("❌ No se encontró requester para $id")
        }
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

            itemsIndexed(state.blocks, key = { _, block -> block.id }) { index, block ->
                val requester = focusRequesters.getOrPut(block.id) { FocusRequester() }

                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            viewModel.handleIntent(EditorIntent.DeleteBlock(block.id))
                            true
                        } else false
                    },
                    positionalThreshold = { it * 0.4f }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        val color = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                            Color.Red.copy(0.3f) else Color.Transparent
                        Box(
                            modifier = Modifier.fillMaxSize().background(color, RoundedCornerShape(8.dp)).padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().animateItem() // ✨ Movimiento suave
                    ) {
                        // --- COLUMNA DE REORDENAMIENTO ---
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(end = 4.dp).width(24.dp)
                        ) {
                            if (index > 0) {
                                IconButton(
                                    onClick = { viewModel.handleIntent(EditorIntent.MoveBlock(index, index - 1)) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Subir",
                                        tint = Color(0xFF50C878).copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.Air,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(16.dp)
                            )

                            // Flecha Abajo (Solo si no es el último)
                            if (index < state.blocks.size - 1) {
                                IconButton(
                                    onClick = { viewModel.handleIntent(EditorIntent.MoveBlock(index, index + 1)) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Bajar",
                                        tint = Color(0xFF50C878).copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        // --- CONTENIDO DEL BLOQUE ---
                        Box(modifier = Modifier.weight(1f)) {
                            when (block) {
                                is EsmeBlock.Text -> {
                                    EsmeTextFieldBlock(
                                        modifier = Modifier.focusRequester(requester),
                                        content = block.content,
                                        onContentChange = { viewModel.handleIntent(EditorIntent.UpdateContent(block.id, it)) },
                                        onNextBlock = { viewModel.handleIntent(EditorIntent.AddBlock(block.id)) },
                                        onDeleteIfEmpty = { viewModel.handleIntent(EditorIntent.DeleteBlock(block.id)) },
                                        transformer = syntaxTransformer
                                    )
                                }
                                is EsmeBlock.Todo -> {
                                    EsmeTodoBlock(
                                        modifier = Modifier.focusRequester(requester),
                                        content = block.content,
                                        isChecked = block.isChecked,
                                        onContentChange = { viewModel.handleIntent(EditorIntent.UpdateBlock(block.copy(content = it))) },
                                        onCheckedChange = { viewModel.handleIntent(EditorIntent.UpdateBlock(block.copy(isChecked = it))) },
                                        onDelete = { viewModel.handleIntent(EditorIntent.DeleteBlock(block.id)) }
                                    )
                                }
                                is EsmeBlock.Priority -> {
                                    EsmePriorityBlock(
                                        modifier = Modifier.focusRequester(requester),
                                        content = block.content,
                                        onContentChange = { viewModel.handleIntent(EditorIntent.UpdateBlock(block.copy(content = it))) }
                                    )
                                }
                                is EsmeBlock.Expense -> {
                                    EsmeExpenseBlock(
                                        modifier = Modifier.focusRequester(requester),
                                        label = block.description,
                                        amount = block.amount,
                                        onLabelChange = { viewModel.handleIntent(EditorIntent.UpdateBlock(block.copy(description = it))) },
                                        onAmountChange = { viewModel.handleIntent(EditorIntent.UpdateBlock(block.copy(amount = it))) },
                                        onDelete = { viewModel.handleIntent(EditorIntent.DeleteBlock(block.id)) }
                                    )
                                }
                                is EsmeBlock.Quote -> {
                                    EsmeQuoteBlock(
                                        modifier = Modifier.focusRequester(requester),
                                        content = block.content,
                                        onContentChange = { viewModel.handleIntent(EditorIntent.UpdateBlock(block.copy(content = it))) }
                                    )
                                }
                                is EsmeBlock.Divider -> {
                                    HorizontalDivider(
                                        color = Color(0xFF50C878).copy(0.4f),
                                        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(0.6f)
                                    )
                                }

                                else -> {}
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}