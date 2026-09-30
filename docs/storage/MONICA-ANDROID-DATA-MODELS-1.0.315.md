# Android 1.0.315 字段声明附录

提取时间：2026-09-30。本附录为当前工作树的源码声明快照，不是独立 JSON Schema。Room 字段名/默认值不等于每个后端的 wire 字段名。具体跨端映射、版本与无损要求见 [实施规范](MONICA-ANDROID-DATA-INTEROP-1.0.315.zh-CN.md)。

模型、枚举和内容键按源文件摘录；未列出的函数仍应查源码。`@Transient` 字段不直接序列化。哈希用于发现文档后源码变化，不代表兼容测试已经通过。

## `data/PasswordEntry.kt`

SHA-256: `2c9b0b0c88773ca8a285fe8cc9f59bac8438e168da274ffbd0689ccd32074b42`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/data/PasswordEntry.kt)

```kotlin
data class PasswordEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val website: String,
    val username: String,
    val password: String, // This will be encrypted
    val notes: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isFavorite: Boolean = false,
    val sortOrder: Int = 0, // 排序顺序(用于拖动排序)
    val isGroupCover: Boolean = false, // 是否作为分组封面
    val appPackageName: String = "", // 关联的应用包名（用于自动填充匹配）
    val appName: String = "", // 关联的应用名称（用于显示）

    // Phase 7: 个人信息字段
    val email: String = "",
    val phone: String = "",

    // Phase 7: 地址信息字段
    val addressLine: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val country: String = "",

    // Phase 7: 支付信息字段 (加密存储)
    val creditCardNumber: String = "",      // 加密存储
    val creditCardHolder: String = "",
    val creditCardExpiry: String = "",       // 格式: MM/YY
    val creditCardCVV: String = "",           // 加密存储

    val categoryId: Long? = null, // 分类ID
    @ColumnInfo(defaultValue = "NULL")
    val boundNoteId: Long? = null, // 绑定的笔记ID

    // 本地 KeePass 数据库归属
    @ColumnInfo(defaultValue = "NULL")
    val keepassDatabaseId: Long? = null, // 归属的 KeePass 数据库ID
    @ColumnInfo(defaultValue = "NULL")
    val keepassGroupPath: String? = null, // 归属的 KeePass 分组路径
    @ColumnInfo(name = "keepass_entry_uuid", defaultValue = "NULL")
    val keepassEntryUuid: String? = null, // KeePass 原生条目 UUID
    @ColumnInfo(name = "keepass_group_uuid", defaultValue = "NULL")
    val keepassGroupUuid: String? = null, // KeePass 当前分组 UUID

    // MDBX project-centric 数据库归属
    @ColumnInfo(name = "mdbx_database_id", defaultValue = "NULL")
    val mdbxDatabaseId: Long? = null, // 归属的 MDBX 数据库ID
    @ColumnInfo(name = "mdbx_folder_id", defaultValue = "NULL")
    val mdbxFolderId: String? = null, // 归属的 MDBX 文件夹ID

    // 关联的验证器密钥 (TOTP Secret)
    val authenticatorKey: String = "",  // 用于存储绑定的TOTP验证器密钥

    // 绑定的通行密钥元数据（JSON）
    @ColumnInfo(name = "passkey_bindings", defaultValue = "")
    val passkeyBindings: String = "",

    // SSH 密钥对元数据（JSON）
    @ColumnInfo(name = "ssh_key_data", defaultValue = "")
    val sshKeyData: String = "",

    // 第三方登录(SSO)字段
    @ColumnInfo(defaultValue = "PASSWORD")
    val loginType: String = "PASSWORD",  // 登录类型: PASSWORD / SSO / WIFI
    @ColumnInfo(defaultValue = "")
    val ssoProvider: String = "",        // SSO提供商: GOOGLE, APPLE, FACEBOOK 等
    @ColumnInfo(defaultValue = "NULL")
    val ssoRefEntryId: Long? = null,     // 引用的账号条目ID

    // WIFI 条目扩展数据（JSON 序列化的 takagi.ru.monica.data.model.WifiData）
    // 仅当 loginType == "WIFI" 时使用；其他登录类型保持空字符串以节省空间。
    @ColumnInfo(name = "wifi_metadata", defaultValue = "")
    val wifiMetadata: String = "",

    // 自定义图标字段
    @ColumnInfo(defaultValue = "NONE")
    val customIconType: String = "NONE", // NONE / SIMPLE_ICON / UPLOADED
    @ColumnInfo(defaultValue = "NULL")
    val customIconValue: String? = null, // SIMPLE_ICON: slug, UPLOADED: local file name
    @ColumnInfo(defaultValue = "0")
    val customIconUpdatedAt: Long = 0L,

    // 回收站功能 - 软删除字段
    @ColumnInfo(defaultValue = "0")
    val isDeleted: Boolean = false,      // 是否已删除（在回收站中）
    @ColumnInfo(defaultValue = "NULL")
    val deletedAt: java.util.Date? = null, // 删除时间（用于自动清空）

    // 归档功能 - 临时隐藏字段（不删除）
    @ColumnInfo(defaultValue = "0")
    val isArchived: Boolean = false,
    @ColumnInfo(defaultValue = "NULL")
    val archivedAt: java.util.Date? = null,

    @ColumnInfo(name = "replica_group_id", defaultValue = "NULL")
    val replicaGroupId: String? = null,

    // Explicit membership created by the multi-password editor, independent of storage object IDs.
    @ColumnInfo(name = "password_group_id", defaultValue = "NULL")
    val passwordGroupId: String? = null,

    // === Bitwarden 集成字段 ===
    // 当此条目来自 Bitwarden 时，以下字段有值
    @ColumnInfo(name = "bitwarden_vault_id", defaultValue = "NULL")
    val bitwardenVaultId: Long? = null,   // 归属的 Bitwarden Vault ID

    @ColumnInfo(name = "bitwarden_cipher_id", defaultValue = "NULL")
    val bitwardenCipherId: String? = null, // Bitwarden Cipher UUID

    @ColumnInfo(name = "bitwarden_folder_id", defaultValue = "NULL")
    val bitwardenFolderId: String? = null, // Bitwarden Folder UUID

    @ColumnInfo(name = "bitwarden_revision_date", defaultValue = "NULL")
    val bitwardenRevisionDate: String? = null, // 服务器版本号 (ISO 8601)

    @ColumnInfo(name = "bitwarden_cipher_type", defaultValue = "1")
    val bitwardenCipherType: Int = 1,     // Cipher 类型: 1=Login, 2=SecureNote, 3=Card, 4=Identity

    @ColumnInfo(name = "bitwarden_local_modified", defaultValue = "0")
    val bitwardenLocalModified: Boolean = false // 本地是否有未同步的修改
) : Parcelable
```

## `data/SecureItem.kt`

SHA-256: `d601363d528fb0f54f7c02ec4c53f4900c45acbb1c4cd63b23df4deb861732d1`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/data/SecureItem.kt)

```kotlin
data class SecureItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 通用字段
    val itemType: ItemType,  // 数据类型:TOTP、BANK_CARD、DOCUMENT
    val title: String,       // 标题/名称
    val notes: String = "",  // 备注
    val isFavorite: Boolean = false,
    val sortOrder: Int = 0,  // 排序顺序(用于拖动排序)
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),

    // 类型特定数据(JSON格式存储)
    val itemData: String,    // 存储不同类型的具体数据(JSON)

    // 图片附件路径(加密存储)
    val imagePaths: String = "", // JSON数组,存储图片文件路径

    // 分类ID（用于验证器等支持分类的类型）
    @ColumnInfo(defaultValue = "NULL")
    val categoryId: Long? = null,

    // 归属的 KeePass 数据库（用于统一目标存储选择）
    @ColumnInfo(name = "keepass_database_id", defaultValue = "NULL")
    val keepassDatabaseId: Long? = null,
    @ColumnInfo(name = "keepass_group_path", defaultValue = "NULL")
    val keepassGroupPath: String? = null,
    @ColumnInfo(name = "keepass_entry_uuid", defaultValue = "NULL")
    val keepassEntryUuid: String? = null,
    @ColumnInfo(name = "keepass_group_uuid", defaultValue = "NULL")
    val keepassGroupUuid: String? = null,

    // MDBX project-centric 数据库归属
    @ColumnInfo(name = "mdbx_database_id", defaultValue = "NULL")
    val mdbxDatabaseId: Long? = null,
    @ColumnInfo(name = "mdbx_folder_id", defaultValue = "NULL")
    val mdbxFolderId: String? = null,

    // 回收站功能 - 软删除字段
    @ColumnInfo(defaultValue = "0")
    val isDeleted: Boolean = false,     // 是否已删除（在回收站中）
    @ColumnInfo(defaultValue = "NULL")
    val deletedAt: Date? = null,         // 删除时间（用于自动清空）

    @ColumnInfo(name = "replica_group_id", defaultValue = "NULL")
    val replicaGroupId: String? = null,

    // Bitwarden 同步字段
    @ColumnInfo(name = "bitwarden_vault_id", defaultValue = "NULL")
    val bitwardenVaultId: Long? = null,           // 关联的 Bitwarden Vault

    @ColumnInfo(name = "bitwarden_cipher_id", defaultValue = "NULL")
    val bitwardenCipherId: String? = null,        // Bitwarden Cipher UUID

    @ColumnInfo(name = "bitwarden_folder_id", defaultValue = "NULL")
    val bitwardenFolderId: String? = null,        // Bitwarden Folder UUID

    @ColumnInfo(name = "bitwarden_revision_date", defaultValue = "NULL")
    val bitwardenRevisionDate: String? = null,    // 最后同步的服务器版本日期

    @ColumnInfo(name = "bitwarden_local_modified", defaultValue = "0")
    val bitwardenLocalModified: Boolean = false,  // 本地是否有未同步的修改

    @ColumnInfo(name = "sync_status", defaultValue = "NONE")
    val syncStatus: String = "NONE"               // 同步状态: NONE, PENDING, SYNCING, SYNCED, FAILED, CONFLICT
)

enum class ItemType {
    PASSWORD,    // 密码
    TOTP,        // 验证器
    BANK_CARD,   // 银行卡
    DOCUMENT,    // 证件
    BILLING_ADDRESS, // 账单地址
    PAYMENT_ACCOUNT, // 支付方式
    NOTE         // 笔记
}
```

