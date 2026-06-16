package com.xjf.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xjf.devjourney.core.data.DevJourneyRepository
import com.xjf.devjourney.core.model.LearningTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(repository: DevJourneyRepository) : ViewModel() {
    val uiState: StateFlow<TasksUiState> = repository.tasks.map { tasks ->
        TasksUiState.Ready(
            tasks = tasks
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TasksUiState.Loading,
    )
}

sealed interface TasksUiState {
    data object Loading : TasksUiState

    data class Ready(
        val tasks: List<LearningTask>
    ) : TasksUiState
}