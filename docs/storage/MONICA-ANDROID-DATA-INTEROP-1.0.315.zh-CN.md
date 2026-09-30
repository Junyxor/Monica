# Monica Android 数据结构与跨端互通实施规范

基线：2026-09-30，Android 本地 main，1.0.315（未发布）。适用于浏览器拓展、CLI、桌面端及其开发 AI。本文描述数据语义、现有格式和验收要求，不新增磁盘格式，也不代表拓展已通过兼容验收。

目标是 Android 创建的数据能被另一端完整读取，另一端创建的数据也能被 Android 完整读取；双方编辑、同步和恢复后仍保持完整。**“100%”必须限定为固定源码与运行时版本、明确类型/字段/后端矩阵全部通过，不可仅凭本文或成功打开一个数据库宣称实现。** 设备绑定且不可导出的 Passkey 私钥不能通过格式适配变成可迁移私钥；未知未来类型可以完整通用查看，但不能假装旧客户端已经支持其业务操作。

## 1. 阅读顺序和权威来源

1. 本文：业务结构及浏览器实施要求。
2. [字段声明附录](MONICA-ANDROID-DATA-MODELS-1.0.315.md)：从本次 Android 源码提取的字段、类型、默认值；附源码哈希，避免只看过时注释。
3. [MDBX 跨端契约](MDBX-CROSS-CLIENT-CONTRACT.zh-CN.md)：引擎、未知类型、精度、同步、冲突与安全边界。
4. [多密码约定](../../Monica%20for%20Android/docs/MULTI-PASSWORD-MDBX-1.0.315.zh-CN.md)、[OTP 约定](../../Monica%20for%20Android/docs/OTP-STORAGE-COMPATIBILITY.zh-CN.md)、[数据安全验证](../../Monica%20for%20Android/docs/DATA-SAFETY-1.0.315.zh-CN.md)。这些报告不是拓展已通过测试的证据。

源代码目录：`Monica-main/Monica for Android/app/src/main/java/takagi/ru/monica/`。出现文档/源码差异时，先用实际 Android 导出和回读复现，再修正文档或实现；不能自行发明第三种兼容格式。已有跨端契约较早的 API Token schema 列表不完整，本次 `data/ApiTokenPayload.kt` 同时接受下文两个 schema。

## 2. 三层模型不能混为一谈

| 层 | 含义 | 禁止的做法 |
| --- | --- | --- |
| 产品项目 | 密码、验证器、银行卡、笔记等；一个密码项目可包含多密码和多个内容区域 | 把所有项目缩减为 username/password/notes |
| Android 模型 | `PasswordEntry`、`SecureItem`、`PasskeyEntry` 及自定义字段、附件等关联实体 | 把 Room 自增 ID 或设备文件路径当跨端身份 |
| 后端格式 | MDBX 对象/关系/Blob、Android ZIP 全量备份、KeePass、Bitwarden | 把四种序列化当同一种 JSON，或用同一份精简对象覆盖全部格式 |

Monica MDBX 是 Monica 的加密引擎格式，不是 libmdbx 键值库。复用当前 Native Host/Rust 引擎，核对 Android 打包的 `mdbx-engine/MDBX3_RUNTIME_PROVENANCE.json`；类名中的 MDBX2 不等于文件或运行时版本。

UI 中“密码项目”和引擎 Collection（历史名 project）不同：Collection 是容器，Object 是记录，多密码项目可以由同库多个 login Object 组成。

## 3. 类型目录

