# Phase 3: Room 本地数据库

## 阶段目标

Phase 3 的目标是接入 Room，把 Phase 2 中保存在内存里的任务数据替换成本地持久化数据。

Phase 2 已经完成了任务列表、新增、编辑、删除和状态切换，但这些数据仍然来自 `FakeDevJourneyRepository` 的 `MutableStateFlow`。应用进程结束后，新增和修改的任务都会丢失。Phase 3 要把任务数据落到本地数据库里，同时尽量保持 `DevJourneyRepository` 对上层的 API 稳定，让 Dashboard 和 Tasks 页面不需要大改。

本阶段先聚焦任务数据，不急着把主题和笔记全部迁入 Room。主题和笔记可以继续使用内存假数据，等你理解 Room 的 Entity、Dao、Database、Flow 查询和数据映射后，再在后续阶段扩展。

## 目标用户

当前阶段的目标用户仍然是开发者本人，也就是这个 App 的学习者。

完成 Phase 3 后，用户应该可以：

- 新增任务后关闭并重新打开 App，任务仍然存在。
- 编辑任务后重新进入页面，修改后的内容仍然存在。
- 删除任务后重新打开 App，被删除的任务不会回来。
- 切换任务状态后，Dashboard 和 Tasks 页面继续同步刷新。

## 功能范围

### 本阶段要做

- 新增 Room 依赖和 KSP 编译器配置。
- 新增 `core:database` 模块。
- 在 `settings.gradle.kts` 中声明 `:core:database`。
- 创建 `TaskEntity`。
- 创建 `TaskDao`。
- 创建 `DevJourneyDatabase`。
- 创建数据库 Hilt Module，提供 Database 和 Dao。
- 在 `core:data` 中依赖 `core:database`。
- 将 `DevJourneyRepository` 的任务读写改为基于 Room。
- 保持 `feature:dashboard` 和 `feature:tasks` 继续依赖 `DevJourneyRepository`，不直接依赖 Room。
- 为首次启动准备初始任务数据，避免数据库为空时页面没有学习样例。
- 验证新增、编辑、删除、状态切换在重启 App 后仍然保留。

### 本阶段不做

- 不接入网络。
- 不新增笔记模块。
- 不做复杂数据库迁移策略。
- 不实现多表关系。
- 不做分页。
- 不做任务搜索和排序。
- 不让 UI 层直接访问 Dao。
- 不急着把 `LearningTopic` 和 `StudyNote` 全部迁入 Room。

## 推荐模块结构

新增 `core:database` 后，推荐结构如下：

```text
DevJourney
├── app
├── core
│   ├── data
│   ├── database
│   ├── model
│   └── ui
└── feature
    ├── dashboard
    └── tasks
```

`core:database` 建议结构：

```text
core/database
└── src/main/java/com/xjf/devjourney/core/database
    ├── DevJourneyDatabase.kt
    ├── dao
    │   └── TaskDao.kt
    ├── di
    │   └── DatabaseModule.kt
    ├── model
    │   └── TaskEntity.kt
    └── converter
        └── TaskStatusConverter.kt
```

`core:data` 继续作为 Repository 层：

```text
core/data
└── src/main/java/com/xjf/devjourney/core/data
    ├── DevJourneyRepository.kt
    ├── OfflineDevJourneyRepository.kt
    ├── FakeDevJourneyRepository.kt
    └── di
        └── DataModule.kt
```

其中 `FakeDevJourneyRepository` 可以暂时保留作为学习参考，但 Hilt 绑定应该切到 `OfflineDevJourneyRepository`。

## 数据流目标

Phase 2 当前数据流是：

```text
FakeDevJourneyRepository
    -> MutableStateFlow<List<LearningTask>>
    -> DevJourneyRepository.tasks
    -> DashboardViewModel / TasksViewModel
    -> Compose UI
```

Phase 3 目标数据流是：

```text
Room TaskDao
    -> Flow<List<TaskEntity>>
    -> OfflineDevJourneyRepository 映射为 List<LearningTask>
    -> DevJourneyRepository.tasks
    -> DashboardViewModel / TasksViewModel
    -> Compose UI
```

写操作目标：

```text
TasksViewModel
    -> DevJourneyRepository.addTask / updateTask / deleteTask / updateTaskStatus
    -> OfflineDevJourneyRepository
    -> TaskDao
    -> Room 更新数据库
    -> Flow 自动发出新列表
    -> UI 自动刷新
```

## 依赖需求

在 `gradle/libs.versions.toml` 中新增 Room 相关版本和库。

建议新增类似条目：

```toml
[versions]
room = "选择当前项目可用的稳定版本"

[libraries]
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
```

`core:database` 依赖建议：

