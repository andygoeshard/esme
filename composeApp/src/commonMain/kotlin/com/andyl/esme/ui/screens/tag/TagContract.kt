package com.andyl.esme.ui.screens.tag

import com.andyl.esme.domain.model.EsmeNote

data class TagState(
    val tag: String = "",
    val hub: TagHubData? = null,
    val isLoading: Boolean = false
)

data class TagHubData(
    val tag: String,
    val notes: List<EsmeNote>,
    val relatedTags: List<RelatedTag>,
    val stats: TagStats
)

data class RelatedTag(
    val tag: String,
    val count: Int
)

data class TagStats(
    val noteCount: Int,
    val pendingTasks: Int,
    val totalExpenses: Double
)