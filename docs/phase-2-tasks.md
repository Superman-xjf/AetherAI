# Phase 2: Tasks 任务模块

## 阶段目标

Phase 2 的目标是新增 `feature:tasks`，完成 DevJourney 的第一个可操作业务功能。

Phase 1 已经让 Dashboard 能展示学习主题、当前任务、最近笔记和学习概览。Phase 2 要在这个基础上补齐任务管理能力：用户可以进入任务页，查看全部任务，按状态理解当前进展，并完成新增、编辑、删除和状态切换。

本阶段仍然不接入 Room，不引入复杂导航栈，不接入网络。数据继续保存在 `FakeDevJourneyRepository` 的内存状态中，但 Repository API 要提前设计成接近真实数据源的形态，为 Phase 3 接入 Room 降低改动成本。

## 目标用户

当前阶段的目标用户仍然是开发者本人，也就是这个 App 的学习者。

打开任务页后，用户应该可以快速完成：

- 看见所有学习任务。
- 区分待开始、进行中、已完成任务。
- 新增一个任务并选择所属主题。
- 修改任务标题、主题和状态。
- 删除不再需要的任务。
- 从任务页修改状态后，Dashboard 的当前任务和学习概览能同步变化。

## 功能范围

### 本阶段要做

- 新增 `feature:tasks` 模块。
- 在 `settings.gradle.kts` 中声明 `:feature:tasks`。
- 为任务模块配置 Compose、Hilt、KSP 和必要依赖。
- 扩展 `DevJourneyRepository`，增加任务写操作 API。
- 将 `FakeDevJourneyRepository` 的任务数据改成可变的 `MutableStateFlow`。
- 实现 `TasksViewModel`，负责加载任务、处理表单事件和调用 Repository。
- 实现任务列表 UI。
- 实现新增任务 UI。
- 实现编辑任务 UI。
- 实现删除任务确认。
- 实现任务状态切换。
- 在 `app` 中临时接入任务入口，保证可以从 App 里打开或验证任务页。
- 保持 Dashboard 继续从同一个 Repository 读取任务数据。

### 本阶段不做

- 不接入 Room。
- 不做真实持久化。
- 不接入网络。
- 不实现笔记模块。
- 不实现复杂多级导航。
- 不做账号体系。
- 不做复杂排序、筛选和搜索。
- 不引入新的 UI 设计系统。

## 推荐模块结构

新增模块后，推荐结构如下：

```text
DevJourney
├── app
├── core
│   ├── data
│   └── model
└── feature
    ├── dashboard
    └── tasks
```

`feature:tasks` 建议先保持简单：

```text
feature/tasks
└── src/main/java/com/xjf/devjourney/feature/tasks
    ├── TasksRoute.kt
    └── TasksViewModel.kt
```

如果 `TasksRoute.kt` 变得太长，后续再拆成 `TaskList.kt`、`TaskForm.kt` 等文件。本阶段优先练清楚数据流和事件处理。

## Repository 需求

当前 `DevJourneyRepository` 只有只读数据流：

```kotlin
val topics: Flow<List<LearningTopic>>
val tasks: Flow<List<LearningTask>>
val notes: Flow<List<StudyNote>>
```

Phase 2 建议增加任务写操作：

```kotlin
suspend fun addTask(title: String, topic: String, status: TaskStatus = TaskStatus.Todo)
suspend fun updateTask(task: LearningTask)
suspend fun deleteTask(taskId: String)
suspend fun updateTaskStatus(taskId: String, status: TaskStatus)
```

验收标准：

- Dashboard 和 Tasks 使用同一个 `tasks` Flow。
- 在任务页新增、编辑、删除、切换状态后，Dashboard 数据能自动刷新。
- Repository 接口不暴露 `MutableStateFlow`。
- 写操作用 `suspend fun`，方便 Phase 3 替换为 Room Dao。

## 页面信息结构

任务页建议按以下顺序展示：

```text
TopAppBar
任务概览 Summary
新增任务入口
任务列表 Tasks
新增/编辑任务表单
删除确认
```

