package com.seyone22.cook.ui.screen.more

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seyone22.cook.R
import com.seyone22.cook.SharedViewModel
import com.seyone22.cook.service.RecipeImportService
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.common.dialog.action.ImportRecipeUrlDialogAction
import com.seyone22.cook.ui.navigation.NavigationDestination
import com.seyone22.cook.ui.screen.more.account.AuthState
import com.seyone22.cook.ui.screen.more.account.AuthViewModel
import kotlinx.coroutines.CoroutineScope

object SettingsDestination : NavigationDestination {
    override val route = "Settings"
    override val titleRes = R.string.app_name
    override val routeId = 15
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDetailScreen(
    modifier: Modifier = Modifier,
    navigateToScreen: (screen: String) -> Unit,
    navigateBack: () -> Unit,
    backStackEntry: String,
    context: Context = LocalContext.current,
    viewModel: MoreViewModel = viewModel(factory = AppViewModelProvider.Factory),
    sharedViewModel: SharedViewModel
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ), title = { Text(text = backStackEntry) }, navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                })
        },
    ) { innerPadding ->
        Column(
            Modifier.padding(innerPadding)
        ) {
            when (backStackEntry) {
                "General" -> {
                    GeneralSettingsList(viewModel = viewModel)
                }

                "Account" -> {
                    AccountSettingsList(
                        viewModel = viewModel,
                        navigateToScreen = navigateToScreen,
                    )
                }

                "Data" -> {
                    DataSettingsList(
                        viewModel = viewModel,
                        sharedViewModel = sharedViewModel,
                        navigateToScreen = navigateToScreen
                    )
                }

                "Tag" -> {
                    TagSettingsList(viewModel = viewModel)
                }

                "About" -> {
                    AboutList()
                }
            }
        }
    }
}

@Composable
fun AboutList(context: Context = LocalContext.current) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)

    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with your XML drawable resource ID
            contentDescription = null, // Provide content description if needed
            modifier = Modifier.size(360.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )
    }
    HorizontalDivider()
    Column(modifier = Modifier.fillMaxWidth()) {
        ListItem(headlineContent = { Text(text = "Version") }, supportingContent = {
            Text(
                text = "${stringResource(id = R.string.app_version)} (${
                    stringResource(
                        id = R.string.release_date
                    )
                } | ${stringResource(id = R.string.release_time)})"
            )
        }, modifier = Modifier.clickable { })
        ListItem(headlineContent = { Text(text = "Database Version") }, supportingContent = {
            Text(
                text = stringResource(id = R.string.db_version)
            )
        }, modifier = Modifier.clickable { })
        ListItem(headlineContent = { Text(text = "Check for new versions") }, supportingContent = {
            Text(
                text = "Download and update the app from Github"
            )
        }, modifier = Modifier.clickable {
            val urlIntent = Intent(
                Intent.ACTION_VIEW, Uri.parse("https://github.com/seyone22/Cook/tags")
            )
            context.startActivity(urlIntent)
        })
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            headlineContent = { Text(text = "Contact") },
            supportingContent = {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            // Open the URL in a web browser
                            val intent = Intent(
                                Intent.ACTION_VIEW, Uri.parse("http://seyone22.github.io")
                            )
                            context.startActivity(intent)
                        }, modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Link, contentDescription = "Open URL"
                        )
                    }
                    IconButton(
                        onClick = {
                            // Open the URL in a web browser
                            val intent = Intent(
                                Intent.ACTION_VIEW, Uri.parse("mailto:s.g.seyone@live.com")
                            )
                            context.startActivity(intent)
                        }, modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Email, contentDescription = "Email"
                        )
                    }
                }
            },
        )
    }
}

@Composable
fun GeneralSettingsList(
    viewModel: MoreViewModel, scope: CoroutineScope = rememberCoroutineScope()
) {
    Column {
        SettingsToggleListItem(
            settingName = "Require Unlock", toggle = false, onToggleChange = { newValue ->

            })
        SettingsListItem(settingName = "Lock when idle", settingSubtext = "", action = {

        })
        SettingsToggleListItem(
            settingName = "Secure screen",
            settingSubtext = "Hides app contents when switching apps, and blocks screenshots",
            toggle = false,
            onToggleChange = { newValue ->

            })
    }
}


@Composable
fun DataSettingsList(
    viewModel: MoreViewModel,
    sharedViewModel: SharedViewModel,
    scope: CoroutineScope = rememberCoroutineScope(),
    navigateToScreen: (screen: String) -> Unit,
    context: Context = LocalContext.current,
) {
    val filePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                viewModel.importRecipe(context, it)
            }
        }

    var isLoading by remember { mutableStateOf(false) }

    Column {
        // File import
        SettingsListItem(
            settingName = "Import a recipe", settingSubtext = "Import from .recipe file", action = {
                filePickerLauncher.launch(arrayOf("*/*"))
            })

        Spacer(modifier = Modifier.height(8.dp))

        // URL import
        SettingsListItem(
            settingName = "Import from URL",
            settingSubtext = "Fetch recipe from a web page",
            action = {
                viewModel.showDialog(
                    ImportRecipeUrlDialogAction(
                        sharedViewModel = sharedViewModel,
                        navigateToScreen = navigateToScreen,
                        context = context,
                        onDismiss = { }
                    )
                )
            })

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }
    }
}


@Composable
fun AccountSettingsList(
    viewModel: MoreViewModel,
    authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory),
    scope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current,
    navigateToScreen: (screen: String) -> Unit,
) {
    val authState by authViewModel.authState.collectAsState()

    // Mutable state to control WebView visibility
    var showWebView by remember { mutableStateOf(false) }
    var redirectUri by remember { mutableStateOf("") }

    // Observe changes in authState and handle redirection
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val uri = (authState as AuthState.Success).result
            Log.d("TAG", "AccountSettingsList: uri $uri")

            if (uri.isNotEmpty()) {
                Log.d("TAG", "AccountSettingsList: here here")
                redirectUri = uri
                showWebView = true // Show WebView when auth completes
            }
        }
    }

    Column {
        if (true) {
            // Show "Create Account" and "Login" options only when no user is logged in
            SettingsListItem(
                settingName = "Create Cook Account",
                settingSubtext = "If you haven't already, create an account to join shared groups",
                action = {
                    navigateToScreen("Register")
                })
            SettingsListItem(
                settingName = "Login",
                settingSubtext = "Login to access your shared groups and recipes",
                action = {
                    authViewModel.startAuth()
                })
        }

        if (showWebView) {
            Log.d("TAG", "AccountSettingsList: here")
            OAuthWebView(redirectUri) { code, state, iss ->
                authViewModel.handleOAuthResult(code, state, iss, context)
                showWebView = false // Hide WebView after authentication
            }
        }
    }
}

@Composable
fun TagSettingsList(
    viewModel: MoreViewModel,
    scope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current,
) {
    viewModel.fetchTags()
    val tagsViewState by viewModel.moreViewState.collectAsState()

    LazyColumn {
        tagsViewState.tags.forEach { tag ->
            item(key = tag?.id) {
                ListItem(headlineContent = { Text(text = tag?.name ?: "") })
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
            val uri = url.toUri()
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


