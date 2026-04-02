package com.seyone22.cook.ui.screen.home.composables

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.seyone22.cook.data.model.Recipe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    recipe: Recipe,
    onDismiss: () -> Unit,
    onExportZip: () -> Unit,
    onSaveCloudSettings: (shareMode: String, allowedEmails: List<String>) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    var currentMode by remember { mutableStateOf(recipe.shareMode) }
    var allowedEmails by remember { mutableStateOf(recipe.allowedEmails) }
    var emailInput by remember { mutableStateOf("") }

    val hasUnsavedChanges = currentMode != recipe.shareMode || allowedEmails != recipe.allowedEmails

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.windowInsetsPadding(WindowInsets.ime)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Share Recipe",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // --- OPTION 1: MANUAL EXPORT ---
            OutlinedCard(
                onClick = {
                    onExportZip()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Archive, contentDescription = "Zip", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Export as ZIP", fontWeight = FontWeight.SemiBold)
                        Text("Share manually via email or chat", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // --- OPTION 2: CLOUD QUICK SHARE ---
            Text(
                text = "Cloud Quick Share",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (recipe.firestoreId == null) {
                // Cannot share until backed up
                OutlinedCard(
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudOff, contentDescription = "No Cloud", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "You must back up this recipe to the cloud before you can create a Quick Share link.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            } else {
                // Segmented Control for Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = currentMode == "PRIVATE",
                        onClick = { currentMode = "PRIVATE" },
                        label = { Text("Private") },
                        leadingIcon = { if (currentMode == "PRIVATE") Icon(Icons.Default.Lock, null) }
                    )
                    FilterChip(
                        selected = currentMode == "ANYONE",
                        onClick = { currentMode = "ANYONE" },
                        label = { Text("Anyone w/ Link") },
                        leadingIcon = { if (currentMode == "ANYONE") Icon(Icons.Default.Public, null) }
                    )
                    FilterChip(
                        selected = currentMode == "SPECIFIC",
                        onClick = { currentMode = "SPECIFIC" },
                        label = { Text("Specific Users") },
                        leadingIcon = { if (currentMode == "SPECIFIC") Icon(Icons.Default.Group, null) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Email Entry for Specific Users
                if (currentMode == "SPECIFIC") {
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Add email address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (emailInput.isNotBlank() && !allowedEmails.contains(emailInput.lowercase())) {
                                allowedEmails = allowedEmails + emailInput.lowercase()
                                emailInput = ""
                            }
                        }),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (emailInput.isNotBlank() && !allowedEmails.contains(emailInput.lowercase())) {
                                    allowedEmails = allowedEmails + emailInput.lowercase()
                                    emailInput = ""
                                }
                            }) {
                                Icon(Icons.Default.Add, "Add")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (allowedEmails.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(allowedEmails) { email ->
                                InputChip(
                                    selected = false,
                                    onClick = { },
                                    label = { Text(email) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { allowedEmails = allowedEmails - email }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // The Link Generator
                if (currentMode != "PRIVATE") {
                    val shareLink = "cookapp://shared/${recipe.firestoreId}"

                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = "QR")
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Share Link", fontWeight = FontWeight.Bold)
                                Text(shareLink, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                            IconButton(onClick = {
                                val clip = ClipData.newPlainText("Recipe Link", shareLink)
                                clipboardManager.setPrimaryClip(clip)
                                Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button (Only enable if changes were made)
                Button(
                    onClick = { onSaveCloudSettings(currentMode, allowedEmails) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = hasUnsavedChanges
                ) {
                    Icon(Icons.Default.CloudSync, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save & Update Cloud")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}