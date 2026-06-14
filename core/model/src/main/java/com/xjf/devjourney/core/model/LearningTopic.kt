package com.xjf.devjourney.core.model

data class LearningTopic(
    val id: String,
    val title: String,
    val description: String,
    val progress: Float,
    val taskCount: Int,
    val completedTaskCount: Int,
)
