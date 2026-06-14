# DevJourney

DevJourney 是一个用于练习现代 Android 技术栈的个人学习工作台。当前版本已经搭好可运行的多模块骨架，并接入 Hilt 作为依赖注入方案。后续可以逐步加入 Room、网络、Paging、WorkManager、测试和 AI 能力。

## 当前模块

```text
app
core:model
core:data
feature:dashboard
```

| 模块 | 职责 |
| --- | --- |
| `app` | 应用入口、主题包装、启动首屏、Hilt Application |
| `core:model` | 领域模型，例如学习主题、任务、笔记 |
| `core:data` | Repository 抽象、当前内存数据源、Hilt 数据绑定 |
| `feature:dashboard` | 首页 UI、UiState、Hilt ViewModel |

## 当前架构

```text
Compose UI -> Hilt ViewModel -> Repository -> Data Source
```

当前 `core:data` 使用 `FakeDevJourneyRepository` 提供内存数据，并通过 Hilt 绑定到 `DevJourneyRepository`。下一步可以把实现替换成 Room + 网络数据源，同时保持上层 API 不变。

## 推荐下一步

1. 在 `core:data` 增加 Room，替换内存任务数据。
2. 新增 `feature:tasks`，实现任务的新增、编辑、删除和状态切换。
3. 新增 `feature:notes`，实现学习笔记和标签搜索。
4. 新增 `core:network`，接入 GitHub Repository 搜索 API。
5. 加入 Repository 单元测试和 ViewModel 测试。

## 本地构建

当前项目使用本机 JDK 17 构建：

```powershell
$env:JAVA_HOME='C:\Users\Jeffery\.jdks\jbr-17.0.14'
.\gradlew.bat :app:assembleDebug
```

如果 Gradle wrapper 正在下载 `gradle-8.14.3` 时被中断，可能会短暂出现 zip 文件锁。等待残留 wrapper 进程退出后重新执行即可。
