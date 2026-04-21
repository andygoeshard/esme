package com.andyl.esme.ui.screens.tag

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andyl.esme.domain.model.EsmeBlock
import com.andyl.esme.domain.model.EsmeNote
import com.andyl.esme.ui.screens.home.EmptyState
import com.andyl.esme.ui.screens.home.MetaTag
import com.andyl.esme.ui.screens.home.MetaTagChip
import com.andyl.esme.ui.screens.home.TaskDashboard
import com.andyl.esme.ui.screens.home.components.HomeNoteItem
import com.andyl.esme.ui.screens.tag.components.TagGraph
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagScreen(
    tag: String,
    onOpenNote: (String) -> Unit,
    onBack: () -> Unit,
    onNavigateToTag: (String) -> Unit
) {
    val viewModel = koinViewModel<TagViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(tag) {
        viewModel.load(tag)
    }

    val hub = state.hub

    Scaffold(
        containerColor = Color(0xFF0B120E),
        topBar = {
            TagTopBar(
                tag = tag,
                count = hub?.stats?.noteCount ?: 0,
                onBack = onBack
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF50C878)
                    )
                }

                hub == null || hub.notes.isEmpty() -> {
                    TagEmptyState(tag)
                }

                else -> {
                    TagHubContent(
                        hub = hub,
                        onOpenNote = onOpenNote,
                        onToggleTask = viewModel::toggleTask,
                        onNavigateToTag = onNavigateToTag
                    )
                }
            }
        }
    }
}

@Composable
fun TagHubContent(
    hub: TagHubData,
    onOpenNote: (String) -> Unit,
    onToggleTask: (EsmeBlock.Todo, Boolean) -> Unit,
    onNavigateToTag: (String) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(170.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp
    ) {

        // 🔥 STATS
        item(span = StaggeredGridItemSpan.FullLine) {
            TagStatsCard(hub.stats)
        }

        // 🔗 RELATED TAGS
        if (hub.relatedTags.isNotEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) {
                RelatedTagsSection(
                    tags = hub.relatedTags,
                    onClick = onNavigateToTag
                )
            }
        }

        // 🧠 NOTAS
        items(
            hub.notes,
            key = { it.id }
        ) { note ->
            HomeNoteItem(
                modifier = Modifier.animateItem(),
                item = note,
                onClick = { onOpenNote(note.id) },
                onDelete = {}
            )
        }

        // ✅ TASKS
        val pendingTasks = hub.notes
            .flatMap { it.blocks }
            .filterIsInstance<EsmeBlock.Todo>()
            .filter { !it.isChecked }

        if (pendingTasks.isNotEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) {
                TaskDashboard(pendingTasks, onToggleTask)
            }
        }

        item(span = StaggeredGridItemSpan.FullLine) {
            TagGraph(
                centerTag = hub.tag,
                related = hub.relatedTags,
                onClick = onNavigateToTag
            )
        }
    }
}

@Composable
fun TagStatsCard(stats: TagStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        Text("Resumen", color = Color.Gray, fontSize = 12.sp)

        Spacer(Modifier.height(8.dp))

        Text("Notas: ${stats.noteCount}", color = Color.White)
        Text("Pendientes: ${stats.pendingTasks}", color = Color(0xFF50C878))
        Text("Gastos: $${stats.totalExpenses}", color = Color(0xFF4FC3F7))
    }
}

@Composable
fun RelatedTagsSection(
    tags: List<RelatedTag>,
    onClick: (String) -> Unit
) {
    Column {

        Text(
            text = "Relacionados",
            color = Color.White.copy(0.7f),
            fontSize = 13.sp
        )

        Spacer(Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                MetaTagChip(
                    tag = MetaTag.Hashtag(tag.tag, tag.count),
                    onClick = onClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagTopBar(
    tag: String,
    count: Int,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = tag,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "$count notas",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
fun TagEmptyState(tag: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = tag,
            color = Color.Gray.copy(0.5f),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "No hay notas con este tag.",
            color = Color.Gray
        )
    }
}