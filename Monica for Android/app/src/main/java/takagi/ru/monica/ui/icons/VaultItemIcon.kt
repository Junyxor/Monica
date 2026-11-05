package takagi.ru.monica.ui.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import takagi.ru.monica.autofill_ng.ui.isWebAddress
import takagi.ru.monica.autofill_ng.ui.rememberAppIcon
import takagi.ru.monica.autofill_ng.ui.rememberFavicon
import takagi.ru.monica.data.UnmatchedIconHandlingStrategy
import takagi.ru.monica.ui.theme.MonicaDesign
import takagi.ru.monica.ui.theme.MonicaShapeTokens

object VaultItemIconDefaults {
    val Size = 40.dp
}

/**
 * The one icon every vault surface shows for an item: password rows, stacks,
 * authenticator cards and the overview all resolve it here, with the same
 * precedence and the same tile.
 *
 * Precedence: the item's emoji, its chosen brand mark or uploaded image, a
 * brand mark matched from the website, title or app, the site's favicon, the
 * linked app's icon, and finally [unmatchedIconHandlingStrategy].
 *
 * Brand marks and favicons are drawn for light backgrounds, so they sit on a
 * light tile in both themes and the light asset variant is always requested;
 * before this, a dark app theme on a light system showed black logos on dark
 * cards.
 *
 * Space is reserved while icons load so titles do not shift when one arrives.
 * With [UnmatchedIconHandlingStrategy.HIDE] nothing is drawn, and no space is
 * taken, until an icon resolves. [endSpacing] is added only when something is
 * drawn, so callers can place text beside it without an empty gap.
 */
@Composable
internal fun VaultItemIcon(
    website: String,
    title: String,
    appPackageName: String,
    customIconType: String,
    customIconValue: String?,
    defaultIcon: ImageVector,
    modifier: Modifier = Modifier,
    unmatchedIconHandlingStrategy: UnmatchedIconHandlingStrategy = UnmatchedIconHandlingStrategy.DEFAULT_ICON,
    size: Dp = VaultItemIconDefaults.Size,
    endSpacing: Dp = 0.dp,
) {
    val surfaces = MonicaDesign.surfaces
    val darkVariant = surfaces.iconTileIsDark

    val emoji = customIconValue?.takeIf { customIconType == PASSWORD_ICON_TYPE_EMOJI && it.isNotBlank() }
    val chosenBrandIcon = if (customIconType == PASSWORD_ICON_TYPE_SIMPLE) {
        rememberSimpleIconBitmap(
            slug = customIconValue,
            tintColor = MaterialTheme.colorScheme.primary,
            darkVariant = darkVariant
        )
    } else null
    val uploadedIcon = if (customIconType == PASSWORD_ICON_TYPE_UPLOADED) {
        rememberUploadedPasswordIcon(customIconValue)
    } else null
    val hasCustomIcon = emoji != null || chosenBrandIcon != null || uploadedIcon != null

    val matchedBrandIcon = rememberAutoMatchedSimpleIcon(
        website = website,
        title = title,
        appPackageName = appPackageName.ifBlank { null },
        tintColor = MaterialTheme.colorScheme.primary,
        enabled = customIconType == PASSWORD_ICON_TYPE_NONE,
        darkVariant = darkVariant
    )
    val favicon = if (!hasCustomIcon && website.isNotBlank()) {
        rememberFavicon(
            url = website,
            enabled = matchedBrandIcon.resolved && matchedBrandIcon.slug == null
        )
    } else null
    val appIcon = if (!hasCustomIcon && appPackageName.isNotBlank() && !isWebAddress(website)) {
        rememberAppIcon(appPackageName)
    } else null

    val brandIcon: ImageBitmap? = chosenBrandIcon ?: uploadedIcon ?: matchedBrandIcon.bitmap
    val hasIcon = emoji != null || brandIcon != null || favicon != null || appIcon != null
    if (!hasIcon && unmatchedIconHandlingStrategy == UnmatchedIconHandlingStrategy.HIDE) return
    // Covers both the brand lookup and the bitmap load after a match, and a
    // chosen brand mark whose bitmap is still decoding.
    val stillMatching = (customIconType == PASSWORD_ICON_TYPE_NONE &&
        (!matchedBrandIcon.resolved || (matchedBrandIcon.slug != null && matchedBrandIcon.bitmap == null))) ||
        (customIconType == PASSWORD_ICON_TYPE_SIMPLE && !customIconValue.isNullOrBlank() && chosenBrandIcon == null)

    val tileModifier = modifier
        .padding(end = endSpacing)
        .size(size)
        .clip(MonicaShapeTokens.IconTile)
    when {
        emoji != null -> IconTile(tileModifier, surfaces.iconTile) {
            EmojiIconText(emoji = emoji, size = size * 0.62f)
        }

        brandIcon != null -> IconTile(tileModifier, surfaces.iconTile) {
            Image(
                bitmap = brandIcon,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(size * 0.62f)
            )
        }

        favicon != null -> IconTile(tileModifier, surfaces.iconTile) {
            Image(
                bitmap = favicon,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(size * 0.56f)
            )
        }

        // Launcher icons carry their own shape and background.
        appIcon != null -> IconTile(tileModifier, surfaces.iconTile) {
            Image(
                bitmap = appIcon,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(size * 0.8f)
            )
        }

        // A neutral tile while the brand lookup runs avoids flashing the
        // fallback glyph for items that do have a match.
        stillMatching -> IconTile(tileModifier, surfaces.iconTile) {}

        unmatchedIconHandlingStrategy == UnmatchedIconHandlingStrategy.WEBSITE_OR_TITLE_INITIAL -> {
            val (container, content) = monogramColors(website.ifBlank { title })
            IconTile(tileModifier, container) {
                Text(
                    text = monogramInitial(website, title),
                    color = content,
                    fontSize = (size.value * 0.42f).sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        else -> IconTile(tileModifier, MaterialTheme.colorScheme.primaryContainer) {
            Icon(
                imageVector = defaultIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}

@Composable
private fun IconTile(
    modifier: Modifier,
    container: Color,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.background(container),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/** One of the theme's three accent containers, stable for the same text. */
@Composable
private fun monogramColors(seed: String): Pair<Color, Color> {
    val colors = MaterialTheme.colorScheme
    val palette = remember(colors) {
        listOf(
            colors.primaryContainer to colors.onPrimaryContainer,
            colors.secondaryContainer to colors.onSecondaryContainer,
            colors.tertiaryContainer to colors.onTertiaryContainer,
        )
    }
    val index = (seed.trim().lowercase().hashCode() and Int.MAX_VALUE) % palette.size
    return palette[index]
}

internal fun monogramInitial(website: String, title: String): String =
    listOf(website.removeUrlScheme(), title)
        .firstNotNullOfOrNull { source ->
            source.trim().firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString()
        } ?: "#"

private fun String.removeUrlScheme(): String =
    substringAfter("://").removePrefix("www.")
