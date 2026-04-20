package com.andyl.esme.ui.screens.editor.blocks

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andyl.esme.ui.screens.editor.components.EsmeBaseTextField

@Composable
fun EsmeQuoteBlock(
    modifier: Modifier = Modifier,
    blockId: String,
    content: String,
    onContentChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    forceCursorToEnd: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .drawBehind {
                drawLine(
                    color = Color(0xFF50C878),
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 4.dp.toPx()
                )
            }
            .padding(start = 16.dp)
    ) {
        EsmeBaseTextField(
            blockId = blockId,
            content = content,
            onContentChange = onContentChange,
            modifier = modifier,
            style = TextStyle(
                fontSize = 18.sp,
                fontStyle = FontStyle.Italic,
                color = Color.White.copy(0.8f)
            ),
            onFocusChangedExternal = onFocusChanged,
            forceCursorToEnd = forceCursorToEnd
        )
    }
}