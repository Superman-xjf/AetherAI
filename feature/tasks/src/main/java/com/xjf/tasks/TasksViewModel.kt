package com.xjf.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xjf.devjourney.core.data.DevJourneyRepository
import com.xjf.devjourney.core.model.LearningTask
import com.xjf.devjourney.core.model.TaskStatus
import com.xjf.devjourney.core.model.task.TaskFormState
import com.xjf.devjourney.core.model.task.TasksSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(private val repository: DevJourneyRepository) :
    ViewModel() {

    private val formState = MutableStateFlow(TaskFormState())
    private val editingTaskId = MutableStateFlow<String?>(null)
    private val deletingTask = MutableStateFlow<LearningTask?>(null)
    private val isFormVisible = MutableStateFlow(false)
    val uiState: StateFlow<TasksUiState> = combine(
        repository.tasks,
        formState,
        editingTaskId,
        isFormVisible,
        deletingTask,
    ) { tasks, form, editingId, formVisible, deletingTask ->
        TasksUiState.Ready(
            summary = TasksSummary(
                totalCount = tasks.size,
                todoCount = tasks.count { it.status == TaskStatus.Todo },
                doingCount = tasks.count { it.status == TaskStatus.Doing },
                doneCount = tasks.count { it.status == TaskStatus.Done },
            ),
            tasks = tasks,
            form = form,
            editingTaskId = editingId,
            isFormVisible = formVisible,
            deletingTask = deletingTask,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TasksUiState.Loading,
    )

    fun onAddTaskClick() {
        editingTaskId.value = null
        formState.value = TaskFormState()

        isFormVisible.value = true
    }

    fun onTitleChange(title: String) {
        formState.value = formState.value.copy(
            title = title,
            titleError = null,
        )
    }

    fun onTopicChange(topic: String) {
        formState.value = formState.value.copy(
            topic = topic,
            titleError = null,
        )
    }

    fun onStatusChange(status: TaskStatus) {
        formState.value = formState.value.copy(
            status = status
        )
    }

    fun onSaveTask() {
        val form = formState.value

        if (form.title.isBlank()) {
            formState.value = form.copy(titleError = "请输入任务标题")
            return
        }

        viewModelScope.launch {
            val taskId = editingTaskId.value

            if (taskId == null) {
                repository.addTask(
                    title = form.title.trim(),
                    topic = form.topic.trim(),
                    status = form.status,
                )
            } else {
                repository.updateTask(
                    LearningTask(
                        id = taskId,
                        title = form.title.trim(),
                        topic = form.topic.trim(),
                        status = form.status,
                    )
                )
            }
            onCancelForm()
        }

    }

    fun onCancelForm() {
        formState.value = TaskFormState()
        isFormVisible.value = false
        editingTaskId.value = null
    }

    fun onEditTaskClick(learningTask: LearningTask) {
        editingTaskId.value = learningTask.id

        formState.value = TaskFormState(
            title = learningTask.title,
            topic = learningTask.topic,
            status = learningTask.status,
        )

        isFormVisible.value = true
    }

    fun onDeleteTaskClick(learningTask: LearningTask) {
        deletingTask.value = learningTask
    }

    fun onDeleteConfirm() {
        val task = deletingTask.value ?: return
        viewModelScope.launch {
            repository.deleteTask(task.id)
            deletingTask.value = null
        }
    }

    fun onDeleteDismiss() {
        deletingTask.value = null
    }

    fun onToggleTaskStatus(learningTask: LearningTask) {
        val nextStatus = when (learningTask.status) {
            TaskStatus.Doing -> TaskStatus.Done
            TaskStatus.Done -> TaskStatus.Todo
            TaskStatus.Todo -> TaskStatus.Doing
        }
        viewModelScope.launch {
            repository.updateTaskStatus(
                taskId = learningTask.id,
                status = nextStatus,
            )
        }
    }
}

sealed interface TasksUiState {
    data object Loading : TasksUiState

    data class Ready(
        val summary: TasksSummary,
        val tasks: List<LearningTask>,
        val form: TaskFormState,
        val editingTaskId: String? = null,
        val deletingTask: LearningTask? = null,
        val isFormVisible: Boolean = false,
    ) : TasksUiState
}