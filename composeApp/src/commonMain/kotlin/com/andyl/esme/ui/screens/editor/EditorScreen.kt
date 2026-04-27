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
import androidx.lifecycle.viewmodel.compose.viewModel
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

private val Bg = Color(0xFF0B120E)
private val Surface = Color(0xFF16201A)
private val Emerald = Color(0xFF50C878)

private fun blockGlyph(block: EsmeBlock): String =
    when (block) {
        is EsmeBlock.Todo -> "□"
        is EsmeBlock.Priority -> "!"
        is EsmeBlock.Quote -> "❝"
        is EsmeBlock.Expense -> "$"
        is EsmeBlock.Divider -> "—"
        else -> "·"
    }
@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun EditorScreen(
    noteId: String? = null,
    onBack: () -> Unit,
    onNavigateToNote: (String) -> Unit,
    onNavigateToTag: (String) -> Unit
) {

    val viewModel = koinViewModel<EditorViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val focusRequesters = remember {
        mutableStateMapOf<String, FocusRequester>()
    }

    var currentUiFocusedId by remember {
        mutableStateOf<String?>(null)
    }

    val syntaxTransformer = remember {
        EsmeSyntaxTransformer(
            onLinkClick = {
                viewModel.handleIntent(EditorIntent.OpenLink(it))
            },
            onHashtagClick = onNavigateToTag,
            onMentionClick = onNavigateToTag
        )
    }

    LaunchedEffect(noteId) {
        viewModel.handleIntent(
            EditorIntent.LoadNote(noteId)
        )
    }

    LaunchedEffect(state.focusedBlockId) {
        state.focusedBlockId?.let { target ->
            if (target != currentUiFocusedId) {
                focusRequesters[target]?.requestFocus()
                currentUiFocusedId = target
            }
        }
    }

    LaunchedEffect(state.blocks) {
        val validIds = state.blocks.map { it.id }.toSet()

        focusRequesters.keys
            .filter { it !in validIds }
            .forEach { focusRequesters.remove(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when(effect){
                EditorEffect.NavigateBack -> onBack()
                is EditorEffect.NavigateToNote ->
                    onNavigateToNote(effect.noteId)

                is EditorEffect.ShowToast -> Unit
            }
        }
    }

    if (state.showSearchSelector) {
        AlertDialog(
            onDismissRequest = {
                viewModel.handleIntent(
                    EditorIntent.CloseSearchSelector
                )
            },
            containerColor = Surface,
            title = {
                Text(
                    "Resultados relacionados",
                    color = Emerald,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ){
                    items(state.searchResults){ note ->

                        Card(
                            onClick = {
                                viewModel.handleIntent(
                                    EditorIntent.CloseSearchSelector
                                )
                                onNavigateToNote(note.id)
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(.04f)
                            )
                        ) {

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(
                                    "↗",
                                    color = Emerald,
                                    fontSize = 16.sp
                                )

                                Spacer(Modifier.width(12.dp))

                                Text(
                                    note.title.ifBlank {
                                        "Nota sin título"
                                    },
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.handleIntent(
                            EditorIntent.CloseSearchSelector
                        )
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }

    Scaffold(
        containerColor = Bg,

        topBar = {

            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Bg
                ),

                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            null,
                            tint = Color.White
                        )
                    }
                },

                title = {

                    Column {

                        Text(
                            text =
                                if(state.title.isBlank())
                                    "Nueva Nota"
                                else
                                    state.title.take(30),

                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                Modifier
                                    .size(8.dp)
                                    .background(
                                        if(state.isSaving)
                                            Color(0xFFFFC857)
                                        else Emerald,
                                        shape = RoundedCornerShape(50)
                                    )
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                if(state.isSaving)
                                    "Guardando..."
                                else
                                    "Sincronizado",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },

                actions = {
                    if(state.id != null){
                        IconButton(
                            onClick = {
                                viewModel.handleIntent(
                                    EditorIntent.DeleteNote
                                )
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = Color.Red.copy(.7f)
                            )
                        }
                    }
                }
            )
        },

    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),

            verticalArrangement = Arrangement.spacedBy(10.dp)
        ){

            item {

                TextField(
                    value = state.title,
                    onValueChange = {
                        viewModel.handleIntent(
                            EditorIntent.UpdateTitle(it)
                        )
                    },
                    placeholder = {
                        Text(
                            "Título...",
                            fontSize = 32.sp,
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        cursorColor = Emerald
                    )
                )

                Text(
                    "${state.blocks.size} bloques",
                    color = Color.Gray,
                    fontSize = 11.sp
                )

                Spacer(Modifier.height(16.dp))
            }

            itemsIndexed(
                state.blocks,
                key = { _,it -> it.id }
            ){ index, block ->

                val requester =
                    focusRequesters.getOrPut(block.id){
                        FocusRequester()
                    }

                val isFocused =
                    currentUiFocusedId == block.id

                val shouldFocus =
                    state.focusedBlockId == block.id

                val isLast =
                    index == state.blocks.lastIndex

                val dismissState =
                    rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if(it ==
                                SwipeToDismissBoxValue.EndToStart){
                                viewModel.handleIntent(
                                    EditorIntent.DeleteBlock(
                                        block.id
                                    )
                                )
                                true
                            } else false
                        }
                    )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,

                    backgroundContent = {

                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Color.Red.copy(.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(end=24.dp),

                            contentAlignment = Alignment.CenterEnd
                        ){
                            Text(
                                "Eliminar",
                                color = Color.Red
                            )
                        }
                    }

                ) {

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(
                                if(isFocused)
                                    Emerald.copy(.05f)
                                else Color.Transparent,

                                RoundedCornerShape(14.dp)
                            )
                            .padding(
                                vertical = 6.dp
                            ),
                        verticalAlignment = Alignment.Top
                    ){

                        Column(
                            modifier = Modifier
                                .width(30.dp)
                                .padding(top=10.dp),
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ){

                            Text(
                                blockGlyph(block),
                                color =
                                    if(isFocused)
                                        Emerald
                                    else
                                        Color.White.copy(.25f),
                                fontWeight =
                                    FontWeight.Bold
                            )
                        }

                        Box(
                            Modifier
                                .weight(1f)
                                .focusRequester(requester)
                        ) {

                            when(block){

                                is EsmeBlock.Text ->
                                    EsmeTextFieldBlock(
                                        content = block.content,
                                        blockId = block.id,

                                        onContentChange = {
                                            viewModel.handleIntent(
                                                EditorIntent.UpdateContent(
                                                    block.id,
                                                    it
                                                )
                                            )
                                        },

                                        onNextBlock = {
                                            viewModel.handleIntent(
                                                EditorIntent.OnEnter(
                                                    block.id,
                                                    it
                                                )
                                            )
                                        },

                                        onDeleteIfEmpty = {
                                            viewModel.handleIntent(
                                                EditorIntent.DeleteBlock(
                                                    block.id
                                                )
                                            )
                                        },

                                        transformer = syntaxTransformer,

                                        onFocusChanged = {
                                            if(it)
                                                currentUiFocusedId =
                                                    block.id
                                        },

                                        forceCursorToEnd =
                                            shouldFocus && isLast
                                    )

                                is EsmeBlock.Todo ->
                                    EsmeTodoBlock(
                                        content = block.content,
                                        blockId = block.id,
                                        isChecked = block.isChecked,
                                        onContentChange = {
                                            viewModel.handleIntent(
                                                EditorIntent.UpdateBlock(
                                                    block.copy(
                                                        content = it
                                                    )
                                                )
                                            )
                                        },
                                        onCheckedChange = {
                                            viewModel.handleIntent(
                                                EditorIntent.UpdateBlock(
                                                    block.copy(
                                                        isChecked = it
                                                    )
                                                )
                                            )
                                        },
                                        onDelete = {
                                            viewModel.handleIntent(
                                                EditorIntent.DeleteBlock(
                                                    block.id
                                                )
                                            )
                                        },
                                        visualTransformation =
                                            syntaxTransformer
                                    )

                                is EsmeBlock.Priority ->
                                    EsmePriorityBlock(
                                        blockId = block.id,
                                        content = block.content,
                                        onContentChange = {
                                            viewModel.handleIntent(
                                                EditorIntent.UpdateBlock(
                                                    block.copy(
                                                        content = it
                                                    )
                                                )
                                            )
                                        },
                                        onFocusChanged = {
                                            if(it)
                                                currentUiFocusedId =
                                                    block.id
                                        },
                                        forceCursorToEnd =
                                            shouldFocus && isLast,
                                        visualTransformation =
                                            syntaxTransformer
                                    )

                                is EsmeBlock.Expense ->
                                    EsmeExpenseBlock(
                                        label = block.description,
                                        amount = block.amount,
                                        blockId = block.id,
                                        onLabelChange = {
                                            viewModel.handleIntent(
                                                EditorIntent.UpdateBlock(
                                                    block.copy(
                                                        description = it
                                                    )
                                                )
                                            )
                                        },
                                        onAmountChange = {
                                            viewModel.handleIntent(
                                                EditorIntent.UpdateBlock(
                                                    block.copy(
                                                        amount = it
                                                    )
                                                )
                                            )
                                        },
                                        onDelete = {
                                            viewModel.handleIntent(
                                                EditorIntent.DeleteBlock(
                                                    block.id
                                                )
                                            )
                                        },
                                        onFocusChanged = {
                                            if(it)
                                                currentUiFocusedId =
                                                    block.id
                                        },
                                        forceCursorToEnd =
                                            shouldFocus && isLast,
                                        visualTransformation =
                                            syntaxTransformer
                                    )

                                is EsmeBlock.Quote ->
                                    EsmeQuoteBlock(
                                        content = block.content,
                                        blockId = block.id,
                                        onContentChange = {
                                            viewModel.handleIntent(
                                                EditorIntent.UpdateBlock(
                                                    block.copy(
                                                        content = it
                                                    )
                                                )
                                            )
                                        },
                                        onFocusChanged = {
                                            if(it)
                                                currentUiFocusedId =
                                                    block.id
                                        },
                                        forceCursorToEnd =
                                            shouldFocus && isLast,
                                        visualTransformation =
                                            syntaxTransformer
                                    )

                                is EsmeBlock.Divider ->
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                vertical = 18.dp
                                            ),
                                        contentAlignment =
                                            Alignment.Center
                                    ){
                                        Text(
                                            "──── ✦ ────",
                                            color =
                                                Emerald.copy(.35f)
                                        )
                                    }

                                else -> Unit
                            }
                        }
                    }
                }
            }

            item {
                Spacer(
                    Modifier.height(140.dp)
                )
            }
        }
    }
}