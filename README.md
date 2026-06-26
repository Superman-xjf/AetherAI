# DevJourney

DevJourney 是一个用于练习现代 Android 技术栈的个人学习工作台。项目目标不是一次性做完一个复杂 App，而是围绕一个真实的小产品，循序渐进地学习 Compose、多模块架构、Hilt、Room、网络、后台任务、测试和后续 AI 能力。

当前版本已经搭好可运行的多模块骨架，并接入 Hilt 作为依赖注入方案。Phase 1 已完成 Dashboard 首页完善，Phase 2 已完成任务模块。现阶段数据仍来自内存中的 `FakeDevJourneyRepository`。Phase 3 的重点是接入 Room，把任务数据从“内存模拟写入”推进到“本地持久化存储”。

## 项目定位

DevJourney 会逐步发展为一个个人学习管理工具，核心能力包括：

- 管理学习主题，例如 Kotlin、Compose、Android 架构、网络和测试。
- 管理学习任务，例如待开始、进行中、已完成。
- 记录学习笔记，并支持标签、搜索和复盘。
- 后续接入网络资源、提醒任务和 AI 学习助手。

## 当前模块结构

```text
DevJourney
├── app
├── core
│   ├── data
│   ├── model
│   └── ui
└── feature
    ├── dashboard
    └── tasks
```

| 模块 | 职责 |
| --- | --- |
| `app` | 应用入口、主题包装、启动首屏、`Application`、Hilt 初始化 |
| `core:model` | 领域模型，例如 `LearningTopic`、`LearningTask`、`StudyNote` |
| `core:data` | Repository 抽象、当前内存数据源、Hilt 数据绑定 |
| `core:ui` | 跨 feature 复用的轻量 UI 组件，例如加载和空状态 |
| `feature:dashboard` | 首页 UI、首页 `UiState`、`DashboardViewModel` |
| `feature:tasks` | 任务列表、任务表单、任务状态切换、`TasksViewModel` |

## 当前架构

当前项目采用一个简化版的分层结构：

```text
Compose UI
    ↓
Hilt ViewModel
    ↓
Repository
    ↓
Fake Data Source
```

对应到当前代码：

```text
feature:dashboard
    DashboardRoute / DashboardScreen
    DashboardViewModel

core:data
    DevJourneyRepository
    FakeDevJourneyRepository
    DataModule

core:model
    LearningTopic
    LearningTask
    StudyNote

feature:tasks
    TasksRoute / TasksScreen
    TasksViewModel
```

`DashboardViewModel` 依赖 `DevJourneyRepository` 抽象，而不是直接依赖具体数据源。这样后续把 `FakeDevJourneyRepository` 替换成 Room、网络或混合数据源时，首页和 ViewModel 的改动会更小。

## 当前数据流

```text
FakeDevJourneyRepository
    -> Flow<List<LearningTopic>>
    -> Flow<List<LearningTask>>
    -> Flow<List<StudyNote>>
        ↓
DashboardViewModel.combine(...)
        ↓
DashboardUiState
        ↓
DashboardScreen
```

这个数据流适合用来练习现代 Android 的几个核心概念：

- 用 `Flow` 暴露持续变化的数据。
- 用 `ViewModel` 把业务数据整理成 UI 需要的状态。
- 用 Compose 根据 `UiState` 声明式渲染页面。
- 用 Hilt 解耦接口和实现。

## 推荐学习路线

### 阶段 0：整理项目地基

目标：让项目结构、文案和构建方式清晰可靠。

- 修复中文文案和 README。
- 确认项目可以执行 `:app:assembleDebug`。
- 理解 `settings.gradle.kts` 中的模块声明。
- 理解 `gradle/libs.versions.toml` 中的依赖版本管理。

学习重点：

- Android 多模块项目结构
- Gradle Version Catalog
- Kotlin DSL
- Hilt 基础配置

### 阶段 1：完善 Dashboard 首页

状态：已完成。

目标：把当前首页变成一个真正可用的学习概览页。

- 优化首页布局和中文文案。
- 显示学习主题、当前任务、最近笔记。
- 增加学习进度、任务数量等统计信息。
- 为加载状态和空状态预留 UI。
- 把较大的 Composable 拆成更小的 UI 组件。

学习重点：

- Compose 基础布局
- `LazyColumn`
- Material 3 组件
- `StateFlow` 与 `collectAsState`
- UI State 设计

### 阶段 2：新增任务模块 `feature:tasks`

状态：已完成。

目标：完成第一个完整业务功能，让用户可以查看、创建、编辑、删除任务，并切换任务状态。

