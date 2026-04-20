package com.andyl.esme.domain.engine

import com.andyl.esme.domain.model.EsmeBlock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object BlockEditorEngine {

    data class Result(
        val blocks: List<EsmeBlock>,
        val focusBlockId: String? = null
    )

    fun onEnter(
        blocks: List<EsmeBlock>,
        blockId: String,
        cursorPosition: Int
    ): Result {
        val index = blocks.indexOfFirst { it.id == blockId }
        if (index == -1) return Result(blocks)

        val current = blocks[index]

        if (current !is EsmeBlock.Text) {
            return Result(blocks)
        }

        val text = current.content

        val before = text.take(cursorPosition)
        val after = text.drop(cursorPosition)

        val updatedCurrent = current.copy(content = before)

        val newBlock = EsmeBlock.Text(
            id = generateId(),
            noteId = current.noteId,
            orderIndex = index + 1,
            content = after
        )

        val newList = blocks.toMutableList().apply {
            set(index, updatedCurrent)
            add(index + 1, newBlock)
        }

        return Result(
            blocks = reindex(newList),
            focusBlockId = newBlock.id
        )
    }

    fun onBackspace(
        blocks: List<EsmeBlock>,
        blockId: String,
        cursorPosition: Int
    ): Result {
        val index = blocks.indexOfFirst { it.id == blockId }
        if (index <= 0) return Result(blocks)

        val current = blocks[index]
        val previous = blocks[index - 1]

        if (current !is EsmeBlock.Text || previous !is EsmeBlock.Text) {
            return Result(blocks)
        }

        // 👉 SOLO merge si cursor está al inicio
        if (cursorPosition > 0) return Result(blocks)

        val merged = previous.copy(
            content = previous.content + current.content
        )

        val newList = blocks.toMutableList().apply {
            set(index - 1, merged)
            removeAt(index)
        }

        return Result(
            blocks = reindex(newList),
            focusBlockId = merged.id
        )
    }

    fun onTextChange(
        block: EsmeBlock,
        newText: String
    ): EsmeBlock {
        return when (block) {
            is EsmeBlock.Text -> block.copy(content = newText)
            is EsmeBlock.Todo -> block.copy(content = newText)
            is EsmeBlock.Priority -> block.copy(content = newText)
            is EsmeBlock.Quote -> block.copy(content = newText)
            else -> block
        }
    }

    // --- helpers ---

    private fun reindex(list: List<EsmeBlock>): List<EsmeBlock> {
        return list.mapIndexed { index, block ->
            when (block) {
                is EsmeBlock.Text -> block.copy(orderIndex = index)
                is EsmeBlock.Todo -> block.copy(orderIndex = index)
                is EsmeBlock.Priority -> block.copy(orderIndex = index)
                is EsmeBlock.Quote -> block.copy(orderIndex = index)
                is EsmeBlock.Expense -> block.copy(orderIndex = index)
                is EsmeBlock.Divider -> block.copy(orderIndex = index)
                else -> block
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateId(): String = Uuid.random().toString()
}