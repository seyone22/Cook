package com.seyone22.cook.ui.screen.more.account

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.ui.navigation.NavigationDestination

object ChangePasswordDestination : NavigationDestination {
    override val route = "ChangePassword"
    override val titleRes = R.string.app_name
    override val routeId = 13
}

@Composable
fun ChangePasswordScreen(
    navController: NavController
) {
    val context = LocalContext.current

    // State variables for password fields
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // New Password input field
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("New Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password input field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display error message if password update fails
        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Update Password button
        Button(
            onClick = {
                // Check if the passwords match and are valid
                if (newPassword == confirmPassword && newPassword.length >= 6) {
                    loading = true
                } else {
                    errorMessage = if (newPassword != confirmPassword) {
                        "Passwords do not match"
                    } else {
                        "Password must be at least 6 characters"
                    }
                }
            }, enabled = !loading, modifier = Modifier.fillMaxWidth()
        ) {
            Text("Update Password")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display a loading indicator when updating the password
        if (loading) {
            CircularProgressIndicator()
        }
    }
}