## 页面需求

### 1. 顶部标题区

显示内容：

- 主标题：`学习任务`
- 副标题：`规划、推进和复盘你的 Android 学习`

验收标准：

- 中文文案正常显示。
- 标题和 Dashboard 的视觉层级保持一致。
- 页面入口清楚，不需要用户猜当前在做什么。

### 2. 任务概览区域

建议展示指标：

- 全部任务数量
- 待开始任务数量
- 进行中任务数量
- 已完成任务数量

验收标准：

- 数量由 `TasksViewModel` 计算，Composable 只负责展示。
- 空任务列表时数量全部为 0。
- 状态文案使用 `TaskStatus.label`。

### 3. 任务列表

每个任务显示：

- 任务标题
- 所属主题
- 当前状态
- 状态切换操作
- 编辑入口
- 删除入口

状态切换建议先做成单步推进：

```text
待开始 -> 进行中 -> 已完成 -> 待开始
```

验收标准：

- 列表使用 `LazyColumn`。
- 每个任务使用稳定 key：`task.id`。
- 点击状态切换后，列表和 Dashboard 都更新。
- 已完成任务不从任务页隐藏。
- 空列表时展示轻量空状态：`还没有学习任务`。

### 4. 新增任务

新增任务表单建议包含：

- 任务标题输入框。
- 所属主题输入或选择。
- 初始状态选择，默认 `Todo`。
- 保存按钮。
- 取消按钮。

验收标准：

- 标题不能为空。
- 标题为空时不调用 Repository，并展示输入错误。
- 保存成功后清空表单并回到列表。
- 新增任务默认能被 Dashboard 读取。

### 5. 编辑任务

编辑任务表单建议复用新增任务表单。

验收标准：

- 点击编辑后，表单填入当前任务数据。
- 修改标题、主题、状态后保存。
- 保存后列表显示最新内容。
- 取消编辑不修改原任务。

### 6. 删除任务

删除建议使用确认对话框。

显示内容：

- 标题：`删除任务`
- 内容：`确定要删除这个任务吗？`
- 确认按钮：`删除`
- 取消按钮：`取消`

验收标准：

- 点击删除入口不会立刻删除。
- 用户确认后才调用 Repository。
- 删除后 Dashboard 同步刷新。

## UiState 需求

建议为任务页定义独立状态：

```kotlin
data class TasksUiState(
    val summary: TasksSummary,
    val tasks: List<LearningTask>,
    val form: TaskFormState,
    val editingTaskId: String? = null,
    val deletingTask: LearningTask? = null,
    val isFormVisible: Boolean = false,
)

data class TasksSummary(
    val totalCount: Int,
    val todoCount: Int,
    val doingCount: Int,
    val doneCount: Int,
)

data class TaskFormState(
    val title: String = "",
    val topic: String = "",
    val status: TaskStatus = TaskStatus.Todo,
    val titleError: String? = null,
)
```

验收标准：

- `TasksViewModel` 暴露 `StateFlow<TasksUiState>`。
- UI 事件通过 ViewModel 方法进入，例如 `onTitleChange`、`onSaveTask`、`onDeleteConfirmed`。
- Composable 不直接修改 Repository。
- 表单状态和列表状态在一个 UiState 中可追踪。

## 组件拆分建议

建议先拆成这些 Composable：

```text
TasksRoute
TasksScreen
TasksContent
TasksSummarySection
TaskSummaryItem
TaskList
TaskRow
TaskForm
TaskStatusSelector
DeleteTaskDialog
EmptyTasksMessage
```

## 学习任务拆分

### Task 1：创建 `feature:tasks` 模块

要处理：

- 新增模块目录和 `build.gradle.kts`。
- 在 `settings.gradle.kts` 中 include。
- 配置 namespace：`com.xjf.devjourney.feature.tasks`。
- 添加 `core:data`、`core:model`、Compose、Hilt、ViewModel 依赖。

完成标准：

