package com.andyl.esme.ui.screens.tag.helper

import com.andyl.esme.domain.model.EsmeBlock
import com.andyl.esme.domain.model.EsmeNote
import com.andyl.esme.ui.screens.tag.RelatedTag
import com.andyl.esme.ui.screens.tag.TagHubData
import com.andyl.esme.ui.screens.tag.TagStats

class TagGraphBuilder {

    private val tagRegex = Regex("[#@][a-zA-Z0-9_-]+")

    fun buildTagHub(tag: String, notes: List<EsmeNote>): TagHubData {
        val normalizedTag = tag.lowercase()

        val notesWithTags = notes.map { note ->
            note to extractTags(note)
        }

        val filteredNotes = notesWithTags
            .filter { (_, tags) -> normalizedTag in tags }
            .map { it.first }

        val relations = buildRelations(notesWithTags)

        val relatedTags = relations[normalizedTag]
            ?.entries
            ?.sortedByDescending { it.value }
            ?.take(10)
            ?.map { RelatedTag(it.key, it.value) }
            ?: emptyList()

        val stats = buildStats(filteredNotes)

        return TagHubData(
            tag = tag,
            notes = filteredNotes,
            relatedTags = relatedTags,
            stats = stats
        )
    }

    private fun extractTags(note: EsmeNote): Set<String> {
        return note.blocks
            .mapNotNull { extractText(it) }
            .flatMap { tagRegex.findAll(it).map { it.value.lowercase() } }
            .toSet()
    }

    private fun extractText(block: EsmeBlock): String? {
        return when (block) {
            is EsmeBlock.Text -> block.content
            is EsmeBlock.Todo -> block.content
            is EsmeBlock.Priority -> block.content
            is EsmeBlock.Quote -> block.content
            is EsmeBlock.Bullet -> block.content
            is EsmeBlock.Code -> block.content
            is EsmeBlock.Expense -> block.description
            else -> null
        }
    }

    private fun buildRelations(
        notes: List<Pair<EsmeNote, Set<String>>>
    ): Map<String, MutableMap<String, Int>> {

        val relations = mutableMapOf<String, MutableMap<String, Int>>()

        notes.forEach { (_, tags) ->
            tags.forEach { a ->
                tags.forEach { b ->
                    if (a == b) return@forEach

                    val map = relations.getOrPut(a) { mutableMapOf() }
                    map[b] = (map[b] ?: 0) + 1
                }
            }
        }

        return relations
    }

    private fun buildStats(notes: List<EsmeNote>): TagStats {
        val tasks = notes.flatMap { it.blocks }
            .filterIsInstance<EsmeBlock.Todo>()

        val expenses = notes.flatMap { it.blocks }
            .filterIsInstance<EsmeBlock.Expense>()

        return TagStats(
            noteCount = notes.size,
            pendingTasks = tasks.count { !it.isChecked },
            totalExpenses = expenses.sumOf { it.amount }
        )
    }
}