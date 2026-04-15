package com.andyl.esme.domain.model

data class EsmeNote(
    val id: String,
    val title: String,
    val blocks: List<EsmeBlock>
)