package com.andyl.esme.ui.screens.editor.transformer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle

class EsmeSyntaxTransformer : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val out = buildAnnotatedString {
            append(originalText) // Ponemos todo el texto base primero

            // 1. Buscamos y aplicamos estilos sobre el texto ya existente

            // --- FLECHAS (Estilo sobre los caracteres existentes) ---
            Regex("->|=>").findAll(originalText).forEach { result ->
                addStyle(
                    style = SpanStyle(color = Color(0xFF50C878), fontWeight = FontWeight.Black),
                    start = result.range.first,
                    end = result.range.last + 1
                )
            }

            // --- LINKS [[Nota]] ---
            Regex("\\[\\[.*?\\]\\]").findAll(originalText).forEach { result ->
                addStyle(
                    style = SpanStyle(
                        color = Color(0xFF50C878),
                        fontWeight = FontWeight.Bold,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    ),
                    start = result.range.first,
                    end = result.range.last + 1
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

            // --- SMART TOKENS //hoy y //hora ---
            Regex("//hoy|//hora").findAll(originalText).forEach { result ->
                addStyle(
                    style = SpanStyle(color = Color(0xFF50C878), background = Color(0xFF50C878).copy(0.1f)),
                    start = result.range.first,
                    end = result.range.last + 1
                )
            }


            Regex("- \\[ \\]").findAll(originalText).forEach { result ->
                addStyle(
                    style = SpanStyle(color = Color(0xFF50C878), fontWeight = FontWeight.ExtraBold),
                    start = result.range.first,
                    end = result.range.last + 1
                )
            }
        }

        // Como NO modificamos el largo del texto (solo agregamos estilos),
        // el OffsetMapping es una línea recta. 0 errores, 0 crashes.
        return TransformedText(out, OffsetMapping.Identity)
    }
}