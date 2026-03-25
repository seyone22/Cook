package com.seyone22.cook.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.seyone22.cook.R
import com.seyone22.cook.ui.navigation.BottomNavigationDestination
import com.seyone22.cook.ui.screen.home.HomeDestination
import com.seyone22.cook.ui.screen.ingredients.IngredientsDestination
import com.seyone22.cook.ui.screen.more.MoreDestination

// Keep the data as "Raw" as possible here (IDs and Vectors)
val destinations = listOf(
    BottomNavigationDestination(
        HomeDestination,
        R.drawable.hand_meal_24px,
        R.drawable.hand_meal_24px_filled
    ),
    BottomNavigationDestination(
        IngredientsDestination, R.drawable.avocado_bean_24px, R.drawable.avocado_bean_24px_filled
    ),
    BottomNavigationDestination(
        MoreDestination, Icons.Outlined.MoreHoriz, Icons.Filled.MoreHoriz
    ),
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CookNavBar(
    currentActivity: String?, navigateToScreen: (screen: String) -> Unit, visible: Boolean = true
) {
    // 1. Define the valid routes where the bar should show
    val rootRoutes = remember {
        listOf(HomeDestination.route, IngredientsDestination.route, MoreDestination.route)
    }

    // 2. Helper to resolve the icon inside the Composable scope
    @Composable
    fun resolveIcon(source: Any): ImageVector {
        return when (source) {
            is ImageVector -> source
            is Int -> ImageVector.vectorResource(id = source)
            else -> Icons.Default.Cookie
        }
    }

    if (visible && currentActivity in rootRoutes) {
        NavigationBar {
            destinations.forEach { destinationItem ->
                val isSelected = currentActivity == destinationItem.destination.route

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { navigateToScreen(destinationItem.destination.route) },
                    label = { Text(destinationItem.destination.route) },
                    icon = {
                        // 3. AnimatedContent needs to be here for the smooth fill/outline swap
                        AnimatedContent(
                            targetState = isSelected, label = "IconSwap"
                        ) { targetSelected ->
                            // IMPORTANT: resolveIcon must be called inside here
                            val icon = if (targetSelected) {
                                resolveIcon(destinationItem.iconSelected)
                            } else {
                                resolveIcon(destinationItem.icon)
                            }

                            Icon(
                                imageVector = icon,
                                contentDescription = destinationItem.destination.route
                            )
                        }
                    })
            }
        }
    }
}