## `data/PasskeyEntry.kt`

SHA-256: `e16ed333716dda2a03db5629b8a3a4688269bee0f4e656a397965754170f491a`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/data/PasskeyEntry.kt)

```kotlin
data class PasskeyEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "credential_id")
    val credentialId: String,

    @ColumnInfo(name = "rp_id")
    val rpId: String,

    @ColumnInfo(name = "rp_name")
    val rpName: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "user_name")
    val userName: String,

    @ColumnInfo(name = "user_display_name")
    val userDisplayName: String,

    @ColumnInfo(name = "public_key_algorithm")
    val publicKeyAlgorithm: Int = -7, // ES256

    @ColumnInfo(name = "public_key")
    val publicKey: String,

    @ColumnInfo(name = "private_key_alias")
    val privateKeyAlias: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_used_at")
    val lastUsedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "use_count")
    val useCount: Int = 0,

    @ColumnInfo(name = "icon_url")
    val iconUrl: String? = null,

    @ColumnInfo(name = "is_discoverable")
    val isDiscoverable: Boolean = true,

    @ColumnInfo(name = "is_user_verification_required")
    val isUserVerificationRequired: Boolean = true,

    @ColumnInfo(name = "transports")
    val transports: String = "internal", // 逗号分隔的传输方式

    @ColumnInfo(name = "aaguid")
    val aaguid: String = "",

    @ColumnInfo(name = "sign_count")
    val signCount: Long = 0,

    @ColumnInfo(name = "is_backed_up")
    val isBackedUp: Boolean = false,

    @ColumnInfo(name = "notes")
    val notes: String = "",

    // 绑定的密码条目（可为空，支持后期绑定）
    @ColumnInfo(name = "bound_password_id", defaultValue = "NULL")
    val boundPasswordId: Long? = null,

    // 统一文件夹归属（复用本地分类体系）
    @ColumnInfo(name = "category_id", defaultValue = "NULL")
    val categoryId: Long? = null,

    // 归属的 KeePass 数据库（用于统一目标存储选择）
    @ColumnInfo(name = "keepass_database_id", defaultValue = "NULL")
    val keepassDatabaseId: Long? = null,

    // KeePass 分组路径（为空表示数据库根目录）
    @ColumnInfo(name = "keepass_group_path", defaultValue = "NULL")
    val keepassGroupPath: String? = null,

    // MDBX project-centric 数据库归属
    @ColumnInfo(name = "mdbx_database_id", defaultValue = "NULL")
    val mdbxDatabaseId: Long? = null,
    @ColumnInfo(name = "mdbx_folder_id", defaultValue = "NULL")
    val mdbxFolderId: String? = null,

    // Bitwarden 同步字段（仅同步元数据，私钥无法导出）
    @ColumnInfo(name = "bitwarden_vault_id", defaultValue = "NULL")
    val bitwardenVaultId: Long? = null,           // 关联的 Bitwarden Vault

    // Bitwarden 文件夹 ID（为空表示 Vault 根目录）
    @ColumnInfo(name = "bitwarden_folder_id", defaultValue = "NULL")
    val bitwardenFolderId: String? = null,

    @ColumnInfo(name = "bitwarden_cipher_id", defaultValue = "NULL")
    val bitwardenCipherId: String? = null,        // Bitwarden Cipher UUID

    @ColumnInfo(name = "sync_status", defaultValue = "NONE")
    val syncStatus: String = "NONE",              // 同步状态: NONE, PENDING, SYNCING, SYNCED, FAILED

    // Passkey 模式:
    // LEGACY         -> 旧 Monica 通行密钥（保留本地兼容，不参与 Bitwarden 可用同步）
    // BW_COMPAT      -> Bitwarden 兼容模式（可参与 Bitwarden/Keyguard 同步）
    // KEEPASS_COMPAT -> KeePassDX/KeePassXC KPEX_PASSKEY_* 兼容格式（可与 Monica 写入/回读的 KDBX 互通）
    @ColumnInfo(name = "passkey_mode", defaultValue = "'LEGACY'")
    val passkeyMode: String = MODE_LEGACY
)
```

## `data/model/SecureItemModels.kt`

SHA-256: `c57c89546aab9d834b634bb5469595f5c2ac33ea2302d9d892f8c9002a60fb9e`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/data/model/SecureItemModels.kt)

```kotlin
enum class OtpType {
    TOTP,   // 基于时间的一次性密码 (Time-based OTP)
    HOTP,   // 基于计数器的一次性密码 (HMAC-based OTP)
    STEAM,  // Steam Guard
    YANDEX, // Yandex OTP
    MOTP    // Mobile-OTP
}

data class TotpData(
    val secret: String,                    // OTP密钥
    val issuer: String = "",               // 发行者（如：Google, GitHub, Steam）
    val accountName: String = "",          // 账户名
    val period: Int = 30,                  // 时间周期（默认30秒，仅TOTP/Steam/Yandex/mOTP使用）
    val digits: Int = 6,                   // 验证码位数（默认6位，Steam为5位）
    val algorithm: String = "SHA1",        // 算法（SHA1, SHA256, SHA512）
    val otpType: OtpType = OtpType.TOTP,  // OTP类型（默认TOTP，确保向后兼容）
    val counter: Long = 0,                 // 计数器（仅HOTP使用）
    val pin: String = "",                  // PIN码（仅mOTP使用，应加密存储）
    val link: String = "",                 // 关联链接
    val associatedApp: String = "",        // 关联应用
    val customIconType: String = "NONE",   // NONE / SIMPLE_ICON / UPLOADED
    val customIconValue: String? = null,   // SIMPLE_ICON: slug, UPLOADED: local file name
    val customIconUpdatedAt: Long = 0L,    // 图标更新时间（毫秒）
    val boundPasswordId: Long? = null,     // 绑定的密码ID
    val categoryId: Long? = null,          // 分类ID（用于验证器分类筛选）
    val keepassDatabaseId: Long? = null,   // 归属的 KeePass 数据库ID
    // Steam 令牌元数据（用于共存导入和精确去重）
    val steamFingerprint: String = "",
    val steamDeviceId: String = "",
    val steamSerialNumber: String = "",
    val steamSharedSecretBase64: String = "",
    val steamRevocationCode: String = "",
    val steamIdentitySecret: String = "",
    val steamTokenGid: String = "",
    val steamRawJson: String = ""
)

data class BankCardData(
    val cardNumber: String,       // 卡号（加密存储）
    val cardholderName: String,   // 持卡人姓名
    val expiryMonth: String,      // 有效期月份
    val expiryYear: String,       // 有效期年份
    val cvv: String = "",         // CVV安全码（加密存储）
    val bankName: String = "",    // 银行名称
    val cardType: CardType = CardType.CREDIT, // 卡类型
    val billingAddress: String = "", // 账单地址（JSON格式存储BillingAddress）
    val brand: String = "",
    val nickname: String = "",
    val validFromMonth: String = "",
    val validFromYear: String = "",
    val pin: String = "",
    val iban: String = "",
    val swiftBic: String = "",
    val routingNumber: String = "",
    val accountNumber: String = "",
    val branchCode: String = "",
    val currency: String = "",
    val customerServicePhone: String = "",
    val customFields: List<SecureCustomField> = emptyList(),
    /** Monica-managed visual card face. Null keeps the legacy card layout unchanged. */
    val cardFace: CardFaceConfig? = null
)

enum class CardFaceDisplayMode {
    ALL,
    CARD_NUMBER_ONLY,
    HIDDEN
}

data class CardFaceConfig(
    val imageAttachmentName: String,
    val displayMode: CardFaceDisplayMode = CardFaceDisplayMode.ALL,
    val showBrandIcon: Boolean = true
)

data class BillingAddress(
    val streetAddress: String = "",   // 街道地址
    val apartment: String = "",       // 公寓/单元号
    val city: String = "",            // 城市
    val stateProvince: String = "",   // 州/省
    val postalCode: String = "",      // 邮政编码
    val country: String = ""          // 国家
)

data class BillingAddressData(
    val fullName: String = "",
    val company: String = "",
    val streetAddress: String = "",
    val apartment: String = "",
    val city: String = "",
    val stateProvince: String = "",
    val postalCode: String = "",
    val country: String = "",
    val phone: String = "",
    val email: String = "",
    val isDefault: Boolean = false,
    val customFields: List<SecureCustomField> = emptyList(),
    /** Optional visual card face. The image itself is stored as an encrypted attachment. */
    val cardFace: CardFaceConfig? = null
)

enum class PaymentAccountType {
    DIGITAL_WALLET,
    BANK_ACCOUNT,
    PAYMENT_APP,
    BUY_NOW_PAY_LATER,
    CRYPTO_WALLET,
    OTHER
}

data class PaymentAccountData(
    val paymentType: PaymentAccountType = PaymentAccountType.DIGITAL_WALLET,
    val provider: String = "",
    val accountName: String = "",
    val accountHolderName: String = "",
    val email: String = "",
    val phone: String = "",
    val username: String = "",
    val accountId: String = "",
    val maskedAccountNumber: String = "",
    val linkedCardLast4: String = "",
    val routingNumber: String = "",
    val iban: String = "",
    val swiftBic: String = "",
    val billingAddress: String = "",
    val website: String = "",
    val currency: String = "",
    val notes: String = "",
    val isDefault: Boolean = false,
    val customFields: List<SecureCustomField> = emptyList()
)

enum class CardType {
    CREDIT,      // 信用卡
    DEBIT,       // 借记卡
    PREPAID      // 预付卡
}

enum class SecureCustomFieldType {
    TEXT,
    HIDDEN,
    BOOLEAN
}

data class SecureCustomField(
    val label: String,
    val value: String = "",
    val type: SecureCustomFieldType = SecureCustomFieldType.TEXT
)

data class DocumentData(
    val documentType: DocumentType, // 证件类型
    val documentNumber: String,      // 证件号码（加密存储）
    val fullName: String,            // 姓名
    val issuedDate: String = "",     // 签发日期
    val expiryDate: String = "",     // 有效期至
    val issuedBy: String = "",       // 签发机关
    val nationality: String = "",    // 国籍
    val additionalInfo: String = "",  // 其他信息
    val title: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val address1: String = "",
    val address2: String = "",
    val address3: String = "",
    val city: String = "",
    val stateProvince: String = "",
    val postalCode: String = "",
    val country: String = "",
    val company: String = "",
    val email: String = "",
    val phone: String = "",
    val ssn: String = "",
    val username: String = "",
    val passportNumber: String = "",
    val licenseNumber: String = "",
    val customFields: List<SecureCustomField> = emptyList(),
    /** Optional visual card face. The image itself is stored as an encrypted attachment. */
    val cardFace: CardFaceConfig? = null
)

enum class DocumentType {
    ID_CARD,       // 身份证
    PASSPORT,      // 护照
    DRIVER_LICENSE,// 驾驶证
    SOCIAL_SECURITY,// 社保卡
    OTHER          // 其他
}

data class NoteData(
    val content: String,            // 笔记正文
    val tags: List<String> = emptyList(), // 标签列表
    val isMarkdown: Boolean = false,     // 是否为Markdown格式
    val customFields: List<SecureCustomField> = emptyList()
)
```

