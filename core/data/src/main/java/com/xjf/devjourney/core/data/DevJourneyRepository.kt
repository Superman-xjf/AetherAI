package com.xjf.devjourney.core.data

import com.xjf.devjourney.core.model.LearningTask
import com.xjf.devjourney.core.model.LearningTopic
import com.xjf.devjourney.core.model.StudyNote
import kotlinx.coroutines.flow.Flow

interface DevJourneyRepository {
    val topics: Flow<List<LearningTopic>>
    val tasks: Flow<List<LearningTask>>
    val notes: Flow<List<StudyNote>>
}
