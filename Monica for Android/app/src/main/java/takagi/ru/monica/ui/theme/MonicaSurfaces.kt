package takagi.ru.monica.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/**
 * Semantic surface roles for vault UI.
 *
 * Screens ask for "the colour of a list item" rather than picking a Material
 * container tone themselves. The roles are resolved once per theme, which is
 * the only place that knows about the OLED pure-black mode: there the lower
 * container tones are all black, so items step up to the tones that still
 * separate from the page.
 */
@Immutable
data class MonicaSurfaceColors(
    /** A vault item resting on the page background. */
    val item: Color,
    /** A row nested inside an item, such as an expanded stack. */
    val nestedItem: Color,
    /** The peeking layers behind a collapsed stack. */
    val stackLayer: Color,
    /** The tile behind brand icons and favicons. */
    val iconTile: Color,
    /** Whether [iconTile] is dark, which decides the brand icon variant. */
    val iconTileIsDark: Boolean,
    /** The bottom navigation bar and other docked bars. */
    val navigationBar: Color,
    /** The action pill in the large top bar. */
    val toolbar: Color,
)

internal fun monicaSurfaceColors(
    colorScheme: ColorScheme,
    darkTheme: Boolean,
    pureBlack: Boolean,
): MonicaSurfaceColors {
    val item = when {
        pureBlack -> colorScheme.surfaceContainerHighest
        else -> colorScheme.surfaceContainer
    }
    // Favicons and full-colour brand marks are drawn for light backgrounds, so
    // they sit on a light tile in both themes; dark mode dims it to avoid glare.
    val iconTile = if (darkTheme) Color(0xFFE9E7EC) else colorScheme.surfaceContainerLowest
    return MonicaSurfaceColors(
        item = item,
        nestedItem = when {
            pureBlack -> colorScheme.surfaceContainerHighest
            darkTheme -> colorScheme.surfaceContainerHigh
            else -> colorScheme.surfaceContainerLowest
        },
        stackLayer = when {
            // Pure black has no container tone between the page and an item.
            pureBlack -> lerp(colorScheme.background, item, 0.6f)
            darkTheme -> colorScheme.surfaceContainerLow
            else -> colorScheme.surfaceContainerHigh
        },
        iconTile = iconTile,
        iconTileIsDark = iconTile.luminance() < 0.5f,
        navigationBar = if (pureBlack) colorScheme.surface else colorScheme.surfaceContainer,
        toolbar = if (pureBlack) colorScheme.surfaceContainerHighest else colorScheme.surfaceContainerHigh,
    )
}

internal val LocalMonicaSurfaces = staticCompositionLocalOf<MonicaSurfaceColors?> { null }

/** Entry point for Monica's design tokens that Material does not model. */
object MonicaDesign {
    /**
     * Surface roles of the current theme. Falls back to roles derived from
     * the ambient Material colours for content hosted outside [MonicaTheme],
     * such as autofill and IME windows that theme themselves.
     */
    val surfaces: MonicaSurfaceColors
        @Composable
        @ReadOnlyComposable
        get() = LocalMonicaSurfaces.current ?: MaterialTheme.colorScheme.let { scheme ->
            monicaSurfaceColors(
                colorScheme = scheme,
                darkTheme = scheme.background.luminance() < 0.5f,
                pureBlack = scheme.background == Color.Black,
            )
        }
}
