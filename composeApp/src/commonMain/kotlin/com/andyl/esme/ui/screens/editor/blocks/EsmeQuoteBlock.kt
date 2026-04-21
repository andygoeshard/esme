package com.andyl.esme.ui.screens.editor.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .drawBehind {
                // línea lateral
                drawLine(
                    color = Color(0xFF50C878).copy(alpha = 0.5f),
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 3.dp.toPx()
                )
            }
            .padding(start = 14.dp)
            .background(
                Color(0xFF50C878).copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {

        EsmeBaseTextField(
            blockId = blockId,
            content = content,
            onContentChange = onContentChange,
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontSize = 17.sp,
                fontStyle = FontStyle.Italic,
                color = Color.White.copy(0.85f),
                lineHeight = 24.sp
            ),
            onFocusChangedExternal = onFocusChanged,
            forceCursorToEnd = forceCursorToEnd
        )
    }
}