| 用户看到的类型 | Android 表达 | MDBX 原生类型/解释 | 关键要求 |
| --- | --- | --- | --- |
| 普通密码、旧第三方登录 | `PasswordEntry`，`loginType=PASSWORD/SSO` | `login`，payload `kind=password` | 旧 SSO 引用和提供商不可丢；新建不必再提供 SSO |
| Wi-Fi | `PasswordEntry` + `WifiData`，`WIFI` | Android 登录适配路径 | 保留 SSID、口令、企业认证、代理/IP等，不参与网站密码填充 |
| SSH | `PasswordEntry` + `SshKeyData`，`SSH_KEY` | Android 登录适配路径；不能随意改成独立 `ssh-key` | OpenSSH 私钥、公钥、指纹和扩展字段完整保留 |
| GPG | `PasswordEntry`，`GPG_KEY` + 自定义字段 | `login` 及 GPG 标记 | 私钥不自动填充；公钥分块和编码标记必须保留 |
| API Key | `PasswordEntry`，`API_KEY` + 自定义字段 | `login` | 与下行 API Token 不是同一格式 |
| API Token | `NativeApiToken` / `ApiTokenPayload` | `api-token` | schema、标签元数据、收藏与令牌一起保留 |
| 二维码/条形码 | 登录模板及对应内容字段 | 以 Android 实际导出为准 | 保留内容和码制；查看码图不等于只保存一张截图 |
| 独立验证器 | `SecureItem(TOTP)` + `TotpData` | `totp`，`kind=totp` | 五种 OTP 类型；账号相同不等于同一验证器 |
| 银行卡 | `SecureItem(BANK_CARD)` + `BankCardData` | `card`，`kind=bank_card` | 包括旧字段、卡面及附件 |
| 证件/个人信息 | `SecureItem(DOCUMENT)` + `DocumentData` | `document-ref`，`kind=document` | 不因拓展 UI 叫 identity 而改变原生类型 |
| 账单地址 | `SecureItem(BILLING_ADDRESS)` + `BillingAddressData` | `billing-address` | 不仅包含街道，还包含联系人、公司和自定义字段 |
| 支付账户 | `SecureItem(PAYMENT_ACCOUNT)` + `PaymentAccountData` | `payment-account` | 钱包、银行账户等枚举和完整资料 |
| 笔记 | `SecureItem(NOTE)` + `NoteData` | `note`，`kind=note` | 正文、Markdown、标签、自定义字段、图片/附件 |
| 通行密钥 | `PasskeyEntry` + 密钥存储 | `passkey` | 元数据读取与实际签名能力分开报告 |
| Steam maFile | 专用 Steam 路径 | `steam-mafile`，`kind=steam_mafile` | `steamid`、`account_name`、完整 `mafile_json`，不能只保留验证码 secret |
| 未知/未来类型 | 通用只读详情 | 原生 type + 原 payload | 可发现、完整查看，不能删除、隐藏或转换为 login |

上表中的 UI 名称不是允许任意写出的 wire type。附录是模型字段目录，**字段出现在 Room 模型中不证明每条后端写入链已经携带它**。Wi-Fi、图标、旧 SSO 等必须逐项用 Android 真实导出确定适配位置；Android 自身若缺字段，也必须明确记录为双向互通阻塞，不能让拓展伪造已支持。

## 4. 密码项目及 MDBX login payload

真实写入入口：`repository/Mdbx2Repository.kt` 的 `passwordMutation`、`passwordCustomFieldsPayload`，补充字段见 `repository/MdbxPasswordContentFields.kt`。读取、旧格式兼容和合并也必须一起追踪，不能只复刻写入函数。

