package com.andyl.esme.ui.screens.editor.transformer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.NonCancellable.start

class EsmeSyntaxTransformer(
    private val onLinkClick: (String) -> Unit
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text

        val out = buildAnnotatedString {
            append(originalText)

            // --- FLECHAS ---
            Regex("->|=>|➔").findAll(originalText).forEach { result ->
                addStyle(
                    style = SpanStyle(color = Color(0xFF50C878), fontWeight = FontWeight.Black),
                    start = result.range.first,
                    end = result.range.last + 1
                )
            }

            // --- LINKS [[Nota]] ---
            Regex("\\[\\[(.*?)\\]\\]").findAll(originalText).forEach { result ->
                val title = result.groupValues[1]
                val start = result.range.first
                val end = result.range.last + 1

                addStyle(
                    style = SpanStyle(
                        color = Color(0xFF50C878),
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    ),
                    start = start,
                    end = end
                )

                addLink(
                    clickable = LinkAnnotation.Clickable(
                        tag = "URL",
                        linkInteractionListener = { onLinkClick(title) }
                    ),
                    start = start,
                    end = end
                )
            }

            // --- MENCIONES @andy ---
            Regex("@[a-zA-Z0-9_-]+").findAll(originalText).forEach { result ->
                addStyle(
                    style = SpanStyle(color = Color(0xFF87CEEB), fontWeight = FontWeight.Bold),
                    start = result.range.first,
                    end = result.range.last + 1
                )
            }

            // --- HASHTAGS #esme ---
            Regex("#[a-zA-Z0-9_-]+").findAll(originalText).forEach { result ->
                addStyle(
                    style = SpanStyle(color = Color(0xFF98FB98), fontWeight = FontWeight.Bold),
                    start = result.range.first,
                    end = result.range.last + 1
                )
            }

            // --- SMART TOKENS //hoy y //hora ---
            Regex("//hoy|//hora").findAll(originalText).forEach { result ->
                addStyle(
                    style = SpanStyle(color = Color(0xFF50C878), background = Color(0xFF50C878).copy(0.1f)),
                    start = result.range.first,
                    end = result.range.last + 1
                )
            }

            // --- TASK TRIGGER - [ ] ---
            Regex("- \\[ \\]").findAll(originalText).forEach { result ->
                addStyle(
                    style = SpanStyle(color = Color(0xFF50C878), fontWeight = FontWeight.ExtraBold),
                    start = result.range.first,
                    end = result.range.last + 1
                )
            }

            // --- CÁLCULOS (10+10)= ---
            Regex("\\(.*?\\)=").findAll(originalText).forEach { result ->
                addStyle(
                    style = SpanStyle(color = Color(0xFFF0E68C), fontWeight = FontWeight.Bold),
                    start = result.range.first,
                    end = result.range.last + 1
                )
            }
        }

        return TransformedText(out, OffsetMapping.Identity)
    }
}