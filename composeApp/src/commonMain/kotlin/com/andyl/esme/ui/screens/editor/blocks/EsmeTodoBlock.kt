package com.andyl.esme.ui.screens.editor.blocks

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andyl.esme.ui.screens.editor.components.EsmeBaseTextField

@Composable
fun EsmeTodoBlock(
    modifier: Modifier = Modifier,
    blockId: String,
    content: String,
    isChecked: Boolean,
    onContentChange: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF50C878),
                uncheckedColor = Color(0xFF50C878).copy(0.4f),
                checkmarkColor = Color(0xFF0B120E)
            )
        )

        EsmeBaseTextField(
            modifier = modifier.weight(1f),
            content = content,
            blockId = blockId,
            onContentChange = onContentChange,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            style = TextStyle(
                color = if (isChecked) Color.Gray else Color.White,
                fontSize = 18.sp,
                textDecoration = if (isChecked) TextDecoration.LineThrough else null
            )
        )

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Borrar",
                tint = Color.White.copy(0.3f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}