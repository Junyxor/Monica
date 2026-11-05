# Monica Android：设计系统与重构路线

## 1. 方向判断：借鉴 Bern 的做法，不迁移到 Bern

[Bern](https://github.com/AimesSoft/Bern) 用一份 Rust 声明生成各平台原生 UI，并把“漏做”变成编译错误（缺翻译、某个布局缺入口）。我们评估过把 Monica 迁到 Bern，结论是现在不迁，理由如下：

- Bern 自己标注为 early preview（0.1.7，需要 Rust 1.99），而 Monica 是线上在用的密码库。
- Monica 的核心能力依赖 Android 原生接口：自动填充服务、Credential Provider/通行密钥、输入法、Keystore 与生物识别。这些在 Bern 的组件模型里没有对应物，迁移后仍要写大量 Kotlin。
- Android 端约 38 万行 Kotlin、29 个 ViewModel、Room 第 80 版数据库。整体重写的风险远大于收益。

我们借鉴的是 Bern 的三个理念，并在现有 Kotlin/Compose 中落地：

| Bern 理念 | 在 Monica 中的做法 |
| --- | --- |
| 界面只读状态、发命令，业务逻辑在核心层 | 组件变成纯展示；图标解析、表面色这类决策集中到一个地方（见第 2 节）；后续把屏幕状态收敛为 `UiState`（见第 4 节） |
| 语义化样式：写“列表项的颜色”，不写具体色值 | `MonicaDesign.surfaces` 语义表面色，主题解析一次，包括 OLED 纯黑模式 |
| 漏做的事在构建期暴露 | 已有的 `LocaleResourceCoverageTest` 等守卫测试继续保留；新增可渲染的设计预览，界面改动可以在 CI/本地出图审阅 |

## 2. 第一阶段（本次）完成的内容

### 设计令牌（`ui/theme/`）

- `Type.kt`：完整的字号体系。字号、行高沿用 Material 3 默认值，已有布局的测量不变；标题和 headline 改为 SemiBold，调用处不必再写 `fontWeight = Bold`。`tabularNumbers` 让验证码、倒计时使用等宽数字，跳动时不抖。
- `Shape.kt`：`MonicaShapes` 接入 `MaterialTheme`。`medium` 改为 16dp，即所有 Card 的默认圆角；`MonicaShapeTokens` 给出列表项、嵌套项、图标底板、胶囊的统一圆角。
- `Spacing.kt`：4dp 网格上的间距名称（列表项内边距、列表间距、图标与文字间距等）。
- `MonicaSurfaces.kt`：语义表面色 `item / nestedItem / stackLayer / iconTile / navigationBar / toolbar`。OLED 纯黑模式下低层容器色全是黑色，所以这里自动换成仍能和页面区分的色调。在 `MonicaTheme` 之外渲染的界面（自动填充、输入法窗口）会从当前配色推导出同样的结果。

### 统一的条目图标（`ui/icons/VaultItemIcon.kt`）

原先除了 VaultV2 使用的 `VaultItemIcon`，还有 7 处各自实现的图标解析与绘制代码：密码卡片、多密码卡片、验证器卡片（标准与紧凑两种布局）、密码详情页、快捷访问面板、自动填充列表和通行密钥列表。现在它们都调用 `VaultItemIcon`。编辑页、安全分析页和通行密钥详情里的 4 处图标预览暂未合并，见第 4 节。`VaultItemIcon` 的规则如下：

- 优先级不变：emoji → 自选品牌图标/上传图片 → 自动匹配的品牌图标 → 网站 favicon → 关联应用图标 → 未匹配策略（默认图标/首字母/隐藏）。
- 品牌图标和 favicon 统一放在浅色圆角底板上。
- **修复了一个既有问题**：品牌图标原先按系统深浅色选资源。应用主题与系统不一致时（例如系统浅色、Monica 强制深色），会出现深色卡片上的黑色 GitHub/Notion 图标。现在按图标实际所在底板的深浅选资源（`rememberSimpleIconBitmap(darkVariant = …)`）。
- 加载期间保留占位，标题不跳动；“隐藏”策略仍然不占位。
- 首字母图标按标题稳定地取主题的三种强调容器色之一。

### 列表与导航改版

- 密码卡片（`PasswordEntryCard` / `MultiPasswordEntryCard`）：
  - 去掉阴影，改用平面色调卡片；
  - 统一的图标底板，标题、副标题层级清晰；
  - 只有一行详情时不显示行首小图标；
  - 收藏、选择框垂直居中；
  - 绑定的验证码改为等宽数字加倒计时小圆环；
  - 行高从约 100dp 降到约 68dp。
- 堆叠分组（`StackedPasswordGroup`）：
  - 折叠态与普通条目同一套结构：首个条目的图标、右上角数量角标、“3 个密码 · 域名”副标题；
  - 背后的层用平面色调露出；
  - 展开态去掉描边和半透明底。
- 验证器卡片（`TotpCodeCard`）：
  - 统一图标；
  - 验证码从粗重的等宽字体改为系统字体加等宽数字；
  - 进度条更细，内边距更宽松。
  - 紧凑网格的 142dp 高度等几何约束保持不变，`AuthenticatorCardGeometryTest` 全部通过（在 Robolectric 下运行）。
- 底部导航抽成 `MonicaBottomNavigationBar`，放在独立的容器色上；英文短标签从 `Pwd/Auth/Card/Note/Pass/Set` 改为 `Logins/2FA/Wallet/Notes/Passkeys/Settings`。

### 设计预览（截图测试）

`app/src/test/.../ui/designsystem/DesignPreviewTest.kt` 用 Robolectric 原生图形渲染真实组件，输出 PNG。覆盖深色、浅色、OLED、中文，以及选择态、首字母图标、多行详情、展开堆叠等组件状态。默认跳过，需要时运行：

```bash
./gradlew :app:testDebugUnitTest --tests '*DesignPreview*' -PmonicaDesignPreviews
# 输出：app/build/outputs/design-previews/*.png
```

## 3. 验证情况

- `:app:compileDebugKotlin` 与 `:app:compileDebugAndroidTestKotlin` 通过。
- JVM 单元测试共 2,276 个（含 8 个默认跳过的设计预览），有 44 个失败。这 44 个在未修改的 `main` 上同样失败，与本次改动无关，例如依赖本机 `ssh-keygen` 的 `MdbxKeyPayloadTest`。本次改动没有引入新的失败。两个基于源码文本的守卫测试已随实现更新：卡片圆角现在来自设计令牌；快捷访问的图标改走 `VaultItemIcon`。
- 与改动组件相关的仪器测试（`AuthenticatorCardGeometryTest`、`UnreadableTotpCardTest`、`MultiPasswordProjectSelectionTest`）的断言在 Robolectric 下全部通过。
- 尚未在真机上验证：滑动删除与多选手势、共享元素转场、宽屏导航栏，以及各种配色预设下的观感。

## 4. 后续阶段（按收益与风险排序）

1. **把设计系统铺到其余页面**：
   - 卡包、笔记、通行密钥、设置页改用 `MonicaDesign.surfaces` 和 `MonicaShapeTokens`；
   - `AddEditPasswordScreen`、`AddEditTotpScreen`、`SecurityAnalysisScreen`、`PasskeyDetailPanes` 中剩余的图标预览改用 `VaultItemIcon`；
   - 逐步替换 UI 层的 6,765 处 `.dp` 字面量和 703 处 `RoundedCornerShape(...)`；
   - 可加一个守卫测试，只允许这些字面量的数量下降。
2. **拆分 `MainActivity.MonicaContent`**（约 3,400 行，一个 NavHost 中有 79 个目的地）：按功能拆成 `NavGraphBuilder.xxxGraph(...)` 文件。注意：目的地 lambda 现在通过委托属性在组合时读取状态；拆分时要传 `State<T>` 或 `() -> T`，不要传快照值，否则图在重组前读到的会是旧值。
3. **依赖注入与仓库边界**：
   - 目前 ViewModel 在 `MonicaApp` 中手工构建；
   - 有 35 个 UI 文件直接调用 `PasswordDatabase.getDatabase`；
   - 先引入轻量的组合根（或 Hilt），再让 UI 只依赖 ViewModel。
4. **屏幕状态收敛为 `UiState` 加意图**：从 `PasswordViewModel` 开始（5,636 行，12 个 MutableStateFlow），对应 Bern 的“状态快照 + 命令”。
5. **扩大 Rust 核心**：`rust-core` 已承担列表投影与检索，`rust-crypto` 承担 KDF；下一步可下沉 TOTP 生成和密码强度计算，为将来的桌面端或 Web 端共享逻辑。到那时可以再评估 Bern（或同类方案）用于新平台，而不是替换 Android 端。
