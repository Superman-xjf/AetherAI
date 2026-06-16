package com.xjf.devjourney.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xjf.devjourney.core.data.DevJourneyRepository
import com.xjf.devjourney.core.model.DashboardSummary
import com.xjf.devjourney.core.model.LearningTask
import com.xjf.devjourney.core.model.LearningTopic
import com.xjf.devjourney.core.model.StudyNote
import com.xjf.devjourney.core.model.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DashboardViewModel @Inject constructor(
    repository: DevJourneyRepository,
) : ViewModel() {
    val uiState: StateFlow<DashboardUiState> = combine(
        repository.topics,
        repository.tasks,
        repository.notes,
    ) { topics, tasks, notes ->
        val topicCount = topics.size
        val totalTaskCount = tasks.size
        val currentTaskCount = tasks.filterNot { tasks -> tasks.status == TaskStatus.Done }.size
        val completedTaskCount = tasks.filter { tasks -> tasks.status == TaskStatus.Done }.size
        val progress = if (tasks.isEmpty()) {
            0f
        } else {
            completedTaskCount.toFloat() / tasks.size
        }
        DashboardUiState.Ready(
            summary = DashboardSummary(
                topicCount,
                totalTaskCount,
                completedTaskCount,
                currentTaskCount,
                progress
            ),
            topics = topics,
            activeTasks = tasks.filterNot { it.status == TaskStatus.Done },
            recentNotes = notes,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState.Loading,
    )
}

sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    data class Ready(
        val summary: DashboardSummary,
        val topics: List<LearningTopic>,
        val activeTasks: List<LearningTask>,
        val recentNotes: List<StudyNote>,
    ) : DashboardUiState
}
