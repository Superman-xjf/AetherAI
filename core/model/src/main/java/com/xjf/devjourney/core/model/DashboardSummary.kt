package com.xjf.devjourney.core.model

data class DashboardSummary(
    val topicCount: Int,
    val totalTaskCount: Int,
    val completedTaskCount: Int,
    val activeTaskCount: Int,
    val overallProgress: Float,
)
