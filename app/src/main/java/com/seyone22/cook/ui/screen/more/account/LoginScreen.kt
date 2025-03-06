package com.seyone22.cook.ui.screen.more.account

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.navigation.NavigationDestination

object LoginDestination : NavigationDestination {
    override val route = "Login"
    override val titleRes = R.string.app_name
    override val routeId = 76
}

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavController,
    context: Context = LocalContext.current
) {

    val authState by viewModel.authState.collectAsState()

    Scaffold {
        Column(modifier = Modifier.padding(it)) {
            when (authState) {
                is AuthState.Initial -> {
                    // Initial state, show the "Start Auth" button
                    Button(onClick = { viewModel.startAuth() }) {
                        Text("Start Authentication")
                    }
                }

                is AuthState.Loading -> {
                    // Show loading state
                    CircularProgressIndicator(modifier = Modifier)
                }

                is AuthState.Success -> {
                    // Show success message or proceed with the next steps
                    Text("Authentication successful: ${(authState as AuthState.Success).result}")
                }

                is AuthState.Error -> {
                    // Show error message
                    Text("Error: ${(authState as AuthState.Error).message}")
                }
            }
        }
    }
}
