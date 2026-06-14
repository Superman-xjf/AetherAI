package com.xjf.devjourney.core.model

data class StudyNote(
    val id: String,
    val title: String,
    val excerpt: String,
    val tags: List<String>,
)
