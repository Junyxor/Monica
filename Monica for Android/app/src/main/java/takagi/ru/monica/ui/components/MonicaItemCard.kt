package takagi.ru.monica.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import takagi.ru.monica.ui.theme.MonicaDesign
import takagi.ru.monica.ui.theme.MonicaShapeTokens

/**
 * Corner treatment shared by every item card and its gesture container.
 * Keeping one shape prevents the outer swipe surface from exposing a second,
 * larger radius around the actual card.
 */
val MonicaItemCardShape = MonicaShapeTokens.Item

// ID-1 cards have an approximately 3.18 mm radius on a 53.98 mm short edge.
val BankCardShape = RoundedCornerShape(percent = 6)

/**
 * Shared surface for vault items across list and tile layouts: a flat tonal
 * card. Separation comes from the surface tone, not a shadow, so items stay
 * crisp on OLED black and in long lists.
 */
@Composable
fun MonicaItemCard(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    transparentContainer: Boolean = false,
    shape: androidx.compose.ui.graphics.Shape = MonicaItemCardShape,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                transparentContainer -> Color.Transparent
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> MonicaDesign.surfaces.item
            }
        ),
        content = content
    )
}
