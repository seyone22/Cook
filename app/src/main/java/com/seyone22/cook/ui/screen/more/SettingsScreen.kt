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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.seyone22.cook.R
import com.seyone22.cook.SharedViewModel
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.common.dialog.action.ImportRecipeUrlDialogAction
import com.seyone22.cook.ui.navigation.NavigationDestination
import com.seyone22.cook.ui.screen.more.account.AuthUiState
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
                Intent.ACTION_VIEW, "https://github.com/seyone22/Cook/tags".toUri()
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
                                Intent.ACTION_VIEW, "http://seyone22.github.io".toUri()
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
                                Intent.ACTION_VIEW, "mailto:s.g.seyone@live.com".toUri()
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
                        onDismiss = { })
                )
            })

        SettingsListItem(
            settingName = "Refresh Ingredient Data",
            settingSubtext = "Fetch latest ingredient data from the database",
            action = {
                viewModel.updateIngredients(context)
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
    navigateToScreen: (screen: String) -> Unit,
) {
    val uiState by authViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // React to error states automatically
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Error) {
            snackbarHostState.showSnackbar((uiState as AuthUiState.Error).message)
            authViewModel.resetUiState()
        }
    }

    val currentUser by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current

    val isGuest = currentUser?.isAnonymous != false

    Column(modifier = Modifier.fillMaxWidth()) {
        if (isGuest) {
            // --- GUEST VIEW ---
            // Wrapped in a Card to make the "Call to Action" pop
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Secure Your Kitchen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Link a Google account to enable family sharing, cloud sync, and device backups.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { authViewModel.launchCredentialManagerAuth(context) }) {
                        Text("Link Account")
                    }
                }
            }
        } else {
            // --- AUTHENTICATED VIEW ---
            val displayName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Cook Chef"
            val email = currentUser?.email ?: "Linked Account"
            val photoUrl = currentUser?.photoUrl

            // 1. Profile Header
            ProfileHeaderCard(
                displayName = displayName,
                email = email,
                photoUrl = photoUrl?.toString()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 2. Management Section
            Text(
                text = "Account Management",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            SettingsListItem(
                settingName = "Manage Google Account",
                settingSubtext = "Security, personal info, and privacy",
                action = {
                    // Launches the system browser to the Google Account dashboard
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myaccount.google.com/"))
                    context.startActivity(intent)
                }
            )

            // Optional: A placeholder for your Firebase sync logic later
            SettingsListItem(
                settingName = "Force Cloud Sync",
                settingSubtext = "Last synced: Just now",
                action = { /* Trigger Room -> Firestore sync */ }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 3. Danger Zone
            Text(
                text = "Danger Zone",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            SettingsListItem(
                settingName = "Sign Out",
                settingSubtext = "Return to a local-only kitchen",
                action = { authViewModel.signOut() }
            )

            SettingsListItem(
                settingName = "Delete Account",
                settingSubtext = "Permanently wipe your cloud data",
                action = {
                    // TODO: Implement Re-Auth and Account Deletion
                    // navigateToScreen("DeleteAccountWarning")
                }
            )

            if (uiState is AuthUiState.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

/**
 * A dedicated, visually distinct header for the user's profile.
 */
@Composable
fun ProfileHeaderCard(
    displayName: String,
    email: String,
    photoUrl: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar using Coil
        AsyncImage(
            model = photoUrl ?: "https://ui-avatars.com/api/?name=${displayName.replace(" ", "+")}&background=random",
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Name and Email
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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