## `data/model/WifiData.kt`

SHA-256: `61d1fc6c19e8e7d171e90147527e2a1a0c39ba3dcd0f7dcdfe60dd4c25e1be86`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/data/model/WifiData.kt)

```kotlin
data class WifiData(
    val ssid: String = "",
    val hiddenNetwork: Boolean = false,
    val security: WifiSecurity = WifiSecurity.WPA2_WPA3,
    val eap: WifiEapSettings? = null,
    val macRandomization: WifiMacRandomization = WifiMacRandomization.DEFAULT,
    val proxy: WifiProxy = WifiProxy.None,
    val ip: WifiIp = WifiIp.Dhcp,
    val bssid: String = ""
)

enum class WifiSecurity {
    NONE,           // 开放网络
    WEP,            // 已淘汰，仅兼容老路由
    WPA_WPA2,       // WPA/WPA2-Personal
    WPA2_WPA3,      // WPA2/WPA3-Personal（默认值，覆盖绝大多数家庭网络）
    WPA3,           // WPA3-Personal
    WPA2_ENTERPRISE,
    WPA3_ENTERPRISE
}

data class WifiEapSettings(
    val method: WifiEapMethod = WifiEapMethod.PEAP,
    val phase2: WifiEapPhase2 = WifiEapPhase2.MSCHAPV2,
    val anonymousIdentity: String = "",
    val caCertificate: String = "",
    val domain: String = ""
)

enum class WifiEapMethod { PEAP, TLS, TTLS, PWD, SIM, AKA, AKA_PRIME }

enum class WifiEapPhase2 { NONE, PAP, MSCHAP, MSCHAPV2, GTC, SIM, AKA, AKA_PRIME }

enum class WifiMacRandomization {
    DEFAULT,    // 使用系统默认（通常是随机）
    RANDOMIZED, // 使用随机 MAC
    DEVICE_MAC  // 使用设备 MAC（Android 10+ 可选）
}

    data class Manual(
        val host: String = "",
        val port: Int = 0,
        val bypassList: String = ""
    ) : WifiProxy()

    data class AutoConfig(
        val pacUrl: String = ""
    ) : WifiProxy()

    data class Static(
        val ipAddress: String = "",
        val gateway: String = "",
        val networkPrefixLength: Int = 24,
        val dns1: String = "",
        val dns2: String = ""
    ) : WifiIp()
```

## `data/model/SshKeyModels.kt`

SHA-256: `50c3b7d5705b2968615386a0089a3da4c4ed5c92824f7f403ee3632721366d80`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/data/model/SshKeyModels.kt)

```kotlin
data class SshKeyData(
    val algorithm: String = "",
    val keySize: Int = 0,
    val publicKeyOpenSsh: String = "",
    val privateKeyOpenSsh: String = "",
    val fingerprintSha256: String = "",
    val comment: String = "",
    val format: String = FORMAT_OPENSSH,
    val schema: String = SCHEMA_V1,
    @Transient val additionalFields: Map<String, JsonElement> = emptyMap()
)
```

## `data/model/EmbeddedWalletContent.kt`

SHA-256: `3434eb61ce9d8f551ee83dc6b2a3b44d6b2a43c461cb278067b0a6c4ab1da40e`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/data/model/EmbeddedWalletContent.kt)

```kotlin
    enum class Kind(val itemType: ItemType) {
        BANK_CARD(ItemType.BANK_CARD), DOCUMENT(ItemType.DOCUMENT), ADDRESS(ItemType.BILLING_ADDRESS), NOTE(ItemType.NOTE)
    }

    enum class AssetRole { CARD_FACE, FRONT, BACK, INLINE_IMAGE, ATTACHMENT }

    data class Asset(
        val name: String,
        val displayName: String,
        val mimeType: String,
        val role: AssetRole,
        val size: Long,
        val sha256: String,
    )

        data class Available(val snapshot: Snapshot) : ReadResult

        data class Unavailable(val original: String) : ReadResult
```

## `data/model/EntryContentFields.kt`

SHA-256: `60fa98ba0238753110fa57d54df1234fdec233b97fdcf42fd5f1830cdf84995a`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/data/model/EntryContentFields.kt)

```kotlin
package takagi.ru.monica.data.model

import takagi.ru.monica.data.CustomFieldDraft

/** Portable additions use the existing encrypted custom-field pipeline on every backend. */
object EntryContentFields {
    const val ORDER = "monica.content.order"
    fun key(section: String, field: String) = "monica.content.${section.lowercase()}.$field"
    fun value(fields: List<CustomFieldDraft>, section: String, field: String): String =
        fields.firstOrNull { it.title == key(section, field) }?.value.orEmpty()
    fun update(fields: List<CustomFieldDraft>, section: String, field: String, value: String,
        protected: Boolean): List<CustomFieldDraft> {
        val title = key(section, field)
        val index = fields.indexOfFirst { it.title == title }
        return fields.toMutableList().apply {
            if (index >= 0) set(index, get(index).copy(value = value))
            else if (value.isNotEmpty()) add(CustomFieldDraft(id = CustomFieldDraft.nextTempId(map { it.id }),
                title = title, value = value, isProtected = protected))
        }
    }
    fun order(fields: List<CustomFieldDraft>): List<String> = fields.firstOrNull { it.title == ORDER }
        ?.value?.split(',')?.filter { it.isNotBlank() }?.distinct().orEmpty()
    fun withOrder(fields: List<CustomFieldDraft>, sections: List<String>): List<CustomFieldDraft> {
        val merged = (sections + order(fields)).distinct()
        if (merged.isEmpty()) return fields
        val existing = fields.firstOrNull { it.title == ORDER }
        val value = merged.joinToString(",")
        return fields.filterNot { it.title == ORDER } + (existing?.copy(value = value)
            ?: CustomFieldDraft(id = CustomFieldDraft.nextTempId(fields.map { it.id }), title = ORDER, value = value))
    }
}
```

## `data/model/ApiKeyEntryFields.kt`

SHA-256: `83319264cb76be17e1163309a654a98997b0d2964e71e1aefc219e00387e061f`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/data/model/ApiKeyEntryFields.kt)

```kotlin
package takagi.ru.monica.data.model

import takagi.ru.monica.data.CustomFieldDraft
import takagi.ru.monica.data.PasswordEntry
import java.net.URI

/** Only the type and endpoint use custom fields. The key uses the encrypted password column. */
object ApiKeyEntryFields {
    const val TYPE = "API_KEY"
    const val MARKER = "monica_api_key_type"
    const val API_URL = "monica_api_key_url"

    fun isApiKey(fields: Map<String, String>): Boolean = fields[MARKER] == TYPE
    fun owns(name: String): Boolean = name == MARKER || name == API_URL

    fun encode(apiUrl: String): List<CustomFieldDraft> = buildList {
        add(CustomFieldDraft(title = MARKER, value = TYPE))
        if (apiUrl.isNotBlank()) add(CustomFieldDraft(title = API_URL, value = apiUrl.trim()))
    }

    fun isValidOptionalUrl(value: String): Boolean {
        val text = value.trim()
        if (text.isEmpty()) return true
        if (text.length > 2048 || text.any { it.isISOControl() }) return false
        return runCatching {
            val uri = URI(text)
            uri.scheme.lowercase() in setOf("http", "https") &&
                !uri.host.isNullOrBlank() && uri.rawUserInfo == null &&
                (uri.port == -1 || uri.port in 1..65535)
        }.getOrDefault(false)
    }
}

data class ApiKeyDraft(
    val provider: String = "",
    val website: String = "",
    val key: String = "",
    val apiUrl: String = "",
    val notes: String = "",
) {
    // Prevent accidental logging of a secret-bearing draft.
    override fun toString(): String = "ApiKeyDraft(redacted)"

    val isValid: Boolean get() = provider.isNotBlank() && key.isNotBlank() &&
        ApiKeyEntryFields.isValidOptionalUrl(website) && ApiKeyEntryFields.isValidOptionalUrl(apiUrl)

    fun toEntry(original: PasswordEntry? = null): PasswordEntry =
        (original ?: PasswordEntry(title = "", website = "", username = "", password = "")).copy(
            title = provider.trim(), website = website.trim(), password = key,
            notes = notes, loginType = ApiKeyEntryFields.TYPE,
        )

    fun customFields(existing: List<CustomFieldDraft>): List<CustomFieldDraft> =
        existing.filterNot { ApiKeyEntryFields.owns(it.title) } + ApiKeyEntryFields.encode(apiUrl)

    companion object {
        fun from(entry: PasswordEntry, fields: Map<String, String>) = ApiKeyDraft(
            provider = entry.title, website = entry.website, key = entry.password,
            apiUrl = fields[ApiKeyEntryFields.API_URL].orEmpty(), notes = entry.notes,
        )
    }
}
```

