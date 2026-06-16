package com.xjf.devjourney.core.model

data class LearningTask(
    val id: String,
    val title: String,
    val topic: String,
    val status: TaskStatus,
)

enum class TaskStatus(val label: String) {
    Todo("待开始"),
    Doing("进行中"),
    Done("已完成"),
}