- 新增 `feature:tasks` 模块。
- 实现任务列表页。
- 实现新增任务表单。
- 实现编辑任务表单。
- 实现删除任务确认。
- 实现任务状态切换，例如 `Todo -> Doing -> Done`。
- 扩展 `DevJourneyRepository` 的任务操作 API，让 Dashboard 和 Tasks 共享同一份任务数据。
- 在 `FakeDevJourneyRepository` 中先用内存可变数据模拟真实写入，为后续 Room 铺路。

学习重点：

- Feature 模块拆分
- Compose 表单
- ViewModel 事件处理
- 单向数据流
- Repository 接口设计
- 跨页面共享状态
- 轻量级错误处理与输入校验

### 阶段 3：接入 Room 本地数据库

状态：下一阶段。

目标：把内存数据替换为本地持久化数据。

- 新增 Room 依赖。
- 新增数据库相关模块，建议命名为 `core:database`。
- 先为任务创建 Entity、Dao 和 Database。
- 用 Room 实现任务的本地存储。
- 在 `core:data` 中组合数据库数据源。
- 保持上层 Repository API 尽量稳定。
- 后续再把主题和笔记迁入 Room。

建议结构：

```text
core
├── model
├── database
└── data
```

学习重点：

- Room Entity
- Dao 查询
- Flow + Room
- 数据库迁移
- Entity 与领域模型转换

### 阶段 4：新增笔记模块 `feature:notes`

目标：增加学习记录能力。

- 新增 `feature:notes` 模块。
- 实现笔记列表。
- 实现新增和编辑笔记。
- 支持标签展示。
- 支持关键词搜索。
- 首页展示最近笔记。

学习重点：

- 文本输入
- 搜索状态管理
- 列表过滤
- 页面与 Repository 协作

### 阶段 5：加入应用导航

目标：让多个功能模块可以自然切换。

- 接入 Navigation Compose。
- 增加首页、任务、笔记等页面路由。
- 使用底部导航栏或其他主导航结构。
- 支持任务详情、笔记详情等页面传参。

学习重点：

- Navigation Compose
- 路由定义
- 页面参数
- 多 feature 模块协作

### 阶段 6：补充测试

目标：让核心逻辑可验证、可放心重构。

- 为 Repository 写单元测试。
- 为 ViewModel 写单元测试。
- 为 Room Dao 写测试。
- 为关键 Compose 页面补少量 UI 测试。

学习重点：

- JUnit
- Coroutine Test
- Flow 测试
- ViewModel 测试
- Android 测试分层

### 阶段 7：接入网络能力

目标：从纯本地学习工具扩展到资源发现工具。

- 新增 `core:network` 模块。
- 接入 GitHub Repository Search API。
- 新增学习资源列表或搜索页。
- 支持收藏远程资源。
- 后续可把收藏结果缓存到 Room。

学习重点：

- Retrofit 或 Ktor Client
- OkHttp
- JSON 解析
- DTO 与领域模型转换
- 网络错误处理

### 阶段 8：加入后台任务和提醒

目标：让 App 具备日常使用价值。

- 接入 WorkManager。
- 增加每日学习提醒。
- 增加每周复盘提醒。
- 根据未完成任务生成提醒内容。

学习重点：

- WorkManager
- Notification
- 后台任务限制
- Android 生命周期与系统限制

### 阶段 9：扩展 AI 学习助手

目标：基于已有任务和笔记提供辅助学习能力。

- 根据笔记生成复习问题。
- 总结一周学习内容。
- 根据任务完成情况生成下一步建议。
- 把学习卡片、复盘摘要保存回本地数据库。

学习重点：

- API 调用
- Prompt 设计
- 本地数据与远程能力结合
- 隐私和成本控制

## 推荐开发顺序

建议按下面顺序推进：

1. 整理 README 和中文文案。已完成。
2. 完善 Dashboard 首页。已完成。
3. 新增 `feature:tasks`。已完成。
4. 接入 Room。下一步。
5. 新增 `feature:notes`。
6. 加入 Navigation Compose。
7. 补充 Repository、ViewModel 和 Dao 测试。
8. 新增 `core:network` 并接入资源搜索。
9. 加入 WorkManager、提醒和 AI 能力。

## 阶段文档

- [Phase 1: Dashboard 首页完善](docs/phase-1-dashboard.md)
- [Phase 2: Tasks 任务模块](docs/phase-2-tasks.md)
- [Phase 3: Room 本地数据库](docs/phase-3-room.md)

## 本地构建

当前项目使用本机 JDK 17 构建：

```powershell
$env:JAVA_HOME='C:\Users\Jeffery\.jdks\jbr-17.0.14'
.\gradlew.bat :app:assembleDebug
```

如果 Gradle wrapper 正在下载 `gradle-8.14.3` 时被中断，可能会短暂出现 zip 文件锁。等待残留 wrapper 进程退出后重新执行即可。
