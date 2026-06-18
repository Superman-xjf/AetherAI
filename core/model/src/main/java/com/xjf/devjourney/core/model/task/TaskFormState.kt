package com.xjf.devjourney.core.model.task

import com.xjf.devjourney.core.model.TaskStatus

data class TaskFormState(
    val title: String = "",
    val topic: String = "",
    val status: TaskStatus = TaskStatus.Todo,
    val titleError: String? = null,
)
