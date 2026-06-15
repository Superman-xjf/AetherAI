# Phase 1: Dashboard 首页完善

## 阶段目标

Phase 1 的目标是把当前 `feature:dashboard` 从“能展示假数据的首页”完善为“可读、可维护、可扩展的学习概览页”。

本阶段不引入 Room、不新增复杂导航、不做真实增删改。重点放在 Compose UI、UiState、ViewModel 数据整理和组件拆分上，为 Phase 2 的任务模块打基础。

## 目标用户

当前阶段的目标用户就是开发者本人，也就是这个 App 的学习者。

打开 App 后，用户应该可以快速知道：

- 当前有哪些学习主题。
- 每个主题的学习进度如何。
- 现在有哪些任务正在进行或待开始。
- 最近记录了哪些学习笔记。
- 整体学习状态大概处于什么水平。

## 功能范围

### 本阶段要做

- 修复 Dashboard 页面中文文案。
- 优化首页整体布局。
- 增加学习概览统计区域。
- 展示学习主题列表。
- 展示当前任务列表。
- 展示最近笔记列表。
- 为加载状态、空状态预留 UI。
- 将 Dashboard 页面拆分为更清晰的小组件。
- 调整 Preview 数据，方便在 Android Studio 中预览。

### 本阶段不做

- 不新增任务。
- 不编辑任务。
- 不删除任务。
- 不接入 Room。
- 不接入网络。
- 不接入 Navigation Compose。
- 不做复杂动画。
- 不引入新的 UI 设计系统。

## 页面信息结构

Dashboard 首页建议按以下顺序展示：

```text
TopAppBar
学习概览 Summary
学习路线 Topics
当前任务 Active Tasks
最近笔记 Recent Notes
```

## 页面需求

### 1. 顶部标题区

顶部标题区用于说明当前 App 和页面用途。

显示内容：

- 主标题：`DevJourney`
- 副标题：`现代 Android 学习工作台`

验收标准：

- 中文文案正常显示。
- 标题和副标题层级清晰。
- 副标题颜色弱于主标题。

### 2. 学习概览区域

学习概览区域用于快速展示整体学习状态。

建议展示指标：

- 学习主题数量
- 当前任务数量
- 已完成任务数量
- 整体完成进度

整体完成进度建议先基于任务计算：

```text
已完成任务数 / 总任务数
```

如果总任务数为 0，进度显示为 0。

验收标准：

- 用户打开首页后可以第一眼看到概览信息。
- 进度数字不会因为空数据崩溃。
- 指标名称简短清楚。

### 3. 学习路线区域

学习路线区域展示 `LearningTopic` 列表。

每个主题卡片显示：

- 主题名称
- 主题描述
- 已完成任务数 / 总任务数
- 进度条

验收标准：

- 列表使用 `LazyColumn`。
- 每个主题有稳定 key。
- 进度条能根据 `LearningTopic.progress` 正确展示。
- 文案和布局在手机宽度下不挤压。

### 4. 当前任务区域

当前任务区域展示未完成任务。

当前规则：

```text
status != Done
```

每个任务显示：

- 任务标题
- 所属主题
- 任务状态

任务状态显示建议：

| 状态 | 页面文案 |
| --- | --- |
| `Todo` | 待开始 |
| `Doing` | 进行中 |
| `Done` | 已完成 |

验收标准：

- 首页默认不展示已完成任务。
- 任务状态显示中文，而不是 enum 原始名称。
- 当前任务为空时展示空状态。

### 5. 最近笔记区域

最近笔记区域展示 `StudyNote` 列表。

每条笔记显示：

- 笔记标题
- 笔记摘要
- 标签

验收标准：

- 标签显示清楚。
- 笔记摘要不影响整体布局。
- 最近笔记为空时展示空状态。

### 6. 加载状态

当 `DashboardUiState.Loading` 时，页面显示加载内容。

验收标准：

- 页面居中显示加载指示器。
- 加载状态代码独立成组件。

### 7. 空状态

当某个列表为空时，展示轻量空状态。

建议文案：

- 学习路线为空：`还没有学习主题`
- 当前任务为空：`暂无进行中的任务`
- 最近笔记为空：`还没有学习笔记`

验收标准：

- 空状态不会让页面显得损坏。
- 每个空状态文案和所在区域匹配。

## UiState 需求

当前 `DashboardUiState.Ready` 可以继续使用，但建议增加首页统计数据，减少 UI 直接计算复杂逻辑。

