package takagi.ru.monica.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Monica's shape scale. `medium` is what Material cards use by default, so it
 * is the corner every vault list item, TOTP card and note tile shares; the
 * other steps keep the Material 3 values that dialogs, menus, chips and the
 * FAB were designed around.
 */
val MonicaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(MonicaShapeTokens.ItemCornerRadius),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Named corners for surfaces that are not plain Material components. */
object MonicaShapeTokens {
    /** Vault list items, authenticator cards and their swipe containers. */
    val ItemCornerRadius = 16.dp

    /** Rows nested inside an expanded stack or another card. */
    val NestedItemCornerRadius = 12.dp

    val Item = RoundedCornerShape(ItemCornerRadius)
    val NestedItem = RoundedCornerShape(NestedItemCornerRadius)

    /** The rounded square behind brand icons, favicons and monograms. */
    val IconTile = RoundedCornerShape(12.dp)

    /** Small count badges and status pills. */
    val Pill = RoundedCornerShape(percent = 50)
}