- `.\gradlew.bat :feature:tasks:compileDebugKotlin` 可以执行到任务模块。

### Task 2：扩展 Repository 写操作

要处理：

- 给 `DevJourneyRepository` 增加任务写 API。
- 改造 `FakeDevJourneyRepository`，让任务列表由私有 `MutableStateFlow` 持有。
- 实现新增、编辑、删除、状态更新。

完成标准：

- Dashboard 仍然能读取任务。
- 写操作不会破坏现有 Flow。

### Task 3：实现 `TasksViewModel`

要处理：

- 订阅 `repository.tasks`。
- 计算 `TasksSummary`。
- 管理新增和编辑表单状态。
- 管理删除确认状态。
- 将保存、删除、状态切换转成 Repository 调用。

完成标准：

- ViewModel 不依赖具体 `FakeDevJourneyRepository`。
- 输入校验集中在 ViewModel。

### Task 4：实现任务列表 UI

要处理：

- 新增 `TasksRoute` 和 `TasksScreen`。
- 展示任务概览。
- 展示任务列表和空状态。
- 每个任务提供状态切换、编辑、删除入口。

完成标准：

- 正常数据、空数据都能显示。
- 状态文案是中文。

### Task 5：实现新增和编辑表单

要处理：

- 表单字段绑定到 `TaskFormState`。
- 保存时调用 ViewModel。
- 取消时清理表单状态。
- 编辑时复用同一套表单。

完成标准：

- 新增任务后列表增加一项。
- 编辑任务后列表内容更新。
- 标题为空时展示错误。

### Task 6：实现删除确认

要处理：

- 点击删除入口时设置 `deletingTask`。
- 展示确认对话框。
- 确认后删除。
- 取消后关闭对话框。

完成标准：

- 删除行为可撤销到确认前。
- 删除后 Dashboard 同步变化。

### Task 7：临时接入 App 验证

要处理：

- 在 `app` 中添加 `feature:tasks` 依赖。
- 选择一个简单入口验证任务页，例如临时把首屏切到 `TasksRoute`，或在主界面提供一个切换入口。

完成标准：

- 可以在 App 中看到任务页。
- 后续 Phase 5 接入 Navigation Compose 时，可以替换临时入口。

## 技术验收清单

Phase 2 完成时应满足：

- 项目包含 `feature:tasks` 模块。
- `DevJourneyRepository` 支持任务增删改和状态切换。
- `FakeDevJourneyRepository` 使用内存状态模拟写入。
- `TasksViewModel` 只依赖 `DevJourneyRepository`。
- 任务页能展示全部任务。
- 任务页能新增任务。
- 任务页能编辑任务。
- 任务页能删除任务。
- 任务页能切换任务状态。
- Dashboard 能同步反映任务变化。
- 空状态、输入错误、删除确认都有处理。
- 项目可以成功构建。

## 建议验证命令

```powershell
$env:JAVA_HOME='C:\Users\Jeffery\.jdks\jbr-17.0.14'
.\gradlew.bat :feature:tasks:compileDebugKotlin
```

完整验证：

```powershell
$env:JAVA_HOME='C:\Users\Jeffery\.jdks\jbr-17.0.14'
.\gradlew.bat :app:assembleDebug
```

## 推荐学习顺序

建议按这个节奏学习和开发：

1. 先读 `LearningTask`、`TaskStatus` 和当前 `DevJourneyRepository`。
2. 再读 `DashboardViewModel`，理解 Dashboard 如何消费任务 Flow。
3. 新增 `feature:tasks` 空模块并保证能编译。
4. 扩展 Repository 写操作。
5. 实现 `TasksViewModel`，先不写复杂 UI。
6. 实现任务列表。
7. 实现新增和编辑表单。
8. 实现删除确认和状态切换。
9. 最后把任务页接入 App 做端到端验证。

每完成一个小任务就跑一次对应模块构建。Phase 2 的关键不是 UI 有多复杂，而是把“用户事件 -> ViewModel -> Repository -> Flow -> UI 刷新”这条链路练扎实。
