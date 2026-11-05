package takagi.ru.monica.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Monica's type scale.
 *
 * Sizes and line heights are the Material 3 defaults, so layouts tuned against
 * them keep their measurements. Only weights and tracking of titles and
 * headlines change: they are semibold so list rows and page titles read as
 * one family without every call site adding `fontWeight = Bold`, and large
 * headlines are tightened slightly so short titles such as "Monica" look set
 * rather than typed.
 */
private val MaterialDefaults = Typography()

val Typography = Typography(
    displayLarge = MaterialDefaults.displayLarge,
    displayMedium = MaterialDefaults.displayMedium,
    displaySmall = MaterialDefaults.displaySmall,
    headlineLarge = MaterialDefaults.headlineLarge.semibold(letterSpacing = (-0.25).sp),
    headlineMedium = MaterialDefaults.headlineMedium.semibold(letterSpacing = (-0.2).sp),
    headlineSmall = MaterialDefaults.headlineSmall.semibold(),
    titleLarge = MaterialDefaults.titleLarge.semibold(),
    titleMedium = MaterialDefaults.titleMedium.semibold(letterSpacing = 0.1.sp),
    titleSmall = MaterialDefaults.titleSmall.semibold(),
    bodyLarge = MaterialDefaults.bodyLarge,
    bodyMedium = MaterialDefaults.bodyMedium,
    bodySmall = MaterialDefaults.bodySmall,
    // Labels keep Material's medium weight: buttons, chips and tab labels are
    // sized for it, and heavier labels would truncate in narrow tab bars.
    labelLarge = MaterialDefaults.labelLarge,
    labelMedium = MaterialDefaults.labelMedium,
    labelSmall = MaterialDefaults.labelSmall,
)

/**
 * Digits that keep their width as they change: one-time codes, countdowns and
 * card numbers line up and do not jitter when a value ticks.
 */
val TextStyle.tabularNumbers: TextStyle
    get() = copy(fontFeatureSettings = "tnum")

private fun TextStyle.semibold(letterSpacing: androidx.compose.ui.unit.TextUnit = this.letterSpacing) =
    copy(fontWeight = FontWeight.SemiBold, letterSpacing = letterSpacing)
