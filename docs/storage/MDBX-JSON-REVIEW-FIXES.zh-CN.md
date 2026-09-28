# MDBX JSON 审查修正与验证

日期：2026-09-28。修复独立审查发现的问题，保留数字精度修复，不回退既有搜索、图标和其他主目录修改。

## 已修复

- **特殊字段误解析**：共享核心解析真实 JSON 容器与原始成员，保留大整数、小数，以及 `$serde_json::private::Number` / `$serde_json::private::RawValue` 等字面字段。覆盖 FFI 读写、事务写入、合并、导出、同步扩展与 CLI Token/SSH 编辑。
- **设备测试依赖现场准备、不能重复运行**：两版都内置同一个 CLI 合成 fixture，默认使用独立临时目录并清理。动态往返通过显式私有目录参数和配套驱动运行，拒绝覆盖已有输出。
- **回读预期来自被测端**：对象与删除标记的 ID 在原始 fixture 里确定；CLI 独立推导 payload、分类、类型、版本和标题。两份输出分别通过后，再在可丢弃副本中制造丢字段、错版本和错分类，确认验证器拒绝。
- **错误密码只检查任意异常**：现在检查实际原生认证错误 `validation error: incorrect credential`。
- **CLI 配置合并同类问题**：临时配置文件负例复现后，配置合并也改用共享解析层，保留原有特殊扩展字段；没有修改用户真实客户端配置。

## 验证结果

| 检查 | 结果 |
| --- | --- |
| 共享核心 Rust 库测试 | 965 通过：core 53、FFI 58、storage 789、sync 65 |
| CLI 常规完整测试 | 301 通过；外部回读测试仍显式启用 |
| CLI 最后配置合并修正 | 先复现失败，修正后 15 项相关回归通过；fmt、Clippy、最终 release 构建通过 |
| 三 ABI | 每种 ABI 均通过 539 个必需导出符号检查；实际加载校验 239 个 API checksum |
| 独立 x86_64 harness | 5 项通过；默认契约再次运行通过；动态 CLI 回读及损坏负例通过 |
| 普通版实际 APK | 5 项设备回归、默认契约重复运行、动态往返及 CLI 损坏负例通过 |
| F-Droid 实际 APK | 同一组设备回归、重复运行、动态往返及 CLI 损坏负例通过 |
| Android JVM | 两版各 210 项选定 MDBX/WebDAV 测试通过 |
| 来源重建 | 精确基准加四个补丁重建，167 个 Rust/Cargo 文件经 LF 归一化后与 vendored 源码一致 |

本轮两版 debug APK 与 instrumentation APK 均构建成功。普通版设备测试版本为 `1.0.314-26092812-23`；F-Droid 为 `1.0.314`。

复用公共 `Monica_Issue136_API_32` 模拟器（API 32，x86_64）；实际应用运行 ARM64 APK，通过 native bridge 转译。没有实体 ARM、ARMv7 执行或真实云账号验证；不宣称所有历史 Android Adapter 都通过无损验收。

## 来源与复跑

Android 基准仍为 `90005c8c608c952093a4522ffa507a562e2e39a4`。按顺序应用：

1. `90005c8-read-session-lifetime.patch`
2. `90005c8-sync-delta-limits.patch`
3. `90005c8-json-precision.patch`
4. `90005c8-json-literal-keys.patch`

普通版三 ABI `.so` 已替换；F-Droid 保持从 vendored `Mdbx-ffi` 源码构建，没有新增预编译库。F-Droid 的 APK 保留少量额外符号元数据，原始文件哈希与主版不同；采用相同 llvm-strip 命令后，三 ABI 字节一致。实际打包哈希与归一化哈希分别记录，不将缓存复用称为独立 clean build 可复现。

详见各引擎的 `MDBX3_RUNTIME_PROVENANCE.json` / `MDBX3_SOURCE_PROVENANCE.json` 和 `patches/README.precision.md`。历史验证记录保留在 history 字段；后续补丁覆盖过的源文件以 `final_overlay_files` 为最终哈希。

默认运行 `MdbxCliContractInstrumentedTest` 无需外部准备。动态往返由 CLI `scripts/check_android_contract.py` 驱动，参数和示例见 CLI 文档 `docs/mdbx-review-fixes-2026-09-28.md`。测试只使用合成凭据，输出与损坏负例位于独立目录。

本机完整日志在 `C:/Users/joyins/Desktop/Monica-all/mdbx-cli-review-evidence-20260928`，包括 `FIX-RESULTS.json`、`fix-core-tests.log`、两版 `fix-*-build-tests.log`、`fix-*-device-*.log`、`fixed-*-return`、`fixed-source-verification.json` 和 `fixed-fdroid-normalization.json`。该目录是本地证据，不是安装包交付目录。
