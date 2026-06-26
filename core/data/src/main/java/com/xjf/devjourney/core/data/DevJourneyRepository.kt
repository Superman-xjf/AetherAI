package com.xjf.devjourney.core.data

import com.xjf.devjourney.core.model.LearningTask
import com.xjf.devjourney.core.model.LearningTopic
import com.xjf.devjourney.core.model.StudyNote
import com.xjf.devjourney.core.model.TaskStatus
import kotlinx.coroutines.flow.Flow

interface DevJourneyRepository {
    val topics: Flow<List<LearningTopic>>
    val tasks: Flow<List<LearningTask>>
    val notes: Flow<List<StudyNote>>

    suspend fun addTask(title: String, topic: String, status: TaskStatus = TaskStatus.Todo)

    suspend fun updateTask(learningTask: LearningTask)

    suspend fun deleteTask(taskId: String)

    suspend fun updateTaskStatus(taskId: String, status: TaskStatus)
}
