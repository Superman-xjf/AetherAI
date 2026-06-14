package com.xjf.devjourney.core.model

data class LearningTask(
    val id: String,
    val title: String,
    val topic: String,
    val status: TaskStatus,
)

enum class TaskStatus {
    Todo,
    Doing,
    Done,
}
