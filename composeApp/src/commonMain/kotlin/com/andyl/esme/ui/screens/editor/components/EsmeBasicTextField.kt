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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
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

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

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
            }
            .metaTagClickable(
                blockId = blockId,
                content = textFieldValue.text,
                textLayoutResult = textLayoutResult,
                transformer = visualTransformation
            )
        ,
        visualTransformation = visualTransformation,
        onTextLayout = { textLayoutResult = it },
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

fun Modifier.metaTagClickable(
    blockId: String,
    content: String,
    textLayoutResult: TextLayoutResult?,
    transformer: VisualTransformation
): Modifier = pointerInput(blockId, content, textLayoutResult) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val down = event.changes.find { it.pressed }

            if (down != null) {
                val layout = textLayoutResult ?: continue

                val offset = layout.getOffsetForPosition(down.position)

                val transformed = transformer
                    .filter(AnnotatedString(content))
                    .text

                transformed.getLinkAnnotations(offset, offset)
                    .firstOrNull()
                    ?.let { annotation ->
                        val clickable = annotation.item as? LinkAnnotation.Clickable
                        if (clickable != null) {
                            down.consume()
                            clickable.linkInteractionListener?.onClick(clickable)
                        }
                    }
            }
        }
    }
}