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
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.ui.text.style.TextOverflow
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
    onBack: () -> Unit,
    onNavigateToNote: (String) -> Unit
) {
    val viewModel = koinViewModel<EditorViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusRequesters = remember { mutableStateMapOf<String, FocusRequester>() }

    val syntaxTransformer = remember {
        EsmeSyntaxTransformer(
            onLinkClick = { linkTitle -> viewModel.handleIntent(EditorIntent.OpenLink(linkTitle)) },
            onHashtagClick = { tag -> viewModel.handleIntent(EditorIntent.SearchHashtag(tag)) },
            onMentionClick = { user -> viewModel.handleIntent(EditorIntent.SearchMention(user)) }
        )
    }

    LaunchedEffect(noteId) {
        viewModel.handleIntent(EditorIntent.LoadNote(noteId))
    }

    LaunchedEffect(state.focusedBlockId) {
        state.focusedBlockId?.let { id ->
            delay(200)
            focusRequesters[id]?.requestFocus()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                EditorEffect.NavigateBack -> onBack()
                is EditorEffect.ShowToast -> { /* Snackbar logic */ }
                is EditorEffect.NavigateToNote -> onNavigateToNote(effect.noteId)
            }
        }
    }

    // --- DIALOGO SELECTOR DE NOTAS ---
    if (state.showSearchSelector) {
        AlertDialog(
            onDismissRequest = { viewModel.handleIntent(EditorIntent.CloseSearchSelector) },
            containerColor = Color(0xFF16201A),
            title = {
                val title = when(state.searchType) {
                    SearchType.HASHTAG -> "Notas con este hashtag"
                    SearchType.MENTION -> "Notas que mencionan a este usuario"
                    else -> "Resultados de búsqueda"
                }
                Text(title, color = Color(0xFF50C878), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Box(modifier = Modifier.heightIn(max = 400.dp)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.searchResults) { note ->
                            Card(
                                onClick = {
                                    viewModel.handleIntent(EditorIntent.CloseSearchSelector)
                                    onNavigateToNote(note.id)
                                },
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.05f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Description, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = note.title.ifBlank { "Nota sin título" },
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.handleIntent(EditorIntent.CloseSearchSelector) }) {
                    Text("Cerrar", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFF0B120E),
        topBar = {
            TopAppBar(
                title = { Text("Editar Nota", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = Color.White)
                    }
                },
                actions = {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF50C878), strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                    }
                    if (state.id != null) {
                        IconButton(onClick = { viewModel.handleIntent(EditorIntent.DeleteNote) }) {
                            Icon(Icons.Default.Delete, "Borrar", tint = Color.Red.copy(0.7f))
                        }
                    }
                }
            )
        },
        bottomBar = {
            EsmeToolbar(onAction = { token ->
                val lastId = state.focusedBlockId ?: state.blocks.lastOrNull()?.id
                if (lastId != null) {
                    val currentContent = (state.blocks.find { it.id == lastId } as? EsmeBlock.Text)?.content ?: ""
                    viewModel.handleIntent(EditorIntent.UpdateContent(lastId, currentContent + token))
                }
            })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        val color = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                            Color.Red.copy(0.2f) else Color.Transparent
                        Box(
                            modifier = Modifier.fillMaxSize().background(color, RoundedCornerShape(8.dp)).padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red)
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().animateItem()
                    ) {
                        // Controles de orden
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(end = 4.dp).width(24.dp)
                        ) {
                            if (index > 0) {
                                IconButton(
                                    onClick = { viewModel.handleIntent(EditorIntent.MoveBlock(index, index - 1)) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(Icons.Default.KeyboardArrowUp, null, tint = Color(0xFF50C878).copy(0.4f))
                                }
                            }
                            Icon(Icons.Default.Air, null, tint = Color.White.copy(0.1f), modifier = Modifier.size(12.dp))
                            if (index < state.blocks.size - 1) {
                                IconButton(
                                    onClick = { viewModel.handleIntent(EditorIntent.MoveBlock(index, index + 1)) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF50C878).copy(0.4f))
                                }
                            }
                        }

                        // Bloques dinámicos
                        Box(modifier = Modifier.weight(1f)) {
                            when (block) {
                                is EsmeBlock.Text -> EsmeTextFieldBlock(
                                    modifier = Modifier.focusRequester(requester),
                                    content = block.content,
                                    onContentChange = { viewModel.handleIntent(EditorIntent.UpdateContent(block.id, it)) },
                                    onNextBlock = { viewModel.handleIntent(EditorIntent.AddBlock(block.id)) },
                                    onDeleteIfEmpty = { viewModel.handleIntent(EditorIntent.DeleteBlock(block.id)) },
                                    transformer = syntaxTransformer
                                )
                                is EsmeBlock.Todo -> EsmeTodoBlock(
                                    modifier = Modifier.focusRequester(requester),
                                    content = block.content,
                                    isChecked = block.isChecked,
                                    onContentChange = { viewModel.handleIntent(EditorIntent.UpdateBlock(block.copy(content = it))) },
                                    onCheckedChange = { viewModel.handleIntent(EditorIntent.UpdateBlock(block.copy(isChecked = it))) },
                                    onDelete = { viewModel.handleIntent(EditorIntent.DeleteBlock(block.id)) }
                                )
                                is EsmeBlock.Priority -> EsmePriorityBlock(
                                    modifier = Modifier.focusRequester(requester),
                                    content = block.content,
                                    onContentChange = { viewModel.handleIntent(EditorIntent.UpdateBlock(block.copy(content = it))) }
                                )
                                is EsmeBlock.Expense -> EsmeExpenseBlock(
                                    modifier = Modifier.focusRequester(requester),
                                    label = block.description,
                                    amount = block.amount,
                                    onLabelChange = { viewModel.handleIntent(EditorIntent.UpdateBlock(block.copy(description = it))) },
                                    onAmountChange = { viewModel.handleIntent(EditorIntent.UpdateBlock(block.copy(amount = it))) },
                                    onDelete = { viewModel.handleIntent(EditorIntent.DeleteBlock(block.id)) }
                                )
                                is EsmeBlock.Quote -> EsmeQuoteBlock(
                                    modifier = Modifier.focusRequester(requester),
                                    content = block.content,
                                    onContentChange = { viewModel.handleIntent(EditorIntent.UpdateBlock(block.copy(content = it))) }
                                )
                                is EsmeBlock.Divider -> HorizontalDivider(
                                    color = Color(0xFF50C878).copy(0.3f),
                                    modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(0.5f)
                                )
                                else -> {}
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(120.dp)) }
        }
    }
}