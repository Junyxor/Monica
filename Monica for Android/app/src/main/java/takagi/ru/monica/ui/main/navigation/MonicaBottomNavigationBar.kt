package takagi.ru.monica.ui.main.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import takagi.ru.monica.ui.theme.MonicaDesign

/**
 * The docked tab bar of the main screen on compact widths.
 *
 * It sits on its own container tone so the list scrolling behind it stays
 * visually separate without a shadow; the selected tab uses the secondary
 * container indicator Material defines for navigation.
 */
@Composable
fun MonicaBottomNavigationBar(
    tabs: List<BottomNavItem>,
    selectedKey: String,
    onSelect: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MonicaDesign.surfaces.navigationBar,
        tonalElevation = 0.dp
    ) {
        tabs.forEach { item ->
            val label = stringResource(item.shortLabelRes())
            NavigationBarItem(
                selected = item.key == selectedKey,
                onClick = { onSelect(item) },
                icon = { Icon(item.icon, contentDescription = label) },
                label = {
                    Text(
                        text = label,
                        maxLines = 2,
                        overflow = TextOverflow.Clip
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
