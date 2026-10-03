# MDBX 原始信息管理与快照浏览

本地编辑器：[打开设计](http://127.0.0.1:5186/mdbx-native-manager.html)。完整分享地址保存在 `canvas-url.txt`；可编辑源文件为 `canvas.json`。

原始信息管理沿用 KeePass 的层级：当前文件夹工具栏、数据库副标题、可横向滚动的面包屑、数量与文件夹／条目连续分组。搜索按需展开，原生页的刷新按钮和底部新建文件夹始终可达。表单使用现有输入与确认组件。

当前数据库和快照两侧共用 `MdbxFolderBrowser`。快照保持只读；横屏左右分别导航与滚动。文件夹数量和条目类型通过语言资源展示，未知类型保留原标识。原始字段按条目读取且默认隐藏，未知 JSON 和较新数据版本保持原样。

`canvas-preview.png` 与 `canvas-compare.png` 是 Canvas 实际渲染；`main-*.png`、`fdroid-*.png` 是公共 Android 32 模拟器上的合成数据测试截图。