## `data/model/GpgEntryFields.kt`

SHA-256: `4f193536e018acb77b09994ab606a16dfc6038ad920d2095370fd14105501427`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/data/model/GpgEntryFields.kt)

```kotlin
package takagi.ru.monica.data.model

import takagi.ru.monica.data.CustomFieldDraft
import takagi.ru.monica.utils.GpgKeyGenerator
import java.util.Base64

/** The secret uses PasswordEntry.password (encrypted at rest); only public data uses custom fields.
 * Chunking keeps RSA certificates below Bitwarden's per-field length limit.
 * Existing backup, KeePass and MDBX field transports preserve this representation.
 */
object GpgEntryFields {
    const val TYPE = "GPG_KEY"
    const val MARKER = "monica_gpg_type"
    const val PUBLIC_PREFIX = "monica_gpg_public_"
    const val FINGERPRINT = "monica_gpg_fingerprint"
    const val USER_ID = "monica_gpg_user_id"
    private const val ENCODING = "monica_gpg_encoding"
    fun isGpg(fields: Map<String, String>) = fields[MARKER] == TYPE
    fun owns(name: String) = name == MARKER || name == FINGERPRINT || name == USER_ID || name == ENCODING || name.startsWith(PUBLIC_PREFIX)
    fun encode(key: GpgKeyGenerator.Key): List<CustomFieldDraft> = buildList {
        add(CustomFieldDraft(title = MARKER, value = TYPE))
        add(CustomFieldDraft(title = FINGERPRINT, value = key.fingerprint))
        add(CustomFieldDraft(title = USER_ID, value = key.userId))
        // Storage adapters trim field edges. Encoding prevents a chunk boundary from losing
        // an armor line break (including the line break before the CRC).
        add(CustomFieldDraft(title = ENCODING, value = "base64"))
        Base64.getEncoder().encodeToString(key.publicKey.toByteArray(Charsets.UTF_8)).chunked(2000).forEachIndexed { i, part ->
            add(CustomFieldDraft(title = PUBLIC_PREFIX + i.toString().padStart(4, '0'), value = part))
        }
    }
    fun publicKey(fields: Map<String, String>): String {
        val chunks = fields.filterKeys { it.startsWith(PUBLIC_PREFIX) }.toSortedMap()
        val maxEncodedSize = (GpgKeyGenerator.MAX_IMPORT_BYTES + 2) / 3 * 4
        require(chunks.isNotEmpty() && chunks.size <= (maxEncodedSize + 1999) / 2000)
        chunks.keys.forEachIndexed { index, name -> require(name == PUBLIC_PREFIX + index.toString().padStart(4, '0')) }
        val stored = chunks.values.joinToString("")
        require(stored.length <= maxEncodedSize)
        val bytes = when (fields[ENCODING]) {
            "base64" -> Base64.getDecoder().decode(stored)
            null -> stored.toByteArray(Charsets.UTF_8) // Earlier unencoded field representation.
            else -> error("Unsupported GPG field encoding")
        }
        require(bytes.size <= GpgKeyGenerator.MAX_IMPORT_BYTES)
        return bytes.toString(Charsets.UTF_8)
    }
}
```

## `data/ApiTokenPayload.kt`

SHA-256: `0adc9de5e9a17df1635d61e2dce341a3dc6408ffb21a290e537a7fc2a3ba98df`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/data/ApiTokenPayload.kt)

```kotlin
package takagi.ru.monica.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.net.URI

/** Native CLI payload. Keep unknown extensions when editing a supported schema. */
object ApiTokenPayload {
    const val NATIVE_TYPE = "api-token"
    const val SCHEMA = "monica.gateway.credential.v1"
    const val APP_SCHEMA = "monica.api-token.v1"
    const val MAX_BYTES = 16 * 1024

    fun decode(value: String): JsonObject? =
        value.takeIf { it.toByteArray(Charsets.UTF_8).size <= MAX_BYTES }?.let(::decodeDraft)

    /** Drafts remain editable when validation fails, including an oversized field. */
    fun decodeDraft(value: String): JsonObject? = runCatching {
        val fields = Json.parseToJsonElement(value) as? JsonObject ?: return null
        if (text(fields, "schema") !in setOf(SCHEMA, APP_SCHEMA)) return null
        if (listOf("provider", "api_base", "token").any {
                (fields[it] as? JsonPrimitive)?.isString != true
            }) return null
        if (fields.containsKey("note") && (fields["note"] as? JsonPrimitive)?.isString != true) return null
        fields
    }.getOrNull()

    fun text(fields: JsonObject?, key: String): String =
        (fields?.get(key) as? JsonPrimitive)?.content.orEmpty()

    fun update(value: String, key: String, text: String): String {
        // An unfinished edit may exceed validation limits. Keep its other fields
        // so correcting that input never silently discards extensions or secrets.
        val current = runCatching { Json.parseToJsonElement(value) as? JsonObject }.getOrNull()
            ?.takeIf { ApiTokenPayload.text(it, "schema") in setOf(SCHEMA, APP_SCHEMA) } ?: return value
        val fields = current.toMutableMap()
        fields[key] = JsonPrimitive(text)
        return JsonObject(fields).toString()
    }

    fun empty(): JsonObject = JsonObject(mapOf(
        "schema" to JsonPrimitive(SCHEMA),
        "provider" to JsonPrimitive("gitlab"),
        "api_base" to JsonPrimitive("https://gitlab.com/api/v4/"),
        "note" to JsonPrimitive(""),
        "token" to JsonPrimitive("")
    ))

    fun isValid(value: String): Boolean {
        val fields = decode(value) ?: return false
        if (text(fields, "schema") == APP_SCHEMA) return isValidForStorage(value)
        val token = text(fields, "token")
        val uri = runCatching { URI(text(fields, "api_base")) }.getOrNull() ?: return false
        val provider = text(fields, "provider")
        val note = text(fields, "note")
        val validPath = when (provider) {
            "gitlab" -> uri.rawPath == "/api/v4/"
            "github" -> uri.rawPath in setOf("", "/", "/api/v3/")
            else -> false
        }
        return validPath && text(fields, "api_base").toByteArray(Charsets.UTF_8).size <= 2048 &&
            uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.rawUserInfo == null &&
            uri.rawQuery == null && uri.rawFragment == null &&
            token.length in 16..4096 && token.all { it.code in 33..126 } &&
            note.toByteArray(Charsets.UTF_8).size <= 1024 &&
            note.none { it.isISOControl() || it in '\u202a'..'\u202e' || it in '\u2066'..'\u2069' } &&
            !note.contains(token) && !text(fields, "api_base").contains(token)
    }

    fun isValidName(value: String): Boolean = value.length in 1..64 &&
        value.all { it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' || it == '-' || it == '_' }

    fun isValidStorageName(value: String): Boolean = value.isNotBlank() && value.length <= 256 &&
        value.none { it.isISOControl() }

    fun isValidForStorage(value: String): Boolean {
        val fields = decode(value) ?: return false
        val provider = text(fields, "provider")
        val token = text(fields, "token")
        if (provider.isBlank() || provider.length > 128 || provider.any { it.isISOControl() } || token.isBlank()) return false
        val endpoint = text(fields, "api_base")
        if (endpoint.isBlank()) return true
        val uri = runCatching { URI(endpoint) }.getOrNull() ?: return false
        return endpoint.length <= 2048 && uri.scheme?.lowercase() in setOf("http", "https") &&
            !uri.host.isNullOrBlank() && uri.rawUserInfo == null && (token.length < 16 || !endpoint.contains(token))
    }

    /** Retain gateway compatibility unless the user chooses general-purpose fields. */
    fun forStorage(value: String, title: String): String =
        if (text(decode(value), "schema") == SCHEMA && (!isValid(value) || !isValidName(title)))
            update(value, "schema", APP_SCHEMA) else value
}

data class NativeApiTokenSummary(
    val databaseId: Long,
    val entryId: String,
    val collectionId: String,
    val collectionTitle: String,
    val title: String,
    val ancestorCollectionIds: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val updatedAt: Long = 0L,
    val isRootCollection: Boolean = false,
) {
    val key: String = "native-token:$databaseId:$entryId"
    // Negative IDs are presentation-only and never enter Room's password table.
    val displayId: Long = java.nio.ByteBuffer.wrap(java.security.MessageDigest.getInstance("SHA-256")
        .digest(key.toByteArray(Charsets.UTF_8))).long or Long.MIN_VALUE

    fun asPasswordCard(typeLabel: String): PasswordEntry = PasswordEntry(
        id = displayId, title = title, username = typeLabel, website = "", password = "",
        createdAt = java.util.Date(updatedAt), updatedAt = java.util.Date(updatedAt),
        isFavorite = isFavorite, mdbxDatabaseId = databaseId, mdbxFolderId = collectionId.takeUnless { isRootCollection },
        loginType = "API_TOKEN",
    )
}

// Deliberately no generated toString(): payloads must never appear in diagnostic output.
class NativeApiToken(val summary: NativeApiTokenSummary, val payload: String, val extras: NativeApiTokenExtras? = null)
```

