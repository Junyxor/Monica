package takagi.ru.monica.ui.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Date
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import takagi.ru.monica.data.ItemType
import takagi.ru.monica.data.PasswordEntry
import takagi.ru.monica.data.SecureItem
import takagi.ru.monica.data.UnmatchedIconHandlingStrategy
import takagi.ru.monica.data.model.TotpData
import takagi.ru.monica.ui.components.ExpressiveTopBar
import takagi.ru.monica.ui.components.TotpCodeCard
import takagi.ru.monica.ui.theme.MonicaSpacing
import takagi.ru.monica.ui.main.navigation.BottomNavItem
import takagi.ru.monica.ui.main.navigation.MonicaBottomNavigationBar
import takagi.ru.monica.ui.password.PasswordListSingleCardItem
import takagi.ru.monica.ui.password.StackCardMode
import takagi.ru.monica.ui.password.StackedPasswordGroup
import takagi.ru.monica.data.PasswordCardDisplayField
import takagi.ru.monica.data.PasswordCardDisplayMode

/** Sample vault content shared by every preview scene. */
internal object DesignPreviewData {
    private val updated = Date(1_793_000_000_000L)

    private fun entry(
        id: Long,
        title: String,
        website: String,
        username: String,
        favorite: Boolean = false,
        authenticatorKey: String = "",
    ) = PasswordEntry(
        id = id,
        title = title,
        website = website,
        username = username,
        password = "",
        isFavorite = favorite,
        updatedAt = updated,
        createdAt = updated,
        authenticatorKey = authenticatorKey,
    )

    val github = entry(1, "GitHub", "https://github.com/login", "takagi@monica.dev", favorite = true)
    val googleGroup = listOf(
        entry(2, "Google", "https://accounts.google.com/", "takagi@gmail.com"),
        entry(3, "Google", "https://accounts.google.com/", "monica.work@gmail.com"),
        entry(4, "Google", "https://accounts.google.com/", "family@gmail.com"),
    )
    val steam = entry(5, "Steam", "https://store.steampowered.com/login", "joyin1234")
    val stripe = entry(
        6,
        "Stripe",
        "https://dashboard.stripe.com/login",
        "ops@monica.dev",
        authenticatorKey = "otpauth://totp/Stripe:ops@monica.dev?secret=JBSWY3DPEHPK3PXP&issuer=Stripe"
    )
    val bank = entry(7, "招商银行", "https://cmbchina.com", "6225 •••• 1024")
    val notion = entry(8, "Notion", "https://www.notion.so/login", "takagi@monica.dev")
    val unmatched = entry(9, "家里的路由器", "", "admin")

    private val json = Json { encodeDefaults = true }

    private fun totp(id: Long, issuer: String, account: String, secret: String) = SecureItem(
        id = id,
        itemType = ItemType.TOTP,
        title = issuer,
        itemData = json.encodeToString(TotpData(secret = secret, issuer = issuer, accountName = account)),
        createdAt = updated,
        updatedAt = updated,
    )

    val totpItems = listOf(
        totp(11, "Stripe", "ops@monica.dev", "JBSWY3DPEHPK3PXP"),
        totp(12, "GitHub", "TakagiKuyomi", "GEZDGNBVGY3TQOJQ"),
        totp(13, "Cloudflare", "takagi@monica.dev", "MFRGGZDFMZTWQ2LK"),
        totp(14, "Discord", "joyin1234", "KRSXG5CTMVRXEZLU"),
    )
}

internal object DesignPreviewScenes {

