package takagi.ru.monica.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing steps on a 4 dp grid. New UI code uses these names instead of
 * literals so list density can be tuned in one place.
 */
object MonicaSpacing {
    val XXSmall = 2.dp
    val XSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val XLarge = 24.dp
    val XXLarge = 32.dp

    /** Horizontal inset of page content from the screen edge. */
    val ScreenHorizontal = 16.dp

    /** Vertical gap between vault list items. */
    val ListItemGap = 8.dp

    /** Padding inside a vault list item. */
    val ListItemHorizontal = 16.dp
    val ListItemVertical = 12.dp

    /** Gap between a leading icon tile and the text beside it. */
    val LeadingGap = 14.dp
}
