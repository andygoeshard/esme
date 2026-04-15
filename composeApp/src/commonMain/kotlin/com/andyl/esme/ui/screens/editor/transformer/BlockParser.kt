package com.andyl.esme.ui.screens.editor.transformer

import com.andyl.esme.domain.model.EsmeBlock

object BlockParser {
    fun parse(
        block: EsmeBlock.Text,
        content: String
    ): Pair<EsmeBlock, Boolean> {
        val noteId = block.noteId
        val blockId = block.id

        return when {
            content.startsWith("- [ ] ") -> {
                EsmeBlock.Todo(
                    blockId,
                    noteId,
                    block.orderIndex,
                    content.removePrefix("- [ ] "),
                    false
                ) to true
            }

            content.startsWith("!!! ") -> {
                EsmeBlock.Priority(
                    blockId,
                    noteId,
                    block.orderIndex,
                    content.removePrefix("!!! ")
                ) to true
            }

            content.startsWith("$ ") -> {
                val raw = content.removePrefix("$ ").trim()
                val parts = raw.split(" ", limit = 2)
                val amount = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                val desc = if (parts.size > 1) parts[1] else "Gasto"

                EsmeBlock.Expense(
                    blockId,
                    noteId,
                    block.orderIndex,
                    desc,
                    amount
                ) to true
            }

            content.startsWith("---") ->
                EsmeBlock.Divider(blockId, noteId, block.orderIndex) to true

            content.startsWith("> ") -> {
                EsmeBlock.Quote(
                    blockId,
                    noteId,
                    block.orderIndex,
                    content.removePrefix("> ")
                ) to true
            }

            else -> block.copy(content = content) to false
        }
    }
}