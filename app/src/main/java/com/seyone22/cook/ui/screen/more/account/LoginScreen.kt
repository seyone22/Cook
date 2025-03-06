package com.seyone22.cook.ui.screen.more.account

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.viewinterop.AndroidView
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
                    // Show WebView with the authentication page
                    val redirectURI = (authState as AuthState.Success).result
                    if (redirectURI.isNotEmpty()) {
                        OAuthWebView(redirectURI) { code, state, iss ->
                            viewModel.handleOAuthResult(code, state, iss)
                        }


                    }
                }

                is AuthState.Error -> {
                    // Show error message
                    Text("Error: ${(authState as AuthState.Error).message}")
                }

                is AuthState.Authenticated -> {

                }
            }
        }
    }
}

@Composable
fun OAuthWebView(redirectURI: String, onAuthComplete: (String?, String?, String?) -> Unit) {
    AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
        android.webkit.WebView(context).apply {
            settings.javaScriptEnabled = true
            webViewClient = OAuthWebViewClient { code, state, iss ->
                // Close WebView and return extracted values
                onAuthComplete(code, state, iss)
            }
            loadUrl(redirectURI)
        }
    })
}

class OAuthWebViewClient(
    private val onAuthSuccess: (code: String?, state: String?, iss: String?) -> Unit
) : android.webkit.WebViewClient() {

    override fun shouldOverrideUrlLoading(
        view: android.webkit.WebView?, request: android.webkit.WebResourceRequest?
    ): Boolean {
        val url = request?.url.toString()

        // Handle the redirect URI
        if (url.startsWith("io.github.seyone22:/oauth/callback")) {
            // Extract query parameters
            val uri = Uri.parse(url)
            val code = uri.getQueryParameter("code")
            val state = uri.getQueryParameter("state")
            val iss = uri.getQueryParameter("iss")

            // Log extracted parameters
            Log.d("OAuth", "Code: $code, State: $state, Iss: $iss")

            // Notify the success callback
            onAuthSuccess(code, state, iss)

            return true // Prevent WebView from opening the URL
        }
        return super.shouldOverrideUrlLoading(view, request)
    }
}
