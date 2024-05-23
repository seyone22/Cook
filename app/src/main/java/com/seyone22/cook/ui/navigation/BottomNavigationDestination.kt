package com.seyone22.cook.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavigationDestination(
    val destination: NavigationDestination,
    val icon: ImageVector,
    val iconSelected: ImageVector
)