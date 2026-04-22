package com.andyl.esme.ui.screens.editor.blocks

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.andyl.esme.ui.screens.editor.components.EsmeBaseTextField

@Composable
fun EsmeTextFieldBlock(
    blockId: String,
    modifier: Modifier = Modifier,
    content: String,
    onContentChange: (String) -> Unit,
    onNextBlock: (Int) -> Unit,
    onDeleteIfEmpty: () -> Unit,
    transformer: VisualTransformation,
    onFocusChanged: (Boolean) -> Unit,
    forceCursorToEnd: Boolean,
) {
    EsmeBaseTextField(
        blockId = blockId,
        content = content,
        onContentChange = {
            if (it.isEmpty()) onDeleteIfEmpty() else onContentChange(it)
        },
        onEnter = { cursor ->
            onNextBlock(cursor)
        },
        visualTransformation = transformer,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next
        ),
        style = TextStyle(
            color = Color.White,
            fontSize = 16.sp,
            lineHeight = 22.sp
        ),
        modifier = modifier.fillMaxWidth(),
        onFocusChangedExternal = onFocusChanged,
        forceCursorToEnd = forceCursorToEnd,
    )
}