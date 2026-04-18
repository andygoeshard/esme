package com.andyl.esme.domain.engine

import com.andyl.esme.domain.model.EsmeBlock

object EsmeBlockEngine {

    fun process(
        block: EsmeBlock,
        input: String
    ): EsmeBlock {

        val noteId = block.noteId
        val id = block.id
        val order = block.orderIndex

        return when {

            // --- TODO ---
            input.startsWith("- [ ] ") -> {
                EsmeBlock.Todo(
                    id,
                    noteId,
                    order,
                    input.removePrefix("- [ ] "),
                    false
                )
            }

            // --- PRIORITY ---
            input.startsWith("!!! ") -> {
                EsmeBlock.Priority(
                    id,
                    noteId,
                    order,
                    input.removePrefix("!!! ")
                )
            }

            // --- EXPENSE ---
            input.startsWith("$ ") -> {
                val raw = input.removePrefix("$ ").trim()
                val parts = raw.split(" ", limit = 2)

                val amount = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                val description =
                    if (parts.size > 1) parts[1]
                    else if (amount == 0.0) raw
                    else "Gasto"

                EsmeBlock.Expense(
                    id,
                    noteId,
                    order,
                    description,
                    amount
                )
            }

            // --- QUOTE ---
            input.startsWith("> ") -> {
                EsmeBlock.Quote(
                    id,
                    noteId,
                    order,
                    input.removePrefix("> ")
                )
            }

            // --- DIVIDER ---
            input.startsWith("---") -> {
                EsmeBlock.Divider(id, noteId, order)
            }

            // --- DEFAULT (NO CAMBIA TIPO) ---
            else -> {
                when (block) {
                    is EsmeBlock.Text -> block.copy(content = input)
                    is EsmeBlock.Todo -> block.copy(content = input)
                    is EsmeBlock.Priority -> block.copy(content = input)
                    is EsmeBlock.Quote -> block.copy(content = input)
                    is EsmeBlock.Bullet -> block.copy(content = input)
                    is EsmeBlock.Code -> block.copy(content = input)
                    is EsmeBlock.Expense -> block.copy(description = input)
                    else -> block
                }
            }
        }
    }
}