    @Composable
    fun PasswordsPage() {
        PageScaffold(selected = BottomNavItem.Passwords, title = "Monica") {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(MonicaSpacing.ListItemGap)
            ) {
                SinglePassword(DesignPreviewData.github)
                StackedPasswordGroup(
                    passwords = DesignPreviewData.googleGroup,
                    isExpanded = false,
                    stackCardMode = StackCardMode.AUTO,
                    onToggleExpand = {},
                    onPasswordClick = {},
                    onSwipeLeft = {},
                    onSwipeRight = {},
                    onGroupSwipeRight = {},
                    onToggleFavorite = {},
                    onToggleGroupCover = {},
                    isSelectionMode = false,
                    selectedPasswords = emptySet(),
                    onToggleSelection = {},
                    onOpenMultiPasswordDialog = {},
                    onLongClick = {},
                    iconCardsEnabled = true,
                    enableSharedBounds = false,
                )
                SinglePassword(DesignPreviewData.stripe)
                SinglePassword(DesignPreviewData.steam)
                SinglePassword(DesignPreviewData.bank)
                SinglePassword(DesignPreviewData.notion)
            }
        }
    }

    @Composable
    fun AuthenticatorPage() {
        PageScaffold(selected = BottomNavItem.Authenticator, title = "Monica") {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(MonicaSpacing.ListItemGap)
            ) {
                DesignPreviewData.totpItems.forEach { item ->
                    TotpCodeCard(
                        item = item,
                        onCopyCode = {},
                        sharedTickSeconds = PREVIEW_EPOCH_SECONDS,
                        sharedProgressTimeMillis = PREVIEW_EPOCH_SECONDS * 1000,
                    )
                }
            }
        }
    }

    @Composable
    fun ComponentsSheet() {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(MonicaSpacing.ListItemGap)
        ) {
            SinglePassword(DesignPreviewData.github, selectionMode = true, selected = true)
            SinglePassword(DesignPreviewData.steam, selectionMode = true)
            SinglePassword(DesignPreviewData.notion, displayMode = PasswordCardDisplayMode.TITLE_ONLY)
            SinglePassword(DesignPreviewData.unmatched, strategy = UnmatchedIconHandlingStrategy.WEBSITE_OR_TITLE_INITIAL)
            SinglePassword(DesignPreviewData.unmatched, strategy = UnmatchedIconHandlingStrategy.DEFAULT_ICON)
            SinglePassword(
                DesignPreviewData.github,
                fields = listOf(
                    PasswordCardDisplayField.USERNAME,
                    PasswordCardDisplayField.WEBSITE,
                    PasswordCardDisplayField.UPDATED_AT
                )
            )
            StackedPasswordGroup(
                passwords = DesignPreviewData.googleGroup,
                isExpanded = true,
                stackCardMode = StackCardMode.AUTO,
                onToggleExpand = {},
                onPasswordClick = {},
                onSwipeLeft = {},
                onSwipeRight = {},
                onGroupSwipeRight = {},
                onToggleFavorite = {},
                onToggleGroupCover = {},
                isSelectionMode = false,
                selectedPasswords = emptySet(),
                onToggleSelection = {},
                onOpenMultiPasswordDialog = {},
                onLongClick = {},
                iconCardsEnabled = true,
                enableSharedBounds = false,
            )
        }
    }

    @Composable
    private fun SinglePassword(
        entry: PasswordEntry,
        selectionMode: Boolean = false,
        selected: Boolean = false,
        displayMode: PasswordCardDisplayMode = PasswordCardDisplayMode.SHOW_ALL,
        strategy: UnmatchedIconHandlingStrategy = UnmatchedIconHandlingStrategy.DEFAULT_ICON,
        fields: List<PasswordCardDisplayField> = PasswordCardDisplayField.DEFAULT_ORDER,
    ) {
        PasswordListSingleCardItem(
            entry = entry,
            onClick = {},
            onLongClick = {},
            onSwipeLeft = {},
            onSwipeRight = {},
            isSwiped = false,
            isSelectionMode = selectionMode,
            isSelected = selected,
            onToggleFavorite = {},
            unmatchedIconHandlingStrategy = strategy,
            passwordCardDisplayMode = displayMode,
            passwordCardDisplayFields = fields,
            showAuthenticator = true,
            hideOtherContentWhenAuthenticator = true,
            totpTimeOffsetSeconds = 0,
            smoothAuthenticatorProgress = false,
            iconCardsEnabled = true,
            enableSharedBounds = false,
        )
    }

    @Composable
    private fun PageScaffold(
        selected: BottomNavItem,
        title: String,
        content: @Composable () -> Unit
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ExpressiveTopBar(
                title = title,
                searchQuery = "",
                onSearchQueryChange = {},
                isSearchExpanded = false,
                onSearchExpandedChange = {},
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Folder, contentDescription = null) }
                    IconButton(onClick = {}) { Icon(Icons.Default.Search, contentDescription = null) }
                    IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, contentDescription = null) }
                }
            )
            Column(modifier = Modifier.weight(1f).fillMaxWidth()) { content() }
            PreviewNavigationBar(selected)
        }
    }

    /** The default tab set of a fresh install. */
    @Composable
    private fun PreviewNavigationBar(selected: BottomNavItem) {
        MonicaBottomNavigationBar(
            tabs = listOf(
                BottomNavItem.Passwords,
                BottomNavItem.Authenticator,
                BottomNavItem.CardWallet,
                BottomNavItem.Notes,
                BottomNavItem.Passkey,
                BottomNavItem.Settings,
            ),
            selectedKey = selected.key,
            onSelect = {}
        )
    }

    /** A fixed moment so codes and countdowns are identical between runs. */
    const val PREVIEW_EPOCH_SECONDS = 1_793_000_011L
}
