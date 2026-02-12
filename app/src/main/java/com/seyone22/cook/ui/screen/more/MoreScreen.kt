package com.seyone22.cook.ui.screen.more

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.ImportExport
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.ui.common.CookTopBar
import com.seyone22.cook.ui.navigation.NavigationDestination
import java.io.File

object MoreDestination : NavigationDestination {
    override val route = "More"
    override val titleRes = R.string.app_name
    override val routeId = 2
}

@Composable
fun MoreScreen(
    modifier: Modifier = Modifier, navController: NavController
) {
    Scaffold(
        modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CookTopBar(currentActivity = "More", navController = navController)
        },
    ) { innerPadding ->
        Column(
            Modifier.padding(innerPadding)
        ) {
            SettingsListItem(settingName = "Shopping List",
                settingSubtext = "View and edit your shopping list",
                settingIcon = Icons.Outlined.ShoppingBag,
                action = { navController.navigate("Shopping List") })

            HorizontalDivider()

            SettingsListItem(settingName = "Settings",
                settingSubtext = "General application settings",
                settingIcon = Icons.Outlined.Checklist,
                action = { navController.navigate("Settings/General") })

            SettingsListItem(settingName = "Account",
                settingSubtext = "Account and Sharing settings",
                settingIcon = Icons.Outlined.ManageAccounts,
                action = { navController.navigate("Settings/Account") })

            SettingsListItem(settingName = "Data Management",
                settingSubtext = "Import and Export Data",
                settingIcon = Icons.Outlined.ImportExport,
                action = { navController.navigate("Settings/Data") })

            SettingsListItem(settingName = "Tag Management",
                settingSubtext = "Add, delete, and manage tags",
                settingIcon = Icons.Default.BookmarkBorder,
                action = { navController.navigate("Settings/Tag") })
            /*            SettingsListItem(
                            settingName = "Privacy and Security",
                            settingSubtext = "App lock, Secure Screen",
                            settingIcon = Icons.Outlined.Security,
                            action = { navController.navigate("SettingsDetail/Security") }
                        )
                        SettingsListItem(
                            settingName = "Import & Export",
                            settingSubtext = "Manage your transaction data",
                            settingIcon = Icons.Outlined.ImportExport,
                            action = { navController.navigate("SettingsDetail/ImportExport") }
                        )*/
            SettingsListItem(settingName = "About",
                settingSubtext = "${stringResource(id = R.string.app_name)} ${
                    stringResource(
                        id = R.string.app_version
                    )
                }",
                settingIcon = Icons.Outlined.Info,
                action = { navController.navigate("Settings/About") })
        }
    }
}

@Composable
fun SettingsListItem(
    modifier: Modifier = Modifier,
    settingName: String,
    settingSubtext: String,
    settingIcon: ImageVector? = null,
    action: () -> Unit
) {
    ListItem(
        modifier = modifier.clickable(onClick = action),
        headlineContent = { Text(text = settingName) },
        supportingContent = { Text(text = settingSubtext) },
        leadingContent = {
            if (settingIcon != null) {
                Icon(
                    imageVector = settingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
    )
}

@Composable
fun SettingsToggleListItem(
    modifier: Modifier = Modifier,
    settingName: String,
    settingSubtext: String? = null,
    settingIcon: ImageVector? = null,
    toggle: Boolean = false,
    onToggleChange: (Boolean) -> Unit
) {
    var tx by remember { mutableStateOf(toggle) }
    ListItem(modifier = modifier.clickable(onClick = {
        tx = !tx
        onToggleChange(tx)
    }), headlineContent = { Text(text = settingName) }, supportingContent = {
        if (settingSubtext != null) {
            Text(text = settingSubtext)
        }
    }, leadingContent = {
        if (settingIcon != null) {
            Icon(
                imageVector = settingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }, trailingContent = {
        Switch(checked = tx, onCheckedChange = {
            tx = !tx
            onToggleChange(tx)
        })
    })
}

@Composable
fun SettingsCameraButton(
    modifier: Modifier = Modifier,
    onImageCaptured: (Uri) -> Unit = {}
) {
    val context = LocalContext.current
    val photoUri = remember {
        val file = File(context.cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            onImageCaptured(photoUri)
        }
    }

    Button(
        onClick = { cameraLauncher.launch(photoUri) },
        modifier = modifier
    ) {
        Icon(Icons.Default.CameraAlt, contentDescription = "Open Camera")
        Spacer(Modifier.width(8.dp))
        Text("Open Camera")
    }
}