| 字段/组 | wire 形状与意义 |
| --- | --- |
| `kind`, `monica_entry_id` | `password`；历史逻辑身份，已有值需保留；原生 object ID 仍是权威身份 |
| `password_group_id` | 可选字符串；同库同组的多条 login 才组成多密码项目 |
| `website`, `username`, `password_plain`, `notes` | 字符串；网站可能有多条，按 Android 编解码保留换行和匹配信息 |
| `app_package_name`, `app_name` | 应用绑定，不随意改为网址 |
| `login_type`, `ssh_key_data` | 子类型及 JSON 字符串；SSH schema 见附录 |
| `authenticator_key` | 完整 OTP 载荷，可能是 Base32 或 URI，不得只取 secret |
| `passkey_bindings` | 原有 JSON 字符串元数据；不是可用私钥本身 |
| `custom_fields` | 数组，每项 `title:string,value:string,is_protected:boolean,sort_order:number` |
| `email`, `phone`, `address_line`, `city`, `state`, `zip_code`, `country` | 现有个人/地址扁平字段，不能被只懂账号密码的表单清空 |
| `credit_card_number_plain`, `credit_card_holder`, `credit_card_expiry`, `credit_card_cvv_plain` | 现有支付字段；卡号和 CVV 保持字符串，含前导零 |
| `sort_order`, `mdbx_folder_id` | 显示顺序与容器投影；实际 Collection 与原生对象关联必须同步一致 |
| `bound_note_entry_id` | 稳定笔记关系；不同于本机 `bound_note_room_id` |
| `room_id`, `category_id`, `bound_note_room_id`, `bitwarden_mode`, `keepass_mode` | 兼容元信息；不能拿 Android Room ID 覆盖另一设备的对象身份/归属 |

`*_plain` 只表示交给加密引擎之前的值；MDBX 文件和同步流仍然加密。不能把 Android Keystore 密文直接填入 `password_plain`，也不能把这些字段写入日志、明文浏览器存储或错误报告。

标题、原生类型、payload version、Collection、提交身份、删除状态以及收藏/标签等原生元数据并不都在上表内。必须以实际对象 API 和标签适配器分别维护；保留创建时间，修改后只更新应更新的时间，不能每次导入都置为当前时间。

### 多密码必须这样实现

- 多条 login，各自稳定 object ID，共享显式 `password_group_id`；缺标记时各自独立。
- 不按标题、用户名、网站或密码相似度猜测合并，不把 `replica_group_id` 当成 `password_group_id`。
- 列表按项目显示；批量选择/移动/删除覆盖全部成员，保留每条密码与组关系。
- 新建两密码、编辑第二条、追加第三条、删一成员、跨库移动、重启/同步后组关系都要测。

## 5. 内容区域、排序与完整副本

### 内容字段不是可以丢弃的“内部垃圾字段”

`data/model/EntryContentFields.kt` 使用现有自定义字段承载：

- `monica.content.order`：**逗号分隔字符串**，不是 JSON 数组；保留未知 section token。具体 token 从当前编辑器和详情的枚举读取，不自行翻译。
- `monica.content.<section小写>.<field>`：支付/个人/地址等补充资料；准确字段规格见 `ui/components/EntrySupplementalFields.kt`。
- `monica.content.wallet.bank_card`、`.document`、`.address`、`.note`：下述独立副本的 JSON **字符串**。旧端不认识时也必须保存。

排序是数据，应同时影响编辑页与详情页；不能只存在拖动组件的内存数组里。系统内容元字段不应混入普通自定义字段编辑让用户误删，但必须能在受保护的高级详情看到，未知内容不得静默消失。

### `EmbeddedWalletContent` version 1

```json
{
  "version": 1,
  "id": "synthetic-copy-id",
  "kind": "NOTE",
  "title": "示例笔记副本",
  "notes": "示例备注",
  "favorite": false,
  "data": {"content": "示例正文", "tags": ["示例"], "isMarkdown": false, "customFields": []},
  "assets": []
}
```

`kind` 为 `BANK_CARD|DOCUMENT|ADDRESS|NOTE`；`data` 是对应 SecureItem 的具体对象，外层副本 `value` 才是序列化字符串。银行卡、证件、账单地址和笔记的完整副本编辑器已接入。复制资产及原生令牌 ZIP 扩展约定见 [完整副本与原生附件](../../Monica%20for%20Android/docs/NATIVE-CONTENT-ASSETS-1.0.315.zh-CN.md)，发布前以其中最终验证记录为准。

资产项：`name,displayName,mimeType,role,size,sha256`；`name` 匹配 `wallet-[a-zA-Z0-9-]+`，`size>=0`，`sha256` 为 64 位小写十六进制；role 为 `CARD_FACE|FRONT|BACK|INLINE_IMAGE|ATTACHMENT`。这些资产属于**目标密码**，不指向原卡片/笔记的本机路径。复制后删除或修改源项目，不影响副本。

