package com.xjf.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.xjf.devjourney.core.model.LearningTask
import com.xjf.devjourney.core.model.TaskStatus
import com.xjf.devjourney.core.model.task.TaskFormState
import com.xjf.devjourney.core.model.task.TasksSummary
import com.xjf.devjourney.core.ui.EmptySectionMessage
import com.xjf.devjourney.core.ui.LoadingContent

@Composable
fun TasksRoute(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    TasksScreen(
        uiState = uiState,
        onAddTaskClick = viewModel::onAddTaskClick,
        onTitleChange = viewModel::onTitleChange,
        onTopicChange = viewModel::onTopicChange,
        onStatusChange = viewModel::onStatusChange,
        onSaveTask = viewModel::onSaveTask,
        onCancelForm = viewModel::onCancelForm,
        onEditTaskClick = viewModel::onEditTaskClick,
        onDeleteTaskClick = viewModel::onDeleteTaskClick,
        onDeleteConfirm = viewModel::onDeleteConfirm,
        onDeleteDismiss = viewModel::onDeleteDismiss,
        onToggleTaskStatus = viewModel::onToggleTaskStatus,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    uiState: TasksUiState,
    modifier: Modifier,
    onAddTaskClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onTopicChange: (String) -> Unit,
    onStatusChange: (TaskStatus) -> Unit,
    onSaveTask: () -> Unit,
    onCancelForm: () -> Unit,
    onEditTaskClick: (LearningTask) -> Unit,
    onDeleteTaskClick: (LearningTask) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onToggleTaskStatus: (LearningTask) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = {
                Column {
                    Text("学习任务")
                    Text(
                        text = "规划、推进和复盘你的 Android 学习",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            })
        }
    ) { contentPadding ->
        when (uiState) {
            TasksUiState.Loading -> LoadingContent(contentPadding)
            is TasksUiState.Ready -> TasksContent(
                state = uiState,
                paddingValues = contentPadding,
                onAddTaskClick = onAddTaskClick,
                onTitleChange = onTitleChange,
                onTopicChange = onTopicChange,
                onStatusChange = onStatusChange,
                onSaveTask = onSaveTask,
                onCancelForm = onCancelForm,
                onEditTaskClick = onEditTaskClick,
                onDeleteTaskClick = onDeleteTaskClick,
                onDeleteConfirm = onDeleteConfirm,
                onDeleteDismiss = onDeleteDismiss,
                onToggleTaskStatus = onToggleTaskStatus,
            )
        }
    }
}


@Composable
fun TasksContent(
    state: TasksUiState.Ready,
    paddingValues: PaddingValues,
    onAddTaskClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onTopicChange: (String) -> Unit,
    onStatusChange: (TaskStatus) -> Unit,
    onSaveTask: () -> Unit,
    onCancelForm: () -> Unit,
    onEditTaskClick: (LearningTask) -> Unit,
    onDeleteTaskClick: (LearningTask) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onToggleTaskStatus: (LearningTask) -> Unit,
) {

    if (state.isFormVisible) {
        TaskForm(
            form = state.form,
            isEditing = state.editingTaskId != null,
            onTitleChange = onTitleChange,
            onTopicChange = onTopicChange,
            onSaveClick = onSaveTask,
            onDismiss = onCancelForm,
            onStatusChange = onStatusChange,
        )
    }

    if (state.deletingTask != null) {
        AlertDialog(
            onDismissRequest = onDeleteDismiss,
            title = {
                Text("删除任务")
            },
            text = {
                Text("确定要删除任务:${state.deletingTask.title}吗？")
            },
            confirmButton = {
                TextButton(onClick = onDeleteConfirm) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = onDeleteDismiss) {
                    Text("取消")
                }
            },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = paddingValues.calculateTopPadding() + 16.dp,
            end = 16.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            TasksSummarySection(state.summary)
        }

        item {
            Button(
                onClick = onAddTaskClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("新增任务")
            }
        }

        item {
            Text(
                text = "任务列表",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
        }

        item {
            TasksList(
                tasks = state.tasks,
                onEditTaskClick = onEditTaskClick,
                onDeleteTaskClick = onDeleteTaskClick,
                onToggleTaskStatus = onToggleTaskStatus
            )
        }

    }
}

@Composable
fun TasksList(
    tasks: List<LearningTask>,
    onEditTaskClick: (LearningTask) -> Unit,
    onDeleteTaskClick: (LearningTask) -> Unit,
    onToggleTaskStatus: (LearningTask) -> Unit,
) {
    if (tasks.isEmpty()) {
        EmptySectionMessage("还没有学习任务")
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tasks.forEach { task ->
                TaskRow(
                    task = task,
                    onEditClick = onEditTaskClick,
                    onDeleteClick = onDeleteTaskClick,
                    onToggleStatusClick = onToggleTaskStatus,
                )
            }
        }
    }
}

@Composable
fun TaskRow(
    task: LearningTask,
    onEditClick: (LearningTask) -> Unit,
    onDeleteClick: (LearningTask) -> Unit,
    onToggleStatusClick: (LearningTask) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(task.title, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        task.topic,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AssistChip(
                    onClick = { onToggleStatusClick(task) },
                    label = { Text(task.status.label) },
                )

                TextButton(onClick = { onEditClick(task) }) {
                    Text("编辑")
                }

                TextButton(onClick = { onDeleteClick(task) }) {
                    Text(
                        text = "删除",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
fun TaskForm(
    form: TaskFormState,
    isEditing: Boolean,
    onTitleChange: (String) -> Unit,
    onTopicChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDismiss: () -> Unit,
    onStatusChange: (TaskStatus) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditing) "编辑任务" else "新增任务")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = form.title,
                    onValueChange = onTitleChange,
                    label = { Text("任务标题") },
                    isError = form.titleError != null,
                    supportingText = {
                        form.titleError?.let { Text(it) }
                    },
                    singleLine = true,
                )

                OutlinedTextField(
                    value = form.topic,
                    onValueChange = onTopicChange,
                    label = { Text("所属主题") },
                    singleLine = true,
                )

                TaskStatusSelector(
                    selectedStatus = form.status,
                    onStatusSelected = onStatusChange,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSaveClick) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
fun TaskStatusSelector(
    selectedStatus: TaskStatus,
    onStatusSelected: (TaskStatus) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TaskStatus.entries.forEach { status ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
                label = { Text(status.label) },
            )
        }
    }
}

@Composable
fun TasksSummarySection(summary: TasksSummary) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "任务概览",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskSummaryItem(
                    label = "全部任务",
                    count = summary.totalCount,
                    modifier = Modifier.weight(1f)
                )

                TaskSummaryItem(
                    label = "待开始",
                    count = summary.todoCount,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TaskSummaryItem(
                    label = "进行中",
                    count = summary.doingCount,
                    modifier = Modifier.weight(1f),
                )
                TaskSummaryItem(
                    label = "已完成",
                    count = summary.doneCount,
                    modifier = Modifier.weight(1f),
                )
            }
        }

    }
}

@Composable
fun TaskSummaryItem(
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}