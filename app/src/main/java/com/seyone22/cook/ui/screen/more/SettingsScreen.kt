package com.seyone22.cook.ui.screen.more

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seyone22.cook.R
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.navigation.NavigationDestination
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
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
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
                    GeneralSettingsList(
                        viewModel = viewModel
                    )
                }

                "Data" -> {
                    DataSettingsList(viewModel = viewModel)
                }

                "About" -> {
                    AboutList(
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
fun AboutList(context: Context) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)

    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with your XML drawable resource ID
            contentDescription = null, // Provide content description if needed
            modifier = Modifier.size(360.dp)
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
        SettingsToggleListItem(settingName = "Require Unlock",
            toggle = false,
            onToggleChange = { newValue ->

            })
        SettingsListItem(settingName = "Lock when idle", settingSubtext = "", action = {

        })
        SettingsToggleListItem(settingName = "Secure screen",
            settingSubtext = "Hides app contents when switching apps, and blocks screenshots",
            toggle = false,
            onToggleChange = { newValue ->

            })
    }
}


@Composable
fun DataSettingsList(
    viewModel: MoreViewModel,
    scope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current,
    activity: Activity = LocalContext.current as Activity
) {
    val filePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                // Handle the selected file URI here
                viewModel.importRecipe(activity, it)
            }
        }

    Column {
        SettingsListItem(
            settingName = "Import a recipe",
            settingSubtext = "Import from .recipe file",
            action = {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*" // All file types
                }
                filePickerLauncher.launch(arrayOf("*/*"))
            })
    }
}