```kotlin
implementation(project(":core:model"))
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
ksp(libs.androidx.room.compiler)
implementation(libs.hilt.android)
ksp(libs.hilt.compiler)
```

如果 Room 版本和 Kotlin/KSP 版本不匹配，优先以 Android Studio 或官方文档提示为准。

## 数据模型设计

### TaskEntity

数据库层建议不要直接复用 `LearningTask`，而是新增 `TaskEntity`：

```kotlin
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val topic: String,
    val status: TaskStatus,
)
```

这里可以先用 `TaskStatus` + TypeConverter，也可以把 `status` 存成 `String`。学习 Room 时更推荐先练 TypeConverter。

### TaskStatusConverter

如果 Entity 里直接使用 `TaskStatus`，需要 TypeConverter：

```kotlin
class TaskStatusConverter {
    @TypeConverter
    fun fromStatus(status: TaskStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): TaskStatus = TaskStatus.valueOf(value)
}
```

验收标准：

- 数据库中可以保存任务状态。
- 从数据库读出后能恢复为 `TaskStatus`。
- 状态切换后重新打开 App，状态不丢失。

## Dao 需求

`TaskDao` 建议包含：

```kotlin
@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("UPDATE tasks SET status = :status WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun taskCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTasks(tasks: List<TaskEntity>)
}
```

如果 `status` 最终存成 `String`，则 `updateTaskStatus` 的参数也改成 `String`。

验收标准：

- `observeTasks()` 返回 `Flow`。
- 新增和编辑都可以通过 upsert 完成。
- 删除支持按 id 删除。
- 状态更新不需要先读出整条任务。

## Database 需求

`DevJourneyDatabase` 建议：

```kotlin
@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(TaskStatusConverter::class)
abstract class DevJourneyDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
```

验收标准：

- Database 位于 `core:database`。
- Dao 由 Database 暴露。
- version 从 1 开始。
- 本阶段不做复杂迁移。

## Hilt 注入需求

