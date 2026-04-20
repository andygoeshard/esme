package com.andyl.esme.ui.screens.editor.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp

@Composable
fun EsmeBaseTextField(
    blockId: String,
    modifier: Modifier = Modifier,
    content: String,
    onContentChange: (String) -> Unit,
    onNextBlock: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = TextStyle(color = Color.White, fontSize = 16.sp),
    onFocusChangedExternal: ((Boolean) -> Unit)? = null,
    forceCursorToEnd: Boolean = false,
    onEnter: ((cursorPosition: Int) -> Unit)? = null,
) {
    var textFieldValue by remember(blockId) {
        mutableStateOf(TextFieldValue(text = content))
    }

    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(content, forceCursorToEnd) {
        if (forceCursorToEnd) {
            textFieldValue = TextFieldValue(
                text = content,
                selection = TextRange(content.length)
            )
        } else if (!isFocused && content != textFieldValue.text) {
            textFieldValue = TextFieldValue(
                text = content,
                selection = TextRange(content.length)
            )
        }
    }

    BasicTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            textFieldValue = newValue
            onContentChange(newValue.text)
        },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged {
                isFocused = it.isFocused
                onFocusChangedExternal?.invoke(it.isFocused)
            },
        visualTransformation = visualTransformation,
        onTextLayout = onTextLayout,
        textStyle = style,
        cursorBrush = SolidColor(Color(0xFF50C878)),
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(
            onNext = {
                onEnter?.invoke(textFieldValue.selection.start)
            }
        )
    )
}