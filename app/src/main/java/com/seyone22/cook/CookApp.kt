package com.seyone22.cook

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.seyone22.cook.ui.common.CookFAB
import com.seyone22.cook.ui.common.CookNavBar
import com.seyone22.cook.ui.common.CookTopBar
import com.seyone22.cook.ui.navigation.CookNavHost
import com.seyone22.cook.ui.screen.search.SearchDestination

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookApp(
    navController: NavHostController = rememberNavController(),
) {
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var topBarOperation: Int by remember { mutableIntStateOf(0) }
    var isSelected by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CookTopBar(
                currentActivity = navBackStackEntry?.destination?.route,
                navController = navController,
                searchAction = { navController.navigate(SearchDestination.route) }
            )
        },
        bottomBar = {
            CookNavBar(
                currentActivity = navBackStackEntry?.destination?.route,
                navigateToScreen = { screen -> navController.navigate(screen) },
            )
        },
        floatingActionButton = {
            CookFAB(
                currentActivity = navBackStackEntry?.destination?.route,
                navigateToScreen = { screen -> navController.navigate(screen) },
            )
        }
    ) { innerPadding ->
        CookNavHost(
            navController = navController,
            innerPadding = innerPadding
        )
    }

}