建议结构：

```kotlin
data class DashboardSummary(
    val topicCount: Int,
    val totalTaskCount: Int,
    val completedTaskCount: Int,
    val activeTaskCount: Int,
    val overallProgress: Float,
)
```

然后在 `DashboardUiState.Ready` 中增加：

```kotlin
val summary: DashboardSummary
```

验收标准：

- 统计逻辑集中在 ViewModel 或独立 helper 中。
- Composable 尽量只负责展示。
- 空任务列表不会导致除零问题。

## 组件拆分建议

建议将 `DashboardRoute.kt` 中的 UI 拆成以下组件：

```text
DashboardRoute
DashboardScreen
LoadingContent
DashboardContent
SummarySection
SummaryItem
SectionTitle
TopicCard
TaskRow
NoteCard
EmptySectionMessage
```

如果文件变得过长，后续可以再拆文件。本阶段可以先保持在一个文件内，方便学习和阅读。

## 学习任务拆分

### Task 1：读懂当前 Dashboard 数据流

要理解：

- `DashboardRoute` 如何获取 ViewModel。
- `DashboardViewModel` 如何组合 repository 的三个 Flow。
- `DashboardUiState` 如何驱动页面。

完成标准：

- 能用自己的话说明 `Repository -> ViewModel -> UiState -> Compose` 的流程。

### Task 2：修复中文文案

要处理：

- 首页标题文案。
- Section 标题文案。
- Fake 数据中的中文内容。
- Preview 数据中的中文内容。

完成标准：

- App 页面和 Preview 中没有乱码。

### Task 3：新增 Summary 数据模型

要处理：

- 新增 `DashboardSummary`。
- 在 ViewModel 中计算统计信息。
- 将 summary 放入 `DashboardUiState.Ready`。

完成标准：

- Summary 能拿到主题数、任务数、已完成数、当前任务数和总进度。

### Task 4：实现 SummarySection

要处理：

- 新增概览区域 UI。
- 用 2x2 或横向列表展示统计项。
- 显示整体进度。

完成标准：

- 首页顶部能清楚看到学习概览。

### Task 5：优化任务状态展示

要处理：

- 为 `TaskStatus` 增加 UI 文案转换。
- Dashboard 不再直接显示 `task.status.name`。

完成标准：

- `Todo` 显示为 `待开始`。
- `Doing` 显示为 `进行中`。
- `Done` 显示为 `已完成`。

### Task 6：补充空状态

要处理：

- 学习路线为空。
- 当前任务为空。
- 最近笔记为空。

完成标准：

- 任一列表为空时页面仍然自然、清楚。

### Task 7：整理 Preview

要处理：

- Preview 数据使用正常中文。
- Preview 覆盖正常数据。
- 可选：增加空状态 Preview。

完成标准：

- Android Studio Preview 能帮助你快速看 UI。

## 技术验收清单

Phase 1 完成时应满足：

- `DashboardRoute` 仍然通过 Hilt 获取 `DashboardViewModel`。
- `DashboardViewModel` 仍然只依赖 `DevJourneyRepository`。
- `DashboardUiState` 可以表达加载状态和就绪状态。
- Dashboard 页面中文文案正常。
- 首页有学习概览统计。
- 任务状态显示中文。
- 三个列表都有空状态。
- Composable 拆分后职责清晰。
- 项目可以成功构建。

## 建议验证命令

```powershell
$env:JAVA_HOME='C:\Users\Jeffery\.jdks\jbr-17.0.14'
.\gradlew.bat :app:assembleDebug
```

如果只想先跑较轻量的检查，可以先运行：

```powershell
$env:JAVA_HOME='C:\Users\Jeffery\.jdks\jbr-17.0.14'
.\gradlew.bat :feature:dashboard:compileDebugKotlin
```

## 推荐学习顺序

建议按这个节奏学习和开发：

1. 先读 `DashboardRoute.kt` 和 `DashboardViewModel.kt`。
2. 再读 `DevJourneyRepository` 和 `FakeDevJourneyRepository`。
3. 先修中文文案，不改结构。
4. 再新增 `DashboardSummary`。
5. 然后实现 Summary UI。
6. 最后整理空状态和 Preview。

每完成一个小任务就运行一次构建或至少打开 Preview 看一下效果。这样学习反馈会更快，也更容易知道自己是哪一步改坏了。