卡面 `cardFace={imageAttachmentName,displayMode,showBrandIcon}`，模式为 `ALL|CARD_NUMBER_ONLY|HIDDEN`；独立卡面管理文件名为 `monica_card_face_<id>.jpg`，副本可使用 `wallet-...`。保留原始图片和全部附件字节，缩略图不是备份内容。先完成资产写入与验证，再提交引用；失败保留旧内容，不能留下看似成功的空壳。未知 version/损坏副本保留原串并只读，不能解码失败后替换成空对象。

### 新增密钥与二维码内容块（接入验证中）

密码正在增加可重复的 `API_KEY|API_TOKEN|SSH_KEY|GPG_KEY|QR_CODE` 内容块。API Key 和 API 令牌是独立概念、入口和 kind，不替换已有独立模板。完整格式见 [内容块传输约定](../../Monica%20for%20Android/docs/PASSWORD-CONTENT-BLOCKS-1.0.315.zh-CN.md)。

字段前缀 `monica.content.block.<uuid>`，manifest + Base64 分段携带完整 JSON；每段最多 1600 字符，全部走现有受保护自定义字段。`monica.content.order` 通过 `BLOCK:<uuid>` 与其他区域交错排序。兼容端必须整组保留，不能只保留 manifest 或某一段，也不能按字段显示排序重新编号。读写和备份都需覆盖大密钥、Unicode、未知字段、缺段/重复段与同类多个块。本文此小节表示格式已接入开发，不代表拓展已实现或测试完成。


`QR_CODE.data` 可增加 `mode="template"` 与 `templateVersion="1"`，`content` 保存占位符，生成时读取当前项目的值。模式缺省或 `literal` 保持普通文本；未知模式／版本必须只读保留。支持 `%ACCOUNT%`、`%PASSWORD%`、`%TITLE%`、`%URL%`、`%EMAIL%`、`%PHONE%`、`%NOTES%` 和 `%FIELD:<字段名的 UTF-8 Base64URL>%`，自定义字段引用不使用本地 ID。只替换一次，`%%` 表示百分号，Wi-Fi 模板仅对插入值转义，缺失、重复或不可读字段阻止生成。禁止将展开后的秘密写回模板。完整规则见 [二维码字段模板](../../Monica%20for%20Android/docs/QR-FIELD-TEMPLATES-1.0.315.zh-CN.md)。

## 6. SecureItem 的双层格式

MDBX 外层包含 `kind,monica_entry_id,room_id,notes,sort_order,item_data,image_paths,category_id,mdbx_folder_id,bound_password_entry_id,bitwarden_mode,keepass_mode`。`item_data` 是 **JSON 字符串**，内部键使用模型中的 camelCase；外层常用 snake_case。`image_paths` 也保留实际字符串格式，不擅自把所有 JSON 字符串变成对象/数组。

完整内部字段、类型和默认值见附录。尤其不能遗漏：

- 银行卡的 PIN、IBAN、SWIFT/BIC、routing/account/branch、币种、客服电话、自定义字段、卡面和旧字段。
- 证件的姓名各部分、证件号码、签发/有效期、国籍、公司、联系方式、地址、SSN、护照/驾照号及卡面。
- 地址的收件人、公司、公寓、邮编、国家、电话、邮箱、默认标记、自定义字段与卡面。
- 支付账户的类型、提供方、账号标识、钱包/银行信息、币种、网站、备注及自定义字段。
- 笔记的 Markdown、标签和自定义字段；`notes` 不等于 `NoteData.content`。
- SecureItem 自定义字段 `{label,value,type}` 与 login 自定义字段 `{title,value,is_protected,sort_order}` 不同；`TEXT|HIDDEN|BOOLEAN` 不能无损地全部降为普通文本。

## 7. OTP、密钥与 Passkey

