package com.andyl.esme.ui.screens.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EsmeToolbar(
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF16201A),
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(
            width = 0.5.dp,
            brush = SolidColor(Color(0xFF50C878).copy(alpha = 0.2f))
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EsmeActionButton(Icons.Default.Title, "TÍTULO") { onAction("# ") }
            EsmeActionButton(Icons.Default.ArrowForward, "FLUJO") { onAction(" -> ") }
            EsmeActionButton(Icons.Default.Event, "HOY") { onAction("//hoy") }
            EsmeActionButton(Icons.Default.Calculate, "CALC") { onAction("(10+10)=") }
            EsmeActionButton(Icons.Default.CheckCircle, "TAREA") { onAction("- [ ] ") }
            EsmeActionButton(Icons.Default.Link, "LINK") { onAction("[[") }
            EsmeActionButton(Icons.Default.Title, "TÍTULO") { onAction("# ") }
            EsmeActionButton(Icons.Default.HorizontalRule, "SEP") { onAction("---") }
        }
    }
}

@Composable
private fun EsmeActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF50C878),
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}