package com.seyone22.cook.ui.common.dialog.action

import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.SharedViewModel
import com.seyone22.cook.data.DialogAction
import com.seyone22.cook.data.DialogStyle
import com.seyone22.cook.ui.screen.crud.recipe.ImportRecipeDestination
import kotlinx.coroutines.launch

class ImportRecipeUrlDialogAction(
    private val sharedViewModel: SharedViewModel,
    private val navigateToScreen: (String) -> Unit,
    private val onDismiss: () -> Unit,
    override val style: DialogStyle = DialogStyle.BASIC,
    override val modifier: Modifier = Modifier,
    val context: Context
) : DialogAction {

    override val title: String = "Import Recipe from URL"
    override val message: String = ""

    private var urlText by mutableStateOf("")

    override val content: @Composable () -> Unit = {
        val context = LocalContext.current
        val clipboardManager = LocalClipboardManager.current

        // Automatically capture clipboard if it contains a URL
        LaunchedEffect(Unit) {
            val clip = clipboardManager.getText()?.text
            if (clip != null && clip.startsWith("http")) {
                urlText = clip
            }
        }

        OutlinedTextField(
            value = urlText,
            onValueChange = { urlText = it },
            placeholder = { Text("Enter recipe URL") },
            singleLine = true,
            trailingIcon = {
                if (urlText.isNotEmpty()) {
                    IconButton(onClick = { urlText = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close, contentDescription = "Clear text"
                        )
                    }
                }
            })
    }

    override fun onConfirm() {
        if (!urlText.startsWith("http")) {
            Toast.makeText(context, "Please enter a valid URL", Toast.LENGTH_SHORT).show()
            return
        }

        sharedViewModel.viewModelScope.launch {
            val success = sharedViewModel.importAndSaveRecipe(urlText)
            if (success) {
                Toast.makeText(context, "Recipe imported!", Toast.LENGTH_SHORT).show()
                navigateToScreen(ImportRecipeDestination.route)
            } else {
                Toast.makeText(context, "Failed to fetch recipe", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCancel() {
        onDismiss()
    }
}
