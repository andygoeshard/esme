package com.andyl.esme.ui.screens.editor.transformer

import com.andyl.esme.domain.engine.EsmeBlockEngine
import com.andyl.esme.domain.model.EsmeBlock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object EsmeMultiBlockParser {

    @OptIn(ExperimentalUuidApi::class)
    fun parse(noteId: String, raw: String): List<EsmeBlock> {
        return raw.split("\n").mapIndexed { index, line ->

            val base = EsmeBlock.Text(
                id = Uuid.random().toString(),
                noteId = noteId,
                orderIndex = index,
                content = ""
            )

            val parsed = EsmeBlockEngine.process(base, line)
            parsed
        }
    }
}