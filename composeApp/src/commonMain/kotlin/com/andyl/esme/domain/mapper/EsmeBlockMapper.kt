package com.andyl.esme.domain.mapper

import com.andyl.esme.data.local.entity.BlockEntity
import com.andyl.esme.domain.model.EsmeBlock
import com.andyl.esme.domain.model.PriorityLevel

object EsmeBlockMapper {
    fun toDomain(entity: BlockEntity): EsmeBlock {
        return when (entity.type) {
            "TEXT" -> EsmeBlock.Text(entity.id, entity.noteId, entity.orderIndex, entity.content ?: "")
            "TODO" -> EsmeBlock.Todo(entity.id, entity.noteId, entity.orderIndex, entity.content ?: "", entity.isChecked ?: false)
            "PRIORITY" -> EsmeBlock.Priority(entity.id, entity.noteId, entity.orderIndex, entity.content ?: "", PriorityLevel.valueOf(entity.priorityLevel ?: "HIGH"))
            "EXPENSE" -> EsmeBlock.Expense(entity.id, entity.noteId, entity.orderIndex, entity.content ?: "", entity.amount ?: 0.0)
            "DIVIDER" -> EsmeBlock.Divider(entity.id, entity.noteId, entity.orderIndex)
            "IMAGE" -> EsmeBlock.Image(entity.id, entity.noteId, entity.orderIndex, entity.mediaUri ?: "", entity.caption ?: "")
            "CODE" -> EsmeBlock.Code(entity.id, entity.noteId, entity.orderIndex, entity.content ?: "")
            "QUOTE" -> EsmeBlock.Quote(entity.id, entity.noteId, entity.orderIndex, entity.content ?: "")
            "BULLET" -> EsmeBlock.Bullet(entity.id, entity.noteId, entity.orderIndex, entity.content ?: "")
            else -> EsmeBlock.Text(entity.id, entity.noteId, entity.orderIndex, entity.content ?: "")
        }
    }

    fun toEntity(block: EsmeBlock): BlockEntity {
        return when (block) {
            is EsmeBlock.Text -> BlockEntity(block.id, block.noteId, block.orderIndex, "TEXT", content = block.content)
            is EsmeBlock.Todo -> BlockEntity(block.id, block.noteId, block.orderIndex, "TODO", content = block.content, isChecked = block.isChecked)
            is EsmeBlock.Priority -> BlockEntity(block.id, block.noteId, block.orderIndex, "PRIORITY", content = block.content, priorityLevel = block.level.name)
            is EsmeBlock.Expense -> BlockEntity(block.id, block.noteId, block.orderIndex, "EXPENSE", content = block.description, amount = block.amount)
            is EsmeBlock.Divider -> BlockEntity(block.id, block.noteId, block.orderIndex, "DIVIDER")
            is EsmeBlock.Image -> BlockEntity(block.id, block.noteId, block.orderIndex, "IMAGE", mediaUri = block.uri, caption = block.caption)
            is EsmeBlock.Code -> BlockEntity(block.id, block.noteId, block.orderIndex, "CODE", content = block.content)
            is EsmeBlock.Quote -> BlockEntity(block.id, block.noteId, block.orderIndex, "QUOTE", content = block.content)
            is EsmeBlock.Bullet -> BlockEntity(block.id, block.noteId, block.orderIndex, "BULLET", content = block.content)
            else -> throw IllegalArgumentException("Tipo de bloque no soportado")
        }
    }
}