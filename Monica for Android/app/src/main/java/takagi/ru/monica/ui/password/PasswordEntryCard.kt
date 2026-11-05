package takagi.ru.monica.ui.password

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import takagi.ru.monica.R
import takagi.ru.monica.bitwarden.sync.SyncStatus
import takagi.ru.monica.data.PasswordCardDisplayField
import takagi.ru.monica.data.PasswordEntry
import takagi.ru.monica.data.UnmatchedIconHandlingStrategy
import takagi.ru.monica.data.primaryLinkedAppPackageName
import takagi.ru.monica.ui.components.SyncStatusIcon
import takagi.ru.monica.ui.icons.VaultItemIcon
import takagi.ru.monica.ui.theme.MonicaDesign
import takagi.ru.monica.ui.theme.MonicaShapeTokens
import takagi.ru.monica.ui.theme.MonicaSpacing
import takagi.ru.monica.ui.theme.tabularNumbers

/**
 * One credential in the vault list.
 *
 * Layout: icon tile, then title with source markers and up to three detail
 * lines (or the bound one-time code), then the trailing action. The trailing
 * action is centred on the whole row so single-line and multi-line items
 * align down the list.
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    androidx.compose.animation.ExperimentalSharedTransitionApi::class
)
@Composable
fun PasswordEntryCard(
    entry: PasswordEntry,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onToggleFavorite: (() -> Unit)? = null,
    onToggleGroupCover: (() -> Unit)? = null,
    supportingBadge: (@Composable () -> Unit)? = null,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    canSetGroupCover: Boolean = false,
    isInExpandedGroup: Boolean = false,
    isSingleCard: Boolean = false,
    iconCardsEnabled: Boolean = false,
    unmatchedIconHandlingStrategy: UnmatchedIconHandlingStrategy = UnmatchedIconHandlingStrategy.DEFAULT_ICON,
    passwordCardDisplayMode: takagi.ru.monica.data.PasswordCardDisplayMode = takagi.ru.monica.data.PasswordCardDisplayMode.SHOW_ALL,
    passwordCardDisplayFields: List<PasswordCardDisplayField> = PasswordCardDisplayField.DEFAULT_ORDER,
    showAuthenticator: Boolean = false,
    hideOtherContentWhenAuthenticator: Boolean = false,
    totpTimeOffsetSeconds: Int = 0,
    smoothAuthenticatorProgress: Boolean = true,
    decryptAuthenticatorKey: ((String) -> String)? = null,
    leadingIconOverride: (@Composable () -> Unit)? = null,
    enableSharedBounds: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape? = null,
) {
    val displayTitle = entry.title.ifBlank { stringResource(R.string.untitled) }
    val sharedTransitionScope = takagi.ru.monica.ui.LocalSharedTransitionScope.current
    val animatedVisibilityScope = takagi.ru.monica.ui.LocalAnimatedVisibilityScope.current
    val reduceAnimations = takagi.ru.monica.ui.LocalReduceAnimations.current
    var sharedModifier: Modifier = Modifier
    val nested = isInExpandedGroup && !isSingleCard
    val cardShape = shape ?: if (nested) MonicaShapeTokens.NestedItem else MonicaShapeTokens.Item
    if (enableSharedBounds && !reduceAnimations && sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            sharedModifier = Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = "password_card_${entry.id}"),
                animatedVisibilityScope = animatedVisibilityScope,
                resizeMode = androidx.compose.animation.SharedTransitionScope.ResizeMode.RemeasureToBounds
            )
        }
    }

    val surfaces = MonicaDesign.surfaces
    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        nested -> surfaces.nestedItem
        else -> surfaces.item
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(sharedModifier),
        shape = cardShape,
        color = containerColor,
        contentColor = contentColorFor(containerColor)
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                    .heightIn(min = if (nested) 60.dp else 68.dp)
                    .padding(
                        start = if (nested) MonicaSpacing.Medium else MonicaSpacing.ListItemHorizontal,
                        end = MonicaSpacing.XSmall,
                        top = if (nested) 10.dp else MonicaSpacing.ListItemVertical,
                        bottom = if (nested) 10.dp else MonicaSpacing.ListItemVertical
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (iconCardsEnabled) {
                    if (leadingIconOverride != null) {
                        leadingIconOverride()
                        Spacer(modifier = Modifier.width(MonicaSpacing.LeadingGap))
                    } else {
                        VaultItemIcon(
                            website = entry.website,
                            title = entry.title,
                            appPackageName = entry.primaryLinkedAppPackageName(),
                            customIconType = entry.customIconType,
                            customIconValue = entry.customIconValue,
                            defaultIcon = Icons.Default.Key,
                            unmatchedIconHandlingStrategy = unmatchedIconHandlingStrategy,
                            size = if (nested) 36.dp else 42.dp,
                            endSpacing = if (nested) MonicaSpacing.Medium else MonicaSpacing.LeadingGap
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = MonicaSpacing.Small),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        EntrySourceMarker(entry)
                    }

                    if (entry.isApiKeyEntry()) {
                        Text(
                            stringResource(R.string.api_key_title),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    val authenticatorState = if (showAuthenticator) {
                        rememberPasswordAuthenticatorDisplayState(
                            authenticatorKey = entry.authenticatorKey,
                            fallbackIssuer = entry.website.ifBlank { entry.title },
                            fallbackAccountName = entry.username.ifBlank { entry.title },
                            timeOffsetSeconds = totpTimeOffsetSeconds,
                            smoothProgress = smoothAuthenticatorProgress,
                            decryptAuthenticatorKey = decryptAuthenticatorKey
                        )
                    } else {
                        null
                    }
                    val shouldHideDisplayLines = hideOtherContentWhenAuthenticator && authenticatorState != null
                    val displayLines = remember(
                        entry.username,
                        entry.website,
                        entry.notes,
                        entry.updatedAt,
                        passwordCardDisplayFields,
                        passwordCardDisplayMode,
                        shouldHideDisplayLines,
                    ) {
                        if (
                            passwordCardDisplayMode == takagi.ru.monica.data.PasswordCardDisplayMode.TITLE_ONLY ||
                            shouldHideDisplayLines
                        ) {
                            emptyList()
                        } else {
                            resolvePasswordCardDisplayLines(entry, passwordCardDisplayFields).take(3)
                        }
                    }
                    // A single detail line reads as a subtitle; icons only help
                    // tell lines apart once there are several.
                    val showLineIcons = displayLines.size > 1
                    displayLines.forEach { line ->
                        PasswordCardDetailLine(line = line, showIcon = showLineIcons)
                    }

                    if (authenticatorState != null) {
                        PasswordAuthenticatorInlineRow(
                            state = authenticatorState,
                            smoothProgress = smoothAuthenticatorProgress
                        )
                    }
                }

                if (onToggleGroupCover != null) {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isSelectionMode) {
                            IconButton(
                                onClick = onToggleGroupCover,
                                modifier = Modifier.size(40.dp),
                                enabled = canSetGroupCover
                            ) {
                                Icon(
                                    if (entry.isGroupCover) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = if (entry.isGroupCover) "Remove cover" else "Set as cover",
                                    tint = if (entry.isGroupCover) {
                                        MaterialTheme.colorScheme.tertiary
                                    } else if (canSetGroupCover) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else if (entry.isGroupCover) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = stringResource(R.string.legacy_ui_cover),
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() }
                    )
                } else if (onToggleFavorite != null) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(40.dp)) {
                        Icon(
                            if (entry.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.favorite),
                            tint = if (entry.isFavorite) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(MonicaSpacing.Medium))
                }
            }

            supportingBadge?.let { badge ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                ) {
                    badge()
                }
            }
        }
    }
}

/** Where an item lives when it is not local: Bitwarden sync state or KeePass. */
@Composable
private fun EntrySourceMarker(entry: PasswordEntry) {
    if (entry.isBitwardenEntry()) {
        val syncStatus = when {
            entry.hasPendingBitwardenSync() -> SyncStatus.PENDING
            else -> SyncStatus.SYNCED
        }
        Spacer(modifier = Modifier.width(6.dp))
        SyncStatusIcon(status = syncStatus, size = 14.dp)
    } else if (entry.isKeePassEntry()) {
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            Icons.Default.VpnKey,
            contentDescription = "KeePass",
            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
internal fun PasswordCardDetailLine(
    line: PasswordCardDisplayLine,
    showIcon: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showIcon) {
            Icon(
                line.icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
        Text(
            text = line.text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun PasswordAuthenticatorInlineRow(
    state: PasswordAuthenticatorDisplayState,
    smoothProgress: Boolean
) {
    val expiring = state.remainingSeconds != null && state.remainingSeconds <= 5
    val accent = if (expiring) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Row(
        horizontalArrangement = Arrangement.spacedBy(MonicaSpacing.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = state.code,
            style = MaterialTheme.typography.titleMedium.tabularNumbers,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (state.progress != null) {
            PasswordAuthenticatorCountdownRing(
                state = state,
                smoothProgress = smoothProgress,
                color = accent
            )
        }
        state.remainingSeconds?.let { remaining ->
            Text(
                text = stringResource(R.string.password_card_authenticator_seconds, remaining),
                style = MaterialTheme.typography.labelMedium.tabularNumbers,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