## `ui/components/EntrySupplementalFields.kt`

SHA-256: `02d620893a0d152a598c51f1bb0674e5fccc34953d229975eba9b00f99694129`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/ui/components/EntrySupplementalFields.kt)

```kotlin
package takagi.ru.monica.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import takagi.ru.monica.R
import takagi.ru.monica.data.CustomFieldDraft
import takagi.ru.monica.data.model.EntryContentFields

data class EntrySupplementalSpec(val key: String, @StringRes val label: Int = 0,
    val literal: String = "", val protected: Boolean = false, val legacyOnly: Boolean = false)

object EntrySupplementalSpecs {
    val payment = listOf(
        EntrySupplementalSpec("bankName", R.string.bank_name),
        EntrySupplementalSpec("cardType", R.string.card_type),
        EntrySupplementalSpec("brand", R.string.bank_card_brand_label, legacyOnly = true),
        EntrySupplementalSpec("nickname", R.string.bank_card_nickname_label, legacyOnly = true),
        EntrySupplementalSpec("validFromMonth", R.string.bank_card_valid_from_month),
        EntrySupplementalSpec("validFromYear", R.string.bank_card_valid_from_year),
        EntrySupplementalSpec("pin", R.string.bank_card_pin_label, protected = true),
        EntrySupplementalSpec("iban", literal = "IBAN", protected = true),
        EntrySupplementalSpec("swiftBic", literal = "SWIFT / BIC"),
        EntrySupplementalSpec("routingNumber", R.string.bank_card_routing_number_label),
        EntrySupplementalSpec("accountNumber", R.string.bank_card_account_number_label, protected = true),
        EntrySupplementalSpec("branchCode", R.string.bank_card_branch_code_label),
        EntrySupplementalSpec("currency", R.string.bank_card_currency_label),
        EntrySupplementalSpec("customerServicePhone", R.string.bank_card_customer_service_phone_label),
        EntrySupplementalSpec("billingAddress", R.string.billing_address),
    )
    val contact = listOf(
        EntrySupplementalSpec("fullName", R.string.full_name),
        EntrySupplementalSpec("title", R.string.document_title_prefix_label),
        EntrySupplementalSpec("firstName", R.string.document_first_name_label),
        EntrySupplementalSpec("middleName", R.string.document_middle_name_label),
        EntrySupplementalSpec("lastName", R.string.document_last_name_label),
        EntrySupplementalSpec("company", R.string.document_company_label),
        EntrySupplementalSpec("documentType", R.string.document_type),
        EntrySupplementalSpec("documentNumber", R.string.document_number, protected = true),
        EntrySupplementalSpec("issuedDate", R.string.issued_date),
        EntrySupplementalSpec("expiryDate", R.string.expiry_date),
        EntrySupplementalSpec("issuedBy", R.string.issued_by),
        EntrySupplementalSpec("nationality", R.string.nationality),
        EntrySupplementalSpec("ssn", R.string.document_ssn_label, protected = true),
        EntrySupplementalSpec("passportNumber", R.string.document_passport_number_label, protected = true),
        EntrySupplementalSpec("licenseNumber", R.string.document_license_number_label, protected = true),
        EntrySupplementalSpec("additionalInfo", R.string.document_additional_info_label),
    )
    val address = listOf(
        EntrySupplementalSpec("fullName", R.string.full_name),
        EntrySupplementalSpec("company", R.string.document_company_label),
        EntrySupplementalSpec("apartment", R.string.apartment),
        EntrySupplementalSpec("address3", R.string.document_address_line_3),
        EntrySupplementalSpec("phone", R.string.phone),
        EntrySupplementalSpec("email", R.string.email),
    )
    fun forSection(section: String) = when (section) { "PAYMENT" -> payment; "CONTACT" -> contact; "ADDRESS" -> address; else -> emptyList() }
    fun spec(title: String): EntrySupplementalSpec? = listOf("PAYMENT", "CONTACT", "ADDRESS").firstNotNullOfOrNull { section ->
        forSection(section).firstOrNull { EntryContentFields.key(section, it.key) == title }
    }
}

@Composable
fun EntrySupplementalFields(section: String, fields: List<CustomFieldDraft>, onFields: (List<CustomFieldDraft>) -> Unit) {
    val specs = EntrySupplementalSpecs.forSection(section)
    EntryOptionalFields(specs, specs.associate { it.key to EntryContentFields.value(fields, section, it.key) },
        onValue = { spec, value -> onFields(EntryContentFields.update(fields, section, spec.key, value, spec.protected)) })
}

/** Empty rare fields stay in the picker. Existing legacy values always remain editable. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryOptionalFields(specs: List<EntrySupplementalSpec>, values: Map<String, String>,
    onValue: (EntrySupplementalSpec, String) -> Unit) {
    var added by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var picker by remember { mutableStateOf(false) }
    val visible = specs.filter { it.key in added || !values[it.key].isNullOrEmpty() }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        visible.forEach { spec ->
            var revealed by remember(spec.key) { mutableStateOf(false) }
            val title = if (spec.label != 0) stringResource(spec.label) else spec.literal
            val choices = when (spec.key) {
                "cardType" -> listOf("CREDIT" to R.string.credit_card, "DEBIT" to R.string.debit_card, "PREPAID" to R.string.prepaid_card)
                "documentType" -> listOf("ID_CARD" to R.string.id_card, "PASSPORT" to R.string.passport,
                    "DRIVER_LICENSE" to R.string.drivers_license, "SOCIAL_SECURITY" to R.string.social_security_card, "OTHER" to R.string.other_document)
                else -> emptyList()
            }
            var choosing by remember { mutableStateOf(false) }
            if (choices.isNotEmpty()) {
                OutlinedButton(onClick = { choosing = true }, modifier = Modifier.fillMaxWidth().testTag("entry_extra_${spec.key}")) {
                    Text(title + ": " + (choices.firstOrNull { it.first == values[spec.key] }?.let { stringResource(it.second) }
                        ?: values[spec.key].orEmpty()))
                }
                if (choosing) AlertDialog(onDismissRequest = { choosing = false }, title = { Text(title) },
                    text = { Column { choices.forEach { (key, label) ->
                        TextButton(onClick = { onValue(spec, key); choosing = false }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(label))
                        }
                    } } }, confirmButton = { TextButton(onClick = { choosing = false }) { Text(stringResource(R.string.cancel)) } })
            } else OutlinedTextField(values[spec.key].orEmpty(), { onValue(spec, it) },
                label = { Text(title) }, entryContentStyle = true,
                visualTransformation = if (spec.protected && !revealed) PasswordVisualTransformation() else VisualTransformation.None,
                trailingIcon = if (spec.protected) {{ IconButton(onClick = { revealed = !revealed },
                    modifier = Modifier.testTag("entry_extra_reveal_${spec.key}")) {
                    Icon(if (revealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        stringResource(if (revealed) R.string.hide_password else R.string.show_password))
                } }} else null,
                modifier = Modifier.fillMaxWidth().testTag("entry_extra_${spec.key}"))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            FilledTonalButton(onClick = { picker = true }, modifier = Modifier.testTag("entry_extra_add")) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.keepass_native_add_field))
            }
        }
    }
    if (picker) ModalBottomSheet(onDismissRequest = { picker = false }) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(12.dp)) {
            Text(stringResource(R.string.keepass_native_add_field), style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(12.dp))
            specs.filter { !it.legacyOnly && it !in visible }.forEach { spec ->
                ListItem(headlineContent = { Text(if (spec.label != 0) stringResource(spec.label) else spec.literal) },
                    modifier = Modifier.testTag("entry_extra_choose_${spec.key}").clickable {
                        added = added + spec.key; picker = false
                    })
            }
        }
    }
}
```

## `ui/components/PasswordContentMenu.kt`

SHA-256: `eea47a52cfec0f024dfcb204facbcb972e0d57ee83998e8bdfbed153b9333015`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/ui/components/PasswordContentMenu.kt)

```kotlin
enum class PasswordContentSection(@StringRes val title: Int, @StringRes val description: Int, val icon: ImageVector) {
    NOTES(R.string.password_content_notes, R.string.password_content_notes_description, Icons.Default.Description),
    AUTHENTICATOR(R.string.section_security_verification, R.string.password_content_otp_description, Icons.Default.Shield),
    PAYMENT(R.string.payment_info, R.string.password_content_payment_description, Icons.Default.CreditCard),
    CONTACT(R.string.personal_info, R.string.password_content_contact_description, Icons.Default.Person),
    ADDRESS(R.string.address_info, R.string.password_content_address_description, Icons.Default.Home),
    CUSTOM_FIELDS(R.string.custom_fields, R.string.password_content_fields_description, Icons.Default.List),
    ATTACHMENTS(R.string.attachments, R.string.password_content_attachments_description, Icons.Default.AttachFile),
}
```

## `repository/Mdbx2Repository.kt`

SHA-256: `9daf216d58dde9bb37283cfeb5dc15677b9f730f5c8a674e4c88fdaf9b52ac53`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/repository/Mdbx2Repository.kt)

参照 `passwordMutation`、`secureItemMutation`、`passkeyMutation`、`passwordCustomFieldsPayload` 和对应读取/合并/标签路径；主文表格描述本次写入字段。