OTP 类型和具体 URI 规则以链接的 OTP 文档为准：TOTP/HOTP 的 algorithm/digits/period/counter、Steam encoder、Yandex PIN、mOTP 的大小写和 PIN 均需保留。HOTP `counter` 是非负 64 位值，JS `number` 无法精确覆盖；不能因为 UI 暂未生成该算法就重写为 TOTP。私有存储 URI 和公开迁移 QR 的 PIN 披露规则不同。

同一登录可以同时含 OTP、Passkey、备注中的恢复码。三个区域必须共存；不能检测到 Passkey 就隐藏或丢弃 OTP，也不能把备注误判为凭据类型。

SSH：`schema=monica.ssh-key.v1`，`algorithm,keySize,publicKeyOpenSsh,privateKeyOpenSsh,fingerprintSha256,comment,format`，保留额外 JSON 字段。GPG 私钥沿用 password 字段，公钥采用 `monica_gpg_public_` 分块，另有 `monica_gpg_type/encoding/fingerprint/user_id`；严格复用 `GpgEntryFields` 的分块、排序与解码方式。API Key 使用 `monica_api_key_type` 和 `monica_api_key_url`。这些敏感凭据都不能误用于网站自动填充。

API Token 原生类型 `api-token`，支持 `monica.gateway.credential.v1` 与 `monica.api-token.v1`，必要字符串键 `provider,api_base,token`，可选 `note`，最大 payload 16 KiB。Gateway 的提供方/URL/令牌验证更严格；不能因为存储可读就自动赋予 Gateway 调用能力。收藏、备注/自定义字段标签元数据必须通过当前 Native Host 配套接口保留。

原生 API Token 文件归属实际 token entry ID，不能伪造 Room 附件所有者。移动保持条目身份，正文、标签和附件应在一个原子操作内提交；跨库移动先完成目标验证，再带版本检查删除源。完整副本包含所有引用资产的原始字节，不能只复制字段 JSON。数据库 ZIP 的 `native_api_tokens.json` 以 `attachments/attachmentCount` 携带文件及 SHA-256，`database_export.json.nativeTokenCount` 防止整份令牌清单被遗漏；存在此计数时必须严格验证。字段、文件总量限制及旧归档兼容约定见上述附件补充文档；不要把数据库导出 ZIP 与应用全量备份混为一谈。

Passkey payload 包括 `credential_id,rp_id,rp_name,user_id,user_name,user_display_name,public_key_algorithm,public_key,private_key_alias,transports,aaguid,sign_count,notes,passkey_mode` 及兼容元信息。名称 `private_key_alias` 不代表它总是可跨端密钥；区分本机引用、可导出 PKCS#8、不可导出的硬件密钥和缺失密钥。不能把 `monica-passkey-key-ref-v1:` 本机引用当私钥上传。保留原密钥算法和编码，不猜 Base64/Base64URL；实际签名验证、计数器更新必须单独测试。完整备份无法导出时失败并给出项目级说明；仅用户明确选择才生成带遗漏清单、仅可合并恢复的部分备份。

## 8. 读写、同步、恢复的无损规则

