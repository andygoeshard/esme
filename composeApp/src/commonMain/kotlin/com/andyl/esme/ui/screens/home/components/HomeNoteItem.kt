package com.andyl.esme.ui.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andyl.esme.data.local.model.NoteWithBlocks
import com.andyl.esme.domain.model.EsmeBlock
import com.andyl.esme.domain.model.EsmeNote

@Composable
fun HomeNoteItem(
    item: EsmeNote,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16201A)),
        border = BorderStroke(0.5.dp, Color(0xFF50C878).copy(alpha = 0.2f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = item.title.ifBlank { "Sin título" },
                    color = Color(0xFF50C878),
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                val lines = item.blocks
                    .sortedBy { it.orderIndex }
                    .take(4)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val lines = item.blocks
                        .sortedBy { it.orderIndex }
                        .take(4)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        lines.forEach { block ->
                            when (block) {

                                is EsmeBlock.Todo -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (block.isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (block.isChecked) Color(0xFF50C878).copy(0.5f) else Color.Gray,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = block.content,
                                            color = if (block.isChecked) Color.Gray else Color.White.copy(0.8f),
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textDecoration = if (block.isChecked) TextDecoration.LineThrough else null
                                        )
                                    }
                                }

                                is EsmeBlock.Expense -> {
                                    Text(
                                        text = "💰 ${block.amount} ${block.description}",
                                        color = Color(0xFF50C878),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                is EsmeBlock.Priority -> {
                                    Text(
                                        text = "🚨 ${block.content}",
                                        color = Color.Red.copy(0.7f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                is EsmeBlock.Text -> {
                                    Text(
                                        text = block.content,
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 12.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 16.sp
                                    )
                                }

                                is EsmeBlock.Quote -> {
                                    Text(
                                        text = "> ${block.content}",
                                        color = Color.Gray,
                                        fontSize = 12.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }

                                is EsmeBlock.Divider -> {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                else -> {}
                            }
                        }
                    }
                }

                if (item.blocks.size > 4) {
                    Text(
                        text = "• • •",
                        color = Color(0xFF50C878).copy(0.2f),
                        modifier = Modifier.padding(top = 4.dp),
                        fontSize = 10.sp
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.Red.copy(alpha = 0.2f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}