package com.andyl.esme.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andyl.esme.domain.model.EsmeBlock
import com.andyl.esme.domain.model.EsmeNote
import com.andyl.esme.ui.screens.home.components.HomeNoteItem
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToEditor: (String?) -> Unit,
    onNavigateToTag: (String) -> Unit
) {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToEditor -> onNavigateToEditor(effect.id)
                is HomeEffect.NavigateToTag -> onNavigateToTag(effect.tag)
                else -> Unit
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0B120E),
        topBar = {
            HomeTopBar(
                state = state,
                onSearchChange = {
                    viewModel.handleIntent(HomeIntent.UpdateSearchQuery(it))
                },
                onToggleSearch = {
                    viewModel.handleIntent(HomeIntent.ToggleSearch(!state.isSearchVisible))
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.handleIntent(HomeIntent.AddTestNote("", "")) },
                containerColor = Color(0xFF50C878),
                contentColor = Color.Black,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nueva Nota") }
            )
        }
    ) { padding ->
        HomeContent(
            modifier = Modifier.padding(padding),
            state = state,
            onOpenNote = onNavigateToEditor,
            onDelete = {
                viewModel.handleIntent(HomeIntent.DeleteNote(it))
            },
            onToggleTask = { block, checked ->
                viewModel.handleIntent(HomeIntent.ToggleTask(block, checked))
            },
            onTagClick = { tag ->
                viewModel.handleIntent(HomeIntent.OpenTag(tag))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    state: HomeState,
    onSearchChange: (String) -> Unit,
    onToggleSearch: () -> Unit
) {
    TopAppBar(
        title = {
            if (state.isSearchVisible) {
                TextField(
                    value = state.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Buscar...") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color(0xFF50C878),
                        focusedTextColor = Color.White
                    )
                )
            } else {
                Column {
                    Text("Las verdaderas", color = Color.White, fontWeight = FontWeight.Black)
                    Text("${state.notes.size} notas", color = Color.Gray, fontSize = 12.sp)
                }
            }
        },
        actions = {
            IconButton(onClick = onToggleSearch) {
                Icon(
                    imageVector = if (state.isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
fun HomeContent(
    modifier: Modifier,
    state: HomeState,
    onOpenNote: (String?) -> Unit,
    onDelete: (String) -> Unit,
    onToggleTask: (EsmeBlock.Todo, Boolean) -> Unit,
    onTagClick: (String) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {

        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF50C878)
                )
            }

            state.notes.isEmpty() -> {
                EmptyState(state.searchQuery)
            }

            else -> {
                NotesGrid(
                    notes = state.notes,
                    showDashboard = !state.isSearchVisible,
                    totalExpenses = state.totalExpenses,
                    onOpenNote = onOpenNote,
                    onDelete = onDelete,
                    onToggleTask = onToggleTask,
                    metaTags = state.metaTags,
                    onTagClick = { tag ->
                        onTagClick(tag)
                    }
                )
            }
        }
    }
}

@Composable
fun NotesGrid(
    notes: List<EsmeNote>,
    showDashboard: Boolean,
    metaTags: List<MetaTag>,
    totalExpenses: Double,
    onOpenNote: (String?) -> Unit,
    onDelete: (String) -> Unit,
    onToggleTask: (EsmeBlock.Todo, Boolean) -> Unit,
    onTagClick: (String) -> Unit
) {
    val pendingTasks = remember(notes) {
        notes.flatMap { it.blocks }
            .filterIsInstance<EsmeBlock.Todo>()
            .filter { !it.isChecked }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(170.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp
    ) {

        items(
            notes,
            key = { it.id }
        ) { note ->
            HomeNoteItem(
                modifier = Modifier.animateItem(),
                item = note,
                onClick = { onOpenNote(note.id) },
                onDelete = { onDelete(note.id) }
            )
        }

        if (showDashboard && pendingTasks.isNotEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) {
                TaskDashboard(pendingTasks, onToggleTask)
            }
        }

        if (showDashboard && totalExpenses > 0.0) {
            item(span = StaggeredGridItemSpan.FullLine) {
                DashboardCard(totalExpenses)
            }
        }

        if (showDashboard && metaTags.isNotEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) {
                MetaTagDashboard(
                    metaTags = metaTags,
                    onClick = onTagClick
                )
            }
        }
    }
}

@Composable
fun EmptyState(query: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (query.isEmpty()) Icons.Default.Add else Icons.Default.Search,
            contentDescription = null,
            tint = Color.Gray.copy(0.3f),
            modifier = Modifier.size(64.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = if (query.isEmpty()) "Esme está vacía." else "No se encontró nada.",
            color = Color.Gray
        )
    }
}

@Composable
fun DashboardCard(total: Double) {
    val displayTotal = ((total * 100).toInt() / 100.0).toString()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16201A)),
        border = BorderStroke(1.dp, Color(0xFF50C878).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Balance de Gastos",
                    color = Color(0xFF50C878).copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$ $displayTotal",
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF50C878).copy(0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.TrendingUp, null, tint = Color(0xFF50C878))
            }
        }
    }
}

@Composable
fun TaskDashboard(
    tasks: List<EsmeBlock.Todo>,
    onToggle: (EsmeBlock.Todo, Boolean) -> Unit
) {
    if (tasks.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16201A)),
        border = BorderStroke(1.dp, Color(0xFF50C878).copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Tareas Pendientes",
                    color = Color(0xFF50C878),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${tasks.size}",
                    color = Color(0xFF50C878).copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            tasks.take(5).forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = { onToggle(task, !task.isChecked) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (task.isChecked)
                                Icons.Default.CheckCircle
                            else
                                Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (task.isChecked)
                                Color(0xFF50C878)
                            else
                                Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = task.content.ifBlank { "Tarea sin texto" },
                        color = Color.White.copy(0.9f),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (tasks.size > 5) {
                Text(
                    text = "y ${tasks.size - 5} más...",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp, start = 36.dp)
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MetaTagDashboard(
    metaTags: List<MetaTag>,
    onClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16201A)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                "Tags",
                color = Color.White.copy(0.7f),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                metaTags.take(15).forEach { tag ->
                    MetaTagChip(tag, onClick)
                }
            }
        }
    }
}

@Composable
fun MetaTagChip(
    tag: MetaTag,
    onClick: (String) -> Unit
) {
    val color = when (tag) {
        is MetaTag.Hashtag -> Color(0xFF50C878)
        is MetaTag.Mention -> Color(0xFF4FC3F7)
    }

    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(50))
            .clickable { onClick(tag.value) }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {

        Text(
            text = tag.value,
            color = Color.White
        )

        Spacer(Modifier.width(6.dp))

        Text(
            text = tag.count.toString(),
            color = color,
            fontSize = 11.sp
        )
    }
}