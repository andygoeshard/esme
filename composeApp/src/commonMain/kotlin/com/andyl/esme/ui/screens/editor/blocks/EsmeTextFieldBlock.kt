package com.andyl.esme.ui.screens.editor.blocks

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp

@Composable
fun EsmeTextFieldBlock(
    modifier: Modifier = Modifier,
    content: String,
    onContentChange: (String) -> Unit,
    onNextBlock: () -> Unit,
    onDeleteIfEmpty: () -> Unit,
    transformer: VisualTransformation
) {
    TextField(
        value = content,
        onValueChange = {
            if (it.endsWith("\n")) onNextBlock()
            else onContentChange(it)
        },
        visualTransformation = transformer,
        modifier = modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color(0xFF50C878),
            focusedTextColor = Color.White.copy(0.9f),
            unfocusedTextColor = Color.White
        ),
        textStyle = TextStyle(fontSize = 18.sp, lineHeight = 26.sp)
    )
}
