package takagi.ru.monica.ui.password

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import takagi.ru.monica.R
import takagi.ru.monica.data.PasswordCardDisplayField
import takagi.ru.monica.data.PasswordEntry
import takagi.ru.monica.data.primaryLinkedAppPackageName
import takagi.ru.monica.data.UnmatchedIconHandlingStrategy
import takagi.ru.monica.ui.icons.VaultItemIcon
import takagi.ru.monica.ui.theme.MonicaDesign
import takagi.ru.monica.ui.theme.MonicaShapeTokens
import takagi.ru.monica.ui.theme.MonicaSpacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun MultiPasswordEntryCard(
    passwords: List<PasswordEntry>,
    onClick: (PasswordEntry) -> Unit,
    onCardClick: (() -> Unit)? = null,
    onLongClick: () -> Unit = {},
    onToggleFavorite: ((PasswordEntry) -> Unit)? = null,
    onToggleGroupCover: ((PasswordEntry) -> Unit)? = null,
    isSelectionMode: Boolean = false,
    selectedPasswords: Set<Long> = emptySet(),
    canSetGroupCover: Boolean = false,
    hasGroupCover: Boolean = false,
    isInExpandedGroup: Boolean = false,
    iconCardsEnabled: Boolean = false,
    unmatchedIconHandlingStrategy: UnmatchedIconHandlingStrategy = UnmatchedIconHandlingStrategy.DEFAULT_ICON,
    passwordCardDisplayMode: takagi.ru.monica.data.PasswordCardDisplayMode = takagi.ru.monica.data.PasswordCardDisplayMode.SHOW_ALL,
    passwordCardDisplayFields: List<PasswordCardDisplayField> = PasswordCardDisplayField.DEFAULT_ORDER,
    showAuthenticator: Boolean = false,
    hideOtherContentWhenAuthenticator: Boolean = false,
    totpTimeOffsetSeconds: Int = 0,
    smoothAuthenticatorProgress: Boolean = true,
    decryptAuthenticatorKey: ((String) -> String)? = null
) {
    val firstEntry = passwords.first()
    val firstEntryTitle = firstEntry.title.ifBlank { stringResource(R.string.untitled) }
    val cardShape = if (isInExpandedGroup) MonicaShapeTokens.NestedItem else MonicaShapeTokens.Item

    val anySelected = passwords.any { selectedPasswords.contains(it.id) }
    val containerColor = when {
        anySelected -> MaterialTheme.colorScheme.primaryContainer
        isInExpandedGroup -> MonicaDesign.surfaces.nestedItem
        else -> MonicaDesign.surfaces.item
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        color = containerColor,
        contentColor = contentColorFor(containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .testTag("password_project_card")
                .combinedClickable(
                    onClick = { if (isSelectionMode) onClick(firstEntry) else onCardClick?.invoke() },
                    onLongClick = onLongClick
                )
                .padding(
                    horizontal = if (isInExpandedGroup) MonicaSpacing.Medium else MonicaSpacing.ListItemHorizontal,
                    vertical = MonicaSpacing.Medium
                ),
            verticalArrangement = Arrangement.spacedBy(MonicaSpacing.Small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (iconCardsEnabled) {
                    VaultItemIcon(
                        website = firstEntry.website,
                        title = firstEntry.title,
                        appPackageName = firstEntry.primaryLinkedAppPackageName(),
                        customIconType = firstEntry.customIconType,
                        customIconValue = firstEntry.customIconValue,
                        defaultIcon = Icons.Default.Key,
                        unmatchedIconHandlingStrategy = unmatchedIconHandlingStrategy,
                        size = if (isInExpandedGroup) 36.dp else 42.dp,
                        endSpacing = MonicaSpacing.Medium
                    )
                }

                Text(
                    text = firstEntryTitle,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelectionMode) {
                        Checkbox(checked = passwords.all { it.id in selectedPasswords }, onCheckedChange = null)
                    }
                    if (firstEntry.isBitwardenEntry()) {
                        Icon(
                            Icons.Default.CloudSync,
                            contentDescription = "Bitwarden",
                            tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    } else if (firstEntry.isKeePassEntry()) {
                        Icon(
                            Icons.Default.VpnKey,
                            contentDescription = "KeePass",
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (onToggleGroupCover != null && passwords.any { it.isGroupCover }) {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isSelectionMode) {
                                passwords.firstOrNull { it.isGroupCover }?.let { coverEntry ->
                                    IconButton(
                                        onClick = { onToggleGroupCover(coverEntry) },
                                        modifier = Modifier.size(36.dp),
                                        enabled = canSetGroupCover
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = stringResource(R.string.legacy_ui_remove_cover),
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            } else {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = stringResource(R.string.legacy_ui_cover),
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    if (onToggleFavorite != null) {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val allFavorited = passwords.all { it.isFavorite }
                            val anyFavorited = passwords.any { it.isFavorite }
                            if (!isSelectionMode) {
                                IconButton(
                                    onClick = {
                                        passwords.forEach { entry ->
                                            onToggleFavorite(entry)
                                        }
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        if (anyFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = if (allFavorited) stringResource(R.string.remove_from_favorites) else stringResource(R.string.add_to_favorites),
                                        tint = if (anyFavorited) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else if (anyFavorited) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = stringResource(R.string.favorite),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            val authenticatorState = if (showAuthenticator) {
                rememberPasswordAuthenticatorDisplayState(
                    authenticatorKey = firstEntry.authenticatorKey,
                    fallbackIssuer = firstEntry.website.ifBlank { firstEntry.title },
                    fallbackAccountName = firstEntry.username.ifBlank { firstEntry.title },
                    timeOffsetSeconds = totpTimeOffsetSeconds,
                    smoothProgress = smoothAuthenticatorProgress,
                    decryptAuthenticatorKey = decryptAuthenticatorKey
                )
            } else {
                null
            }
            val shouldHideDisplayLines = hideOtherContentWhenAuthenticator && authenticatorState != null
            val displayLines = if (
                passwordCardDisplayMode == takagi.ru.monica.data.PasswordCardDisplayMode.TITLE_ONLY || shouldHideDisplayLines
            ) {
                emptyList()
            } else {
                resolvePasswordCardDisplayLines(firstEntry, passwordCardDisplayFields).take(3)
            }
            val showLineIcons = displayLines.size > 1
            displayLines.forEach { line ->
                PasswordCardDetailLine(line = line, showIcon = showLineIcons)
            }

            if (showAuthenticator) {
                if (authenticatorState != null) {
                    PasswordAuthenticatorInlineRow(
                        state = authenticatorState,
                        smoothProgress = smoothAuthenticatorProgress
                    )
                }
            }

            if (!isSelectionMode) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stringResource(R.string.password)}:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        passwords.forEachIndexed { index, password ->
                            val isSelected = selectedPasswords.contains(password.id)

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                },
                                onClick = { onClick(password) },
                                modifier = Modifier.heightIn(min = 32.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Key,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        }
                                    )

                                    Text(
                                        text = stringResource(R.string.password_item_title, index + 1),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