1. 原生 object ID、Collection ID、类型和 payload version 稳定；副本才创建新身份。Room ID、浏览器缓存 ID、显示分组 ID 分别管理。
2. 原始 source record + 当前版本一起保留。写入前重读并校验 revision，只 patch 当前编辑器拥有的字段，递归保留未知键、数组扩展项。只做 `{...original,...new}` 顶层合并不足以保护嵌套数据。
3. 区分不存在、null、空字符串、false、0；明确空密码不能回退旧 password。大整数、高精度小数及 `$serde_json::private::Number` 等字面键原值保存；普通 `JSON.parse`/`JSON.stringify` 不可作为无损通道。
4. 未知类型/高版本：通用安全只读详情、保留原始类型和 payload，不能落入普通密码表单或自动填充。已知类型未知字段也须保留；无法保证时阻止写入。
5. MDBX 使用引擎 bootstrap + `.sync` 不可变段和 Blob，核对 `MdbxRemoteSyncPaths`；保留配对 checkpoint、提交依赖、幂等和原子失败。不能以整库覆盖替代已有增量协议。
6. 全量 ZIP 备份与 MDBX 同步是两条链路，分别测。ZIP 以 Android 实际导出归档的目录/manifest/版本/历史/附件为准，不能拿 Room dump 冒充备份。WebDAV 和 OneDrive 仅是传输层，不改变归档含义。
7. 附件 CEK 与本机包裹密钥分离；现有 `MdbxAttachmentCekPayload` 使用 `portable-attachment-cek-v1:` 的 32 字节 CEK 表示，由 vault 加密保护。优先复用已有附件 API，不能把本机 wrapped CEK 复制到另一个安装实例。
8. 下载失败不覆盖旧文件；恢复先验证后提交，失败/取消回滚；部分备份不得覆盖恢复。列表为空、解密失败或缺 Blob 不是用户删除指令。冲突、超限、缺密钥要明确展示，不静默跳过并提示“成功”。

## 9. 浏览器拓展静态检查发现与执行顺序

检查目录：`C:/Users/joyins/Desktop/Monica-all/monica-extension`。以下是本轮**静态审查线索**，不是已复现的端到端缺陷，也不是穷尽列表。

| 线索 | 证据入口 | 对目标会话的要求 |
| --- | --- | --- |
| 核心模型/两个主要 codec 未找到 `passwordGroupId/password_group_id` | `src/core/model.ts`、`providers/mdbx2/mdbx2-item-codec.ts`、`providers/webdav/android-backup-codec.ts` | 核查 source envelope 能否保留，同时补齐真正项目分组、新建和整项操作；原串保留不等于支持 UI |
| `LoginItem.loginType` 联合未列 GPG_KEY/API_KEY | `src/core/model.ts` | 追踪其他识别标记与写回，不能让新建/保存退化为普通密码 |
| MDBX payload 使用普通 JSON.parse，OTP counter 为 number | 同上及 codec `parsePayload` | 复现 9007199254740993、64 位计数器、未知数字写回，建立无损解析通道 |
| 未知 type 返回 unsupportedReason 且没有 item | codec `decodeMdbx2Object` | 追踪调用方是否能列出和安全查看完整原始对象；不能只留日志 |
| API Token 依赖 Host 标签元信息；Passkey 目前可用性判定针对 -7 | 同一 codec | 清楚区分可读、可编辑、可签名；升级 Host 协议时保留兼容门禁 |
| 已有 source record、mergeCustomFields、附件/历史/同步测试 | `ProviderSourceRecord` 与 `tests/interop` | 复用并补齐，不推倒重建；对照 Android 新字段验证深层无损 |
| 互通脚本默认启动 Pixel_Fold_API_35 | `tests/interop/README.md` | 改用/显式指定公共 AVD，不另建默认模拟器 |

执行顺序：先输出逐类型/字段/后端差异表 → 修复无损数据层及 Native Host → 双向原生 fixture 验证 → 按数据模型完善创建/详情/编辑 → M3E Canvas 设计与 Edge 实测 → 全矩阵回归并提交实际证据。数据安全缺陷优先，不能以 UI 进度替代数据验收。

Android main 是本次基线，不因拓展旧结构而删改 Android 数据。若 Android 写入链自身遗漏必要字段，单列复现与最小修复建议，不擅自修改 Android 主仓库；不要声称单改拓展就已解决双方问题。

## 10. UI 与测试环境交接要求