`core:database` 中提供：

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): DevJourneyDatabase {
        return Room.databaseBuilder(
            context,
            DevJourneyDatabase::class.java,
            "devjourney.db",
        ).build()
    }

    @Provides
    fun provideTaskDao(database: DevJourneyDatabase): TaskDao {
        return database.taskDao()
    }
}
```

验收标准：

- `OfflineDevJourneyRepository` 可以通过构造函数注入 `TaskDao`。
- App 启动后 Hilt 能正常构建依赖图。
- 不在 ViewModel 或 Composable 中创建数据库。

## Repository 需求

新增或重命名 Repository 实现：

```kotlin
@Singleton
class OfflineDevJourneyRepository @Inject constructor(
    private val taskDao: TaskDao,
) : DevJourneyRepository {
    override val tasks: Flow<List<LearningTask>> =
        taskDao.observeTasks().map { entities ->
            entities.map { it.asExternalModel() }
        }

    override suspend fun addTask(title: String, topic: String, status: TaskStatus) {
        taskDao.upsertTask(
            TaskEntity(
                id = "task-${System.currentTimeMillis()}",
                title = title,
                topic = topic,
                status = status,
            )
        )
    }

    override suspend fun updateTask(task: LearningTask) {
        taskDao.upsertTask(task.asEntity())
    }

    override suspend fun deleteTask(taskId: String) {
        taskDao.deleteTaskById(taskId)
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus) {
        taskDao.updateTaskStatus(taskId, status)
    }
}
```

建议新增映射函数：

```kotlin
fun TaskEntity.asExternalModel(): LearningTask
fun LearningTask.asEntity(): TaskEntity
```

验收标准：

- `feature:dashboard` 和 `feature:tasks` 不知道 Room 的存在。
- `DevJourneyRepository` API 尽量不改。
- UI 层不用修改任务读写逻辑。

## 初始数据策略

Room 数据库首次创建时是空的。为了保留学习样例，本阶段建议在 Repository 初始化或 App 首次启动时插入默认任务。

简单方案：

```kotlin
suspend fun seedTasksIfEmpty()
```

在 Repository 或单独的 bootstrap 类中：

```kotlin
if (taskDao.taskCount() == 0) {
    taskDao.insertTasks(defaultTasks)
}
```

验收标准：

- 首次安装后能看到默认任务。
- 用户新增或删除任务后，不会每次启动都重新插入默认任务。

学习阶段可以先在 `OfflineDevJourneyRepository` 的 `init` 中用协程处理，但更稳妥的方式是后续引入明确的初始化流程。本阶段重点是理解 Room，不必过早复杂化。

## 学习任务拆分

### Task 1：新增 Room 依赖

要处理：

- 在 `libs.versions.toml` 中新增 Room 版本和库。
- 新增 `core:database` 模块。
- 配置 Android Library、Kotlin、KSP、Hilt。
- 在 `settings.gradle.kts` include `:core:database`。

完成标准：

- `.\gradlew.bat :core:database:compileDebugKotlin` 可以执行。

### Task 2：创建 Entity 和 Converter

要处理：

- 创建 `TaskEntity`。
- 创建 `TaskStatusConverter`。
- 确认 `TaskStatus` 可以被 Room 保存和读取。

完成标准：

- Entity 编译通过。
- Converter 被 Database 引用。

### Task 3：创建 Dao

要处理：

- 创建 `TaskDao`。
- 实现观察任务列表、新增/更新、删除、状态更新。
- 查询返回 `Flow<List<TaskEntity>>`。

完成标准：

- Dao 编译通过。
- 查询语句字段名和 Entity 字段一致。

### Task 4：创建 Database 和 Hilt Module

要处理：

- 创建 `DevJourneyDatabase`。
- 创建 `DatabaseModule`。
- 提供 Database 和 TaskDao。

完成标准：

- Hilt 可以注入 `TaskDao`。
- App 构建通过。

### Task 5：新增数据库 Repository 实现

要处理：

- 新增 `OfflineDevJourneyRepository`。
- 实现 Entity 与领域模型转换。
- 将任务读写委托给 `TaskDao`。
- 暂时保留 topics 和 notes 的内存假数据，避免扩大范围。

完成标准：

- `DevJourneyRepository.tasks` 来自 Room。
- `addTask`、`updateTask`、`deleteTask`、`updateTaskStatus` 都写入数据库。

### Task 6：切换 Hilt 绑定

要处理：

- 修改 `DataModule`。
- 将 `DevJourneyRepository` 绑定到 `OfflineDevJourneyRepository`。
- 确认 UI 层无需改动。

完成标准：

- App 启动使用 Room 数据。
- `FakeDevJourneyRepository` 不再是默认注入实现。

### Task 7：处理初始数据

要处理：

- 准备默认任务列表。
- 数据库为空时插入默认任务。
- 避免每次启动重复插入。

完成标准：

- 首次安装能看到默认任务。
- 用户修改后的数据不会被默认数据覆盖。

### Task 8：端到端验证

要处理：

- 新增任务。
- 编辑任务。
- 删除任务。
- 切换任务状态。
- 关闭并重新打开 App。
- 确认 Dashboard 和 Tasks 仍然同步。

完成标准：

- 任务数据重启后仍存在。
- 构建成功。
- 没有明显崩溃或空数据异常。

## 技术验收清单

Phase 3 完成时应满足：

- 项目包含 `core:database` 模块。
- `core:database` 包含 `TaskEntity`、`TaskDao`、`DevJourneyDatabase`。
- Room 依赖通过 Version Catalog 管理。
- Hilt 可以提供 Database 和 Dao。
- `core:data` 通过 `OfflineDevJourneyRepository` 使用 Room。
- `DevJourneyRepository` 上层 API 基本保持稳定。
- `feature:tasks` 不直接依赖 `core:database`。
- `feature:dashboard` 不直接依赖 `core:database`。
- 任务新增、编辑、删除、状态切换都能持久化。
- 关闭重开 App 后任务数据仍然存在。
- 项目可以成功构建。

## 建议验证命令

先验证数据库模块：

```powershell
$env:JAVA_HOME='C:\Users\Jeffery\.jdks\jbr-17.0.14'
.\gradlew.bat :core:database:compileDebugKotlin
```

再验证数据层：

```powershell
$env:JAVA_HOME='C:\Users\Jeffery\.jdks\jbr-17.0.14'
.\gradlew.bat :core:data:compileDebugKotlin
```

完整验证：

```powershell
$env:JAVA_HOME='C:\Users\Jeffery\.jdks\jbr-17.0.14'
.\gradlew.bat :app:assembleDebug
```

## 推荐学习顺序

建议按这个节奏学习和开发：

1. 先读当前 `DevJourneyRepository` 和 `FakeDevJourneyRepository`。
2. 理解 Phase 2 中任务写操作如何更新 `MutableStateFlow`。
3. 新增 `core:database` 空模块并保证能编译。
4. 添加 Room 依赖。
5. 创建 `TaskEntity` 和 `TaskStatusConverter`。
6. 创建 `TaskDao`。
7. 创建 `DevJourneyDatabase` 和 `DatabaseModule`。
8. 新增 `OfflineDevJourneyRepository`。
9. 切换 Hilt 绑定。
10. 跑通任务 CRUD。
11. 关闭重开 App 验证持久化。

Phase 3 的关键不是写很多 UI，而是理解“数据库 Flow 如何替代内存 StateFlow”。如果这个替换做得干净，后面的 Notes、Network 和 AI 阶段都会轻松很多。
