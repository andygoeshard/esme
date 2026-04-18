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

@Composable
fun EsmeTextFieldBlock(
    modifier: Modifier = Modifier,
    content: String,
    onContentChange: (String) -> Unit,
    onNextBlock: () -> Unit,
    onDeleteIfEmpty: () -> Unit,
    transformer: VisualTransformation
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    BasicTextField(
        value = content,
        onValueChange = { if (it.isEmpty()) onDeleteIfEmpty() else onContentChange(it) },
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(content) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val down = event.changes.find { it.pressed }
                        if (down != null) {
                            textLayoutResult?.let { layout ->
                                val offset = layout.getOffsetForPosition(down.position)
                                val transformed = transformer.filter(AnnotatedString(content)).text

                                transformed.getLinkAnnotations(offset, offset).firstOrNull()?.let { annotation ->
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
            },
        onTextLayout = { textLayoutResult = it },
        visualTransformation = transformer,
        textStyle = TextStyle(color = Color.White, fontSize = 16.sp, lineHeight = 22.sp),
        cursorBrush = SolidColor(Color(0xFF50C878)),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { onNextBlock() })
    )
}