- **只用 Microsoft Edge，不用 Chrome。** Playwright 明确设置 `msedge` channel 或实际 Edge executable，并在报告记录路径/版本；不能默认 Chromium 后称为 Edge。测试真实加载的拓展 popup、side panel、全页、Native Messaging 和锁定重开，不只测试 Vite 普通网页。
- 设计使用[本地 M3E Canvas](http://127.0.0.1:5186/)：先结合现有流程更新可编辑草图，使用现成 searchBar/textField/select/listItem/button/sheet 等组件，不用说明文字卡片充当 UI；保留草图链接，落实后在 Edge 检查实际渲染。
- 创建、详情、编辑采用一致内容分组，密钥默认隐藏，OTP/Passkey/备注同时可见，卡面与附件真的能打开；新建内容直接拖动排序，详情遵循顺序。未知内容给安全通用视图；支持窄 popup、侧栏、大字体、键盘与读屏。
- 复用 `Monica_Issue136_API_32`，先确认是否运行；当前设备序号需现场核实。数据目录 `D:/AndroidSDK/codex-avd/issue-136-vault-navigation-20260914`，不 wipe，不另建测试 AVD。已有互通 runner 应显式传入对应 AVD/serial，避免默认启动另一台。
- 使用合成数据和独立远端测试目录，不清空用户库、不重置 Keystore、不使用真实账号密码填充测试网页。不在截图、Console、报告或 fixture 中泄露真实资料。

## 11. 双向验收矩阵与交付标准

每个“类型 × 关键字段 × 后端”执行以下闭环，记录通过、失败、跳过与证据路径：

| 编号 | 必测闭环 | 判定 |
| --- | --- | --- |
| A | Android UI 创建 → 原生持久化 → 拓展 Edge 读取/详情 | 所有值、类型、顺序、引用、字节与状态一致 |
| B | 拓展 UI 创建 → 原生保存 → Android 重开/详情 | Android 真正识别类型；不是只在 JSON 看得到 |
| C | A/B 分别编辑一个字段 → 对端重开 → 再编辑返回 | 未编辑字段、未来字段、身份和创建时间不变 |
| D | 双端关闭重启/锁定解锁/缓存重建 | 数据可恢复，不能依赖另一端本机密钥或缓存 |
| E | WebDAV MDBX bootstrap/segments/Blob 双向同步 | 实际原生引擎，重复/乱序/中断/冲突不丢数据 |
| F | Android 全量备份 → 拓展读取/导出 → Android 恢复 | 元数据、历史、附件、分组齐全；失败不破坏原数据 |
| G | KeePass 与 Bitwarden 各自往返 | 各自协议测试，不用 MDBX 通过代替；模拟服务与真实服务分开报告 |
| H | 删除/回收站/归档/收藏/移动/分类层级 | 稳定身份和预期语义；不复活，不重复，不丢组成员 |

必备 fixture：两条同标题独立密码、显式三密码组、多网址/应用绑定、全部五种 OTP（含大 HOTP counter）、OTP+Passkey+恢复备注同条目、银行卡所有低频字段与卡面、完整笔记副本含图片/附件、内容顺序、全部 SecureItem 类型、Wi-Fi 企业/代理/IP、SSH/GPG/API Key/API Token/Steam/码图、未知类型/高版本/嵌套未来字段/大数字/空值/Unicode/前导零。

故障 fixture：错密码、损坏/截断归档、断网、缺 Blob、401/429/507、过期 revision、取消恢复、不可导出 Passkey、旧本机密文、超限 payload；失败时旧数据和旧备份保留，不能推进成功游标或伪装完整备份。

复用拓展现有 `npm run check`、`npm test`、`npm run build`、`test:mdbx2-host`、`test:mdbx2-android-interop`、`test:keepass-interop`、`test:bitwarden-interop` 及 E2E。先审查脚本环境和 fixture 覆盖，不盲跑默认 AVD；现有 MDBX 引擎往返测试必须补上 Android app 业务模型/真实 UI 层，否则不能证明银行卡副本、分组等已互通。

交付：差异表、最终能力矩阵、可重复 fixture/命令、Edge 截图与 Canvas 链接、两端源码版本及 dirty diff 哈希、Android runtime provenance/Native Host 版本、通过/失败/跳过计数。报告必须区分“读取/无损保留/编辑/业务操作”四种能力；任何未测项不得写成通过。

本轮这里只做文档与交接；拓展代码修复、设计和 Edge 端到端测试由指定拓展会话执行。
