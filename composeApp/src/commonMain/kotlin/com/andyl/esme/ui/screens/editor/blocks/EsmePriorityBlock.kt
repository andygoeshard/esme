package com.andyl.esme.ui.screens.editor.blocks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andyl.esme.ui.screens.editor.components.EsmeBaseTextField

@Composable
fun EsmePriorityBlock(
    modifier: Modifier = Modifier,
    blockId: String,
    content: String,
    onContentChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    forceCursorToEnd: Boolean,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Surface(
        color = Color(0xFFFF4444).copy(alpha = 0.1f),
        border = BorderStroke(1.dp, Color(0xFFFF4444).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFFF4444),
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(12.dp))

            EsmeBaseTextField(
                blockId = blockId,
                content = content,
                onContentChange = onContentChange,
                modifier = modifier.fillMaxWidth(),
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                onFocusChangedExternal = onFocusChanged,
                forceCursorToEnd = forceCursorToEnd,
                visualTransformation = visualTransformation,
            )
        }
    }
}