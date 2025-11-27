package com.seyone22.cook

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.common.CookFAB
import com.seyone22.cook.ui.common.CookNavBar
import com.seyone22.cook.ui.navigation.CookNavHost
import com.seyone22.cook.ui.screen.crud.recipe.ImportRecipeDestination
import com.seyone22.expensetracker.ui.common.dialogs.GenericDialog

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun CookApp(
    navController: NavHostController = rememberNavController(),
    onCameraRequested: () -> Unit,
    sharedViewModel: SharedViewModel = viewModel(factory = AppViewModelProvider.Factory),
    sharedText: String?,
) {
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var topBarOperation: Int by remember { mutableIntStateOf(0) }
    var isSelected by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var overlayShown by remember { mutableStateOf(false) }
    val loading by sharedViewModel.isLoading.collectAsState()

    val currentDialog by sharedViewModel.currentDialog
    currentDialog?.let {
        GenericDialog(dialogAction = it, onDismiss = { sharedViewModel.dismissDialog() })
    }

    LaunchedEffect(sharedText) {
        sharedText?.let { url ->
            if (url.startsWith("http")) {
                sharedViewModel.setLoading(true) // show loader
                val success = sharedViewModel.importAndSaveRecipe(url)
                sharedViewModel.setLoading(false) // hide loader
                if (success) {
                    navController.navigate(ImportRecipeDestination.route)
                } else {
                    Toast.makeText(
                        context, "Failed to fetch recipe from shared URL", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            CookNavBar(
                currentActivity = navBackStackEntry?.destination?.route,
                navigateToScreen = { screen -> navController.navigate(screen) },
                visible = !overlayShown
            )
        },
        floatingActionButton = {
            CookFAB(
                currentActivity = navBackStackEntry?.destination?.route,
                navigateToScreen = { screen -> navController.navigate(screen) },
                visible = !overlayShown
            )
        }) { innerPadding ->
        CookNavHost(
            snackbarHostState = snackbarHostState,
            navController = navController,
            innerPadding = innerPadding,
            setOverlayStatus = {
                Log.d("TAG", "CookApp: $it")
                overlayShown = it
            },
        )
    }

    if (loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
    }
}