## `repository/MdbxPasswordContentFields.kt`

SHA-256: `c78d4ac4620fdd245930276c5cf921d91f64de79ecdedbaed3472c44fabd81da`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/repository/MdbxPasswordContentFields.kt)

```kotlin
package takagi.ru.monica.repository

import org.json.JSONObject
import takagi.ru.monica.data.PasswordEntry

/** Existing password content stored inside the encrypted MDBX record, without a schema change. */
internal object MdbxPasswordContentFields {
    fun writeTo(payload: JSONObject, entry: PasswordEntry, decryptSensitive: (String) -> String): JSONObject =
        payload.put("email", entry.email)
            .put("phone", entry.phone)
            .put("address_line", entry.addressLine)
            .put("city", entry.city)
            .put("state", entry.state)
            .put("zip_code", entry.zipCode)
            .put("country", entry.country)
            .put("credit_card_number_plain", decryptSensitive(entry.creditCardNumber))
            .put("credit_card_holder", entry.creditCardHolder)
            .put("credit_card_expiry", entry.creditCardExpiry)
            .put("credit_card_cvv_plain", decryptSensitive(entry.creditCardCVV))

    fun readInto(payload: JSONObject, entry: PasswordEntry, previous: PasswordEntry? = null): PasswordEntry {
        val fallback = previous ?: entry
        // Missing keys from an older writer preserve the existing projection. Explicit empty
        // strings/nulls clear a value. Card fields follow the readable editor/KDBX model.
        fun value(key: String, old: String): String =
            if (!payload.has(key)) old else if (payload.isNull(key)) "" else payload.getString(key)
        return entry.copy(
            passwordGroupId = if (payload.has("password_group_id"))
                payload.optString("password_group_id").takeIf { !payload.isNull("password_group_id") && it.isNotBlank() }
                else fallback.passwordGroupId,
            email = value("email", fallback.email), phone = value("phone", fallback.phone),
            addressLine = value("address_line", fallback.addressLine), city = value("city", fallback.city),
            state = value("state", fallback.state), zipCode = value("zip_code", fallback.zipCode),
            country = value("country", fallback.country),
            creditCardNumber = value("credit_card_number_plain", fallback.creditCardNumber),
            creditCardHolder = value("credit_card_holder", fallback.creditCardHolder),
            creditCardExpiry = value("credit_card_expiry", fallback.creditCardExpiry),
            creditCardCVV = value("credit_card_cvv_plain", fallback.creditCardCVV),
        )
    }
}
```

## `repository/MdbxAttachmentCekPayload.kt`

SHA-256: `03226b0c4053a2e2a27cb2c0141bffe70a1f11bd09fbda04c0b85da636e558cd`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/repository/MdbxAttachmentCekPayload.kt)

```kotlin
package takagi.ru.monica.repository

import java.util.Base64

/**
 * Portable representation of an attachment CEK inside an MDBX vault.
 *
 * Room stores Attachment.wrappedCek with the current device SecurityManager. That value cannot
 * be used by another device. MDBX must therefore store the CEK in a vault-portable form and let
 * each importing device wrap it again for its local Room database.
 */
object MdbxAttachmentCekPayload {
    private const val PREFIX = "portable-attachment-cek-v1:"
    private const val CEK_SIZE_BYTES = 32

    fun fromLocalWrappedCek(
        wrappedCek: String,
        unwrapToBase64: (String) -> String
    ): String = PREFIX + unwrapToBase64(wrappedCek)

    fun toLocalWrappedCek(
        storedValue: String,
        wrapBase64: (String) -> String
    ): String {
        val trimmed = storedValue.trim()
        val portableBase64 = when {
            trimmed.startsWith(PREFIX) -> trimmed.removePrefix(PREFIX)
            looksLikeRawCekBase64(trimmed) -> trimmed
            else -> return trimmed
        }
        return wrapBase64(portableBase64)
    }

    fun isPortable(storedValue: String): Boolean =
        storedValue.trim().startsWith(PREFIX)

    private fun looksLikeRawCekBase64(value: String): Boolean {
        if (value.isBlank()) return false
        return runCatching {
            Base64.getDecoder().decode(value).size == CEK_SIZE_BYTES
        }.getOrDefault(false)
    }
}
```

## `data/CustomField.kt`

SHA-256: `8752611ecbe7c919325d5ad321ef8951d7e9ec7a93be6819ef11ff8ded0b8221`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/data/CustomField.kt)

```kotlin
package takagi.ru.monica.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 自定义字段实体
 *
 * 每个密码条目可以拥有多个自定义字段，用于存储额外的键值对信息。
 * 使用外键关联到 PasswordEntry，支持级联删除。
 *
 * @property id 自定义字段的唯一标识符
 * @property entryId 关联的密码条目ID
 * @property title 字段名称（如："安全问题"、"备用邮箱"）
 * @property value 字段值（支持任意文本，包括特殊字符和表情）
 * @property isProtected 是否为敏感数据（为true时UI默认隐藏内容，复制时标记为敏感剪贴板）
 * @property sortOrder 排序顺序（用于保持用户自定义的显示顺序）
 */
@Entity(
    tableName = "custom_fields",
    foreignKeys = [
        ForeignKey(
            entity = PasswordEntry::class,
            parentColumns = ["id"],
            childColumns = ["entry_id"],
            onDelete = ForeignKey.CASCADE  // 删除密码条目时级联删除关联的自定义字段
        )
    ],
    indices = [
        Index(value = ["entry_id"])  // 为外键创建索引，提升查询性能
    ]
)
data class CustomField(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "entry_id")
    val entryId: Long,

    val title: String,

    val value: String,

    @ColumnInfo(name = "is_protected", defaultValue = "0")
    val isProtected: Boolean = false,

    @ColumnInfo(name = "sort_order", defaultValue = "0")
    val sortOrder: Int = 0
) {
    /**
     * 检查字段是否有效（标题不为空）
     */
    fun isValid(): Boolean = title.isNotBlank()

    /**
     * 生成用于同步 Hash 的排序键
     * 确保顺序无关的 Hash 计算
     */
    fun toHashKey(): String = "$title:$value:$isProtected"

    companion object {
        /**
         * 创建一个新的自定义字段（用于 UI 编辑时的临时对象）
         */
        fun create(entryId: Long, title: String, value: String, isProtected: Boolean = false, sortOrder: Int = 0): CustomField {
            return CustomField(
                id = 0,  // 新建时ID为0，保存时自动生成
                entryId = entryId,
                title = title,
                value = value,
                isProtected = isProtected,
                sortOrder = sortOrder
            )
        }

        /**
         * 从 KeePass StringField 创建自定义字段
         */
        fun fromKeePassStringField(entryId: Long, key: String, value: String, isProtected: Boolean): CustomField {
            return CustomField(
                id = 0,
                entryId = entryId,
                title = key,
                value = value,
                isProtected = isProtected,
                sortOrder = 0
            )
        }
    }
}

/**
 * 用于 UI 编辑时的临时自定义字段状态
 * 不包含 entryId，便于在创建新条目时使用
 */
data class CustomFieldDraft(
    val id: Long = 0,  // 临时ID，仅用于 UI 区分
    val title: String = "",
    val value: String = "",
    val isProtected: Boolean = false,
    val isPreset: Boolean = false,      // 是否为预设字段（来自设置中的预设模板）
    val isRequired: Boolean = false,    // 是否必填
    val presetId: String? = null,       // 关联的预设字段ID
    val placeholder: String = "",       // 占位提示
    // Retain the type of secure-item fields while using the shared editor.
    val secureFieldType: takagi.ru.monica.data.model.SecureCustomFieldType? = null
) {
    /**
     * 转换为 CustomField 实体
     */
    fun toCustomField(entryId: Long, sortOrder: Int): CustomField {
        return CustomField(
            id = 0,  // 保存时生成新ID
            entryId = entryId,
            title = title,
            value = value,
            isProtected = isProtected,
            sortOrder = sortOrder
        )
    }

    /**
     * 检查是否为有效字段（至少有标题）
     */
    fun isValid(): Boolean = title.isNotBlank()

    /**
     * 检查字段是否应该保存到条目。
     * 用户未填写内容时不应保存成详情页里的空白词条。
     */
    fun shouldPersist(): Boolean {
        return title.isNotBlank() && value.isNotBlank()
    }

    /**
     * 检查是否为空字段（标题和值都为空）
     */
    fun isEmpty(): Boolean = title.isBlank() && value.isBlank()

    /**
     * 检查必填字段是否已填写
     */
    fun isFilled(): Boolean = !isRequired || value.isNotBlank()

    companion object {
        private var tempIdCounter = -1L

        /**
         * 生成下一个临时ID（用于 UI 列表的 key）
         */
        fun nextTempId(existingIds: Collection<Long> = emptyList()): Long {
            while (tempIdCounter in existingIds) tempIdCounter--
            return tempIdCounter--
        }

        /**
         * 从 CustomField 创建 Draft
         */
        fun fromCustomField(field: CustomField): CustomFieldDraft {
            return CustomFieldDraft(
                id = field.id,
                title = field.title,
                value = field.value,
                isProtected = field.isProtected,
                isPreset = false,
                isRequired = false,
                presetId = null,
                placeholder = ""
            )
        }

        /**
         * 从预设字段创建 Draft
         */
        fun fromPreset(preset: PresetCustomField): CustomFieldDraft {
            return CustomFieldDraft(
                id = nextTempId(),
                title = preset.fieldName.trim(),
                value = preset.defaultValue,
                isProtected = preset.isSensitive,
                isPreset = true,
                isRequired = preset.isRequired,
                presetId = preset.id,
                placeholder = preset.placeholder
            )
        }
    }
}
```

## `data/PasswordHistoryEntry.kt`

SHA-256: `a8cbe8e224e108d5bee03068bb8dfdad9d7e40f74d414eedb8a8a316f73ed558`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/data/PasswordHistoryEntry.kt)

```kotlin
package takagi.ru.monica.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Historical password snapshot for a password entry.
 *
 * The password value is stored encrypted, following the same rule as the
 * current password field on [PasswordEntry].
 */
@Entity(
    tableName = "password_history_entries",
    foreignKeys = [
        ForeignKey(
            entity = PasswordEntry::class,
            parentColumns = ["id"],
            childColumns = ["entry_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["entry_id"]),
        Index(value = ["entry_id", "last_used_at"])
    ]
)
data class PasswordHistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "entry_id")
    val entryId: Long,
    val password: String,
    @ColumnInfo(name = "last_used_at")
    val lastUsedAt: Date = Date()
)
```

## `data/model/PasskeyBinding.kt`

SHA-256: `f0369d7bc46257a448506bfd5d2ae65302a49b1aab9f3f8ad1b5361464017d2c`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/data/model/PasskeyBinding.kt)

```kotlin
package takagi.ru.monica.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PasskeyBinding(
    val credentialId: String = "",
    val rpId: String = "",
    val rpName: String = "",
    val userName: String = "",
    val userDisplayName: String = ""
)

object PasskeyBindingCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun decodeList(raw: String): MutableList<PasskeyBinding> {
        if (raw.isBlank()) return mutableListOf()
        return try {
            json.decodeFromString(raw)
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun encodeList(list: List<PasskeyBinding>): String {
        return try {
            json.encodeToString(list)
        } catch (e: Exception) {
            ""
        }
    }

    fun addBinding(raw: String, binding: PasskeyBinding): String {
        val list = decodeList(raw)
        val existingIndex = list.indexOfFirst { it.credentialId == binding.credentialId && it.credentialId.isNotBlank() }
        if (existingIndex >= 0) {
            list[existingIndex] = binding
        } else {
            list.add(binding)
        }
        return encodeList(list)
    }

    fun removeBinding(raw: String, credentialId: String): String {
        if (credentialId.isBlank()) return raw
        val list = decodeList(raw).filterNot { it.credentialId == credentialId }
        return encodeList(list)
    }
}
```

## `attachments/model/Attachment.kt`

SHA-256: `af13dc81274997ce97cdfe38e06dcb35eed4dd6f99d95bcde814925a24a1bc78`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/attachments/model/Attachment.kt)

```kotlin
package takagi.ru.monica.attachments.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import takagi.ru.monica.data.PasswordEntry
import takagi.ru.monica.data.SecureItem

/**
 * 附件元数据。
 *
 * 一条 [Attachment] 始终挂在一个 [PasswordEntry] 或 [SecureItem] 之下：
 * - 永久删除所属条目时 CASCADE 清除附件元数据；
 * - 软删除/恢复通过应用层事务同步 [isDeleted]/[deletedAt]；
 * - 不强制 [fileName] 唯一，同名文件可共存。
 *
 * 字段语义见各个 @ColumnInfo 上的注释，[source] 决定了 [localPath]、[bitwardenAttachmentId]、
 * [keepassBinaryRef] 中哪些字段会被填充：
 *
 * | source      | localPath | bitwardenAttachmentId | bitwardenUrl | bitwardenFileKeyEnc | keepassBinaryRef |
 * |-------------|-----------|-----------------------|--------------|---------------------|------------------|
 * | LOCAL       | 非空      | null                  | null         | null                | null             |
 * | BITWARDEN   | 可空*     | 非空                  | 非空         | 非空                | null             |
 * | KEEPASS     | 可空*     | null                  | null         | null                | 非空             |
 *
 * *（BITWARDEN/KEEPASS 的 localPath 在 DOWNLOADED 状态下指向本地缓存密文，PENDING 时为 null。）
 */
@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = PasswordEntry::class,
            parentColumns = ["id"],
            childColumns = ["parent_password_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SecureItem::class,
            parentColumns = ["id"],
            childColumns = ["parent_secure_item_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["parent_password_id"], name = "index_attachments_parent"),
        Index(value = ["parent_secure_item_id"], name = "index_attachments_secure_item_parent"),
        Index(value = ["source"], name = "index_attachments_source"),
        Index(value = ["bitwarden_attachment_id"], name = "index_attachments_bw_id"),
        Index(value = ["keepass_binary_ref"], name = "index_attachments_kp_ref")
    ]
)
data class Attachment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 所属密码的数据库 id；与 [parentSecureItemId] 二选一。 */
    @ColumnInfo(name = "parent_password_id")
    val parentPasswordId: Long? = null,

    /** 所属安全项的数据库 id；与 [parentPasswordId] 二选一。 */
    @ColumnInfo(name = "parent_secure_item_id")
    val parentSecureItemId: Long? = null,

    /** [AttachmentSource] 的 DB 存储形式（字符串，与枚举 name 对应）。 */
    @ColumnInfo(name = "source")
    val source: String,

    /** 用户可见的文件名（用于展示 / 分享）。不进日志。 */
    @ColumnInfo(name = "file_name")
    val fileName: String,

    /** MIME 类型，如 `image/png`、`application/pdf`。无法识别时写 `application/octet-stream`。 */
    @ColumnInfo(name = "mime_type")
    val mimeType: String,

    /** 原始文件字节数（明文大小，非密文）。 */
    @ColumnInfo(name = "size_bytes")
    val sizeBytes: Long,

    /** 明文 SHA-256 hex。本地附件必填；远端附件在首次下载后回填。 */
    @ColumnInfo(name = "sha256_hex")
    val sha256Hex: String? = null,

    /**
     * 用 Monica 主密钥包裹后的 Attachment_CEK（Base64）。
     * 远端附件在 `PENDING` 状态下为 null，下载并缓存成功后回填。
     */
    @ColumnInfo(name = "wrapped_cek")
    val wrappedCek: String? = null,

    /** 本地密文路径：`filesDir/secure_attachments/<uuid>.enc`。 */
    @ColumnInfo(name = "local_path")
    val localPath: String? = null,

    /** Bitwarden attachment id（服务端 UUID）。 */
    @ColumnInfo(name = "bitwarden_attachment_id")
    val bitwardenAttachmentId: String? = null,

    /** Bitwarden 附件下载 URL（可能是 Azure Blob 直链）。 */
    @ColumnInfo(name = "bitwarden_url")
    val bitwardenUrl: String? = null,

    /**
     * Bitwarden 附件独立密钥（EncString）：使用 cipher key 解包后得到 64 字节的
     * `enc||mac` 用于 AES-CBC-HMAC 解密附件字节。
     */
    @ColumnInfo(name = "bitwarden_file_key_enc")
    val bitwardenFileKeyEnc: String? = null,

    /** KeePass binary pool 的引用键（kotpass BinaryReference.hash 或等价标识）。 */
    @ColumnInfo(name = "keepass_binary_ref")
    val keepassBinaryRef: String? = null,

    /** [AttachmentDownloadState] 的 DB 存储形式（字符串）。 */
    @ColumnInfo(name = "download_state")
    val downloadState: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null
) {
    /** 返回明确区分表类型的所有者；数据库约束保证正常记录只会命中一个分支。 */
    val owner: AttachmentOwner?
        get() = when {
            parentPasswordId != null && parentSecureItemId == null ->
                AttachmentOwner.password(parentPasswordId)
            parentPasswordId == null && parentSecureItemId != null ->
                AttachmentOwner.secureItem(parentSecureItemId)
            else -> null
        }

    /** 便捷访问枚举值。 */
    val sourceEnum: AttachmentSource
        get() = AttachmentSource.fromDbValue(source)

    /** 便捷访问枚举值。 */
    val downloadStateEnum: AttachmentDownloadState
        get() = AttachmentDownloadState.fromDbValue(downloadState)
}
```

## `data/model/PasswordContentBlocks.kt`

SHA-256: `ba0e662f809f576700ff75078a8c97290bfa6b76bc2fbc81ad117989f8ce5d68`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/data/model/PasswordContentBlocks.kt)

```kotlin
package takagi.ru.monica.data.model

import java.security.MessageDigest
import java.util.Base64
import java.util.UUID
import kotlinx.serialization.json.*
import takagi.ru.monica.data.CustomFieldDraft

/** Independent content, carried by the existing encrypted custom-field transports. */
object PasswordContentBlocks {
    const val PREFIX = "monica.content.block."
    private const val CHUNK_SIZE = 1600 // Below Bitwarden's per-field limit, without splitting Unicode.
    private const val MAX_BYTES = 256 * 1024
    enum class Kind { API_KEY, API_TOKEN, SSH_KEY, GPG_KEY, QR_CODE }

    data class Block(val id: String, val raw: JsonObject) {
        val kind: Kind get() = Kind.valueOf(raw.getValue("kind").jsonPrimitive.content)
        val title: String get() = raw.getValue("title").jsonPrimitive.content
        val data: JsonObject get() = raw.getValue("data").jsonObject
        fun value(key: String): String = (data[key] as? JsonPrimitive)?.takeIf { it.isString }?.content.orEmpty()
        fun edited(title: String, patch: Map<String, String>): Block = copy(raw = JsonObject(raw + mapOf(
            "title" to JsonPrimitive(title), "data" to JsonObject(data + patch.mapValues { JsonPrimitive(it.value) }))))
    }
    data class Stored(val token: String, val block: Block?)
    fun token(id: String) = "BLOCK:$id"
    fun owns(title: String) = title.startsWith(PREFIX)
    fun create(kind: Kind): Block {
        val id = UUID.randomUUID().toString()
        return Block(id, buildJsonObject {
            put("version", 1); put("id", id); put("kind", kind.name); put("title", "")
            put("data", buildJsonObject { })
        })
    }

    /** Damaged, duplicate and future blocks remain visible/read-only, never silently replaced. */
    fun read(fields: List<CustomFieldDraft>): List<Stored> = fields.filter { owns(it.title) }
        .map { it.title.removePrefix(PREFIX).substringBefore('.') }.distinct().map { id ->
            Stored(token(id), runCatching {
                require(UUID.fromString(id).toString() == id)
                val header = Json.parseToJsonElement(fields.single { it.title == PREFIX + id }.value).jsonObject
                require(header["version"]?.jsonPrimitive?.int == 1)
                require(header["encoding"]?.jsonPrimitive?.content == "base64")
                val count = header.getValue("parts").jsonPrimitive.int
                require(count in 1..220)
                val encoded = (0 until count).joinToString("") { index ->
                    fields.single { it.title == chunkName(id, index) }.value.also { require(it.length <= CHUNK_SIZE) }
                }
                require(fields.count { it.title.startsWith("$PREFIX$id.") } == count)
                val bytes = Base64.getDecoder().decode(encoded)
                require(bytes.size <= MAX_BYTES && digest(bytes) == header.getValue("sha256").jsonPrimitive.content)
                val raw = Json.parseToJsonElement(Charsets.UTF_8.newDecoder().onMalformedInput(java.nio.charset.CodingErrorAction.REPORT).decode(java.nio.ByteBuffer.wrap(bytes)).toString()).jsonObject
                require(raw["version"]?.jsonPrimitive?.int == 1 && raw["id"]?.jsonPrimitive?.content == id)
                require(raw["title"]?.jsonPrimitive?.isString == true)
                Block(id, raw).also { block ->
                    block.kind; block.data
                    // A future shape for a known editor field must not be overwritten as an empty string.
                    editableKeys(block.kind).forEach { key ->
                        require(block.data[key] == null || (block.data[key] is JsonPrimitive && block.data[key]!!.jsonPrimitive.isString))
                    }
                    if (block.kind == Kind.QR_CODE) {
                        listOf("mode", "templateVersion").forEach { key ->
                            require(block.data[key] == null || (block.data[key] is JsonPrimitive && block.data[key]!!.jsonPrimitive.isString))
                        }
                        require(PasswordQrTemplate.supported(block))
                    }
                }
            }.getOrNull())
        }

    fun put(fields: List<CustomFieldDraft>, block: Block): List<CustomFieldDraft> {
        val existing = read(fields).firstOrNull { it.token == token(block.id) }
        require(existing == null || existing.block != null) { "Unreadable content must be preserved" }
        val bytes = block.raw.toString().toByteArray(Charsets.UTF_8)
        require(bytes.size <= MAX_BYTES) { "Content exceeds 256 KiB" }
        val chunks = Base64.getEncoder().encodeToString(bytes).chunked(CHUNK_SIZE)
        val headerName = PREFIX + block.id
        val oldHeader = fields.firstOrNull { it.title == headerName }?.let { Json.parseToJsonElement(it.value).jsonObject }.orEmpty()
        val header = JsonObject(oldHeader + mapOf("version" to JsonPrimitive(1), "encoding" to JsonPrimitive("base64"),
            "parts" to JsonPrimitive(chunks.size), "sha256" to JsonPrimitive(digest(bytes))))
        val replacement = listOf(headerName to header.toString()) + chunks.mapIndexed { i, text -> chunkName(block.id, i) to text }
        val result = fields.filterNot { it.title == headerName || it.title.startsWith("$headerName.") }.toMutableList()
        replacement.forEach { (title, value) ->
            val old = fields.firstOrNull { it.title == title }
            result += old?.copy(value = value, isProtected = true) ?: CustomFieldDraft(
                id = CustomFieldDraft.nextTempId(result.map { it.id }), title = title, value = value, isProtected = true)
        }
        require(read(result).first { it.token == token(block.id) }.block != null)
        return result
    }
    fun remove(fields: List<CustomFieldDraft>, token: String): List<CustomFieldDraft> {
        val stored = read(fields).single { it.token == token }
        require(stored.block != null) { "Unreadable content must be preserved" }
        val name = PREFIX + stored.block.id
        return fields.filterNot { it.title == name || it.title.startsWith("$name.") }.map {
            if (it.title == EntryContentFields.ORDER) it.copy(value = it.value.split(',').filterNot { key -> key == token }.joinToString(",")) else it
        }
    }
    fun editableKeys(kind: Kind): List<String> = when (kind) {
        Kind.API_KEY -> listOf("key", "url", "notes")
        Kind.API_TOKEN -> listOf("provider", "api_base", "token", "notes")
        Kind.SSH_KEY -> listOf("algorithm", "keySize", "format", "publicKeyOpenSsh", "privateKeyOpenSsh", "fingerprintSha256", "comment", "notes")
        Kind.GPG_KEY -> listOf("publicKey", "privateKey", "fingerprint", "userId", "notes")
        Kind.QR_CODE -> listOf("content", "notes")
    }
    private fun chunkName(id: String, index: Int) = "$PREFIX$id.${index.toString().padStart(4, '0')}"
    private fun digest(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
```

## `data/model/PasswordQrTemplate.kt`

SHA-256: `e9c3141433b469deb7f053fe99358ce89ebcc0f58eaa2370986dda6685baa50b`

[查看源码](../../Monica%20for%20Android/app/src/main/java/takagi/ru/monica/data/model/PasswordQrTemplate.kt)

```kotlin
package takagi.ru.monica.data.model

import java.util.Base64
import takagi.ru.monica.data.PasswordEntry

/** No scripting, recursive expansion or saved resolved secrets. Names travel across databases. */
object PasswordQrTemplate {
    const val WIFI = "WIFI:T:WPA;S:%ACCOUNT%;P:%PASSWORD%;H:false;;"
    val keys = listOf("ACCOUNT", "PASSWORD", "TITLE", "URL", "EMAIL", "PHONE", "NOTES")
    data class Values(val fields: Map<String, String?>, val custom: List<Pair<String, String?>> = emptyList())
    class Invalid(val field: String) : IllegalArgumentException("Unavailable template field")
    fun values(entry: PasswordEntry, password: String?, custom: List<Pair<String, String?>> = emptyList()) = Values(
        mapOf("ACCOUNT" to entry.username, "PASSWORD" to password, "TITLE" to entry.title, "URL" to entry.website,
            "EMAIL" to entry.email, "PHONE" to entry.phone, "NOTES" to entry.notes), custom)
    fun customToken(name: String) = "%FIELD:${Base64.getUrlEncoder().withoutPadding().encodeToString(name.toByteArray(Charsets.UTF_8))}%"
    fun isTemplate(block: PasswordContentBlocks.Block) = block.value("mode") == "template"
    fun supported(block: PasswordContentBlocks.Block): Boolean = when (block.value("mode")) {
        "", "literal" -> true
        "template" -> block.value("templateVersion") == "1"
        else -> false
    }
    fun render(template: String, values: Values): String {
        require(template.toByteArray(Charsets.UTF_8).size <= 256 * 1024)
        val wifi = template.startsWith("WIFI:", ignoreCase = true)
        val result = StringBuilder()
        var offset = 0
        // %% represents a literal percent; values are inserted once, never parsed as templates.
        val tokens = Regex("%%|%([A-Z][A-Z0-9_]*)(?::([^%]*))?%")
        for (match in tokens.findAll(template)) {
            result.append(template, offset, match.range.first)
            if (match.value == "%%") result.append('%') else {
                val key = match.groupValues[1]
                val value = if (key == "FIELD") {
                    val name = runCatching {
                        val bytes = Base64.getUrlDecoder().decode(match.groupValues[2])
                        Charsets.UTF_8.newDecoder().onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                            .decode(java.nio.ByteBuffer.wrap(bytes)).toString()
                    }.getOrElse { throw Invalid("FIELD") }
                    values.custom.filter { it.first == name }.singleOrNull()?.second ?: throw Invalid(name)
                } else {
                    if (match.groupValues[2].isNotEmpty()) throw Invalid(key)
                    values.fields[key] ?: throw Invalid(key)
                }
                result.append(if (wifi) escapeWifi(value) else value)
            }
            require(result.length <= 256 * 1024) { "Rendered template too large" }
            offset = match.range.last + 1
        }
        result.append(template, offset, template.length)
        require(result.toString().toByteArray(Charsets.UTF_8).size <= 256 * 1024)
        return result.toString()
    }
    fun escapeWifi(value: String) = buildString { value.forEach { c ->
        if (c in "\\;,:\"") append('\\')
        append(c)
    } }
    fun resolve(block: PasswordContentBlocks.Block, values: Values): String {
        require(supported(block))
        return if (isTemplate(block)) render(block.value("content"), values) else block.value("content")
    }
}
```

## Unified template editor and billing address (2026-09-30)

Wi-Fi, SSH, API Key and GPG share the password editor’s icon/title, optional content and save pipeline, including notes, complete wallet/note copies, custom fields, attachments and ordering, with matching detail views. Wi-Fi scanning sits below the network name. Embedded API keys/tokens and SSH/GPG keys use full-page editors with shared core fields and key generation/import. Contact and address information now share the complete billing-address editor. Payment and billing-address cards open a separate stack page and individual full details, without duplicate expandable payment rows. Preserve legacy contact/document fields, card artwork and attachments. Native API tokens retain their separate MDBX payload/metadata, with notes, fields, content blocks and Emoji icons; native-token attachments and wallet copies containing assets remain pending.

The storage schema is unchanged: billing addresses use `monica.content.wallet.address` v1, and contact/address projection does not remove legacy fields. Native API-token Emoji icons use the `monica.icon.emoji` custom field in the existing extras metadata, not in the strict gateway payload.
