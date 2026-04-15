package com.andyl.esme.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andyl.esme.data.local.entity.BlockEntity
import com.andyl.esme.domain.model.EsmeBlock
import com.andyl.esme.ui.screens.home.components.HomeNoteItem
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToEditor: (String?) -> Unit
) {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

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
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(minSize = 170.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp
                ) {
                    items(
                        state.notes,
                        key = { noteWithBlocks ->
                            noteWithBlocks.id + noteWithBlocks.blocks.hashCode()
                        }
                    ) { item ->
                        HomeNoteItem(
                            modifier = Modifier.animateItem(),
                            item = item,
                            onClick = { onNavigateToEditor(item.id) },
                            onDelete = { viewModel.handleIntent(HomeIntent.DeleteNote(item.id)) }
                        )
                    }
                    if (!state.isSearchVisible) {
                    val pendingTasks = state.notes
                        .flatMap { it.blocks }
                        .filterIsInstance<EsmeBlock.Todo>()
                        .filter { !it.isChecked }

                    if (pendingTasks.isNotEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            TaskDashboard(
                                tasks = pendingTasks,
                                onToggle = { block, isChecked ->
                                    viewModel.handleIntent(HomeIntent.ToggleTask(block, isChecked))
                                }
                            )
                        }
                    }
                }
                    if (!state.isSearchVisible && state.totalExpenses > 0.0) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            DashboardCard(total = state.totalExpenses)
                        }
                    }

                }
            }
        }
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