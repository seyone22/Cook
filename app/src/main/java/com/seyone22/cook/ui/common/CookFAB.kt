package com.seyone22.cook.ui.common

import android.content.Context
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seyone22.cook.SharedViewModel
import com.seyone22.cook.helper.rememberCameraCaptureManager
import com.seyone22.cook.service.parseRecipeViaBackend
import com.seyone22.cook.service.recognizeTextFromUri
import com.seyone22.cook.service.rememberDocumentScannerLauncher
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.common.dialog.action.ImportRecipeUrlDialogAction
import com.seyone22.cook.ui.screen.crud.ingredient.AddIngredientDestination
import com.seyone22.cook.ui.screen.crud.recipe.AddRecipeDestination
import com.seyone22.cook.ui.screen.crud.recipe.ImportRecipeDestination
import com.seyone22.cook.ui.screen.home.HomeDestination
import com.seyone22.cook.ui.screen.ingredients.IngredientsDestination
import com.seyone22.cook.ui.screen.meals.MealsViewModel
import com.seyone22.cook.ui.screen.more.MoreViewModel
import com.seyone22.cook.ui.screen.shoppingList.ShoppingListDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import recipeimporter.model.Recipe

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CookFAB(
    currentActivity: String?,
    navigateToScreen: (String) -> Unit = {},
    // `action` parameter is not used in the original code, consider removing or integrating
    // action: () -> Unit = {},
    visible: Boolean = true,
    viewModel: MealsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    moreViewModel: MoreViewModel = viewModel(factory = AppViewModelProvider.Factory),
    sharedViewModel: SharedViewModel = viewModel(factory = AppViewModelProvider.Factory),
    scope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current
) {
    var expanded by remember { mutableStateOf(false) } // This controls the FAB and the menu
    val focusRequester = remember { FocusRequester() }

    val cameraCaptureManager = rememberCameraCaptureManager { uri ->
        viewModel.addImageMealEntry(uri.toString()) { newId ->
            // Handle success, e.g., navigate to the new meal entry
        }
    }

    val launchScanner = rememberDocumentScannerLauncher(
        pageLimit = 1,
        allowGalleryImport = true,
        onSuccess = { imageUris, pdfUri ->
            val uri = imageUris.firstOrNull() ?: pdfUri ?: return@rememberDocumentScannerLauncher
            // run OCR + parse in coroutine
            scope.launch {
                try {
                    val rawText = recognizeTextFromUri(context, uri)
                    Log.d("TAG", "CookFAB: $rawText")
                    //val parsedJsonLd = recipeimporter.parser.JsonLdExtractor(rawText)
                    // store into SharedViewModel and navigate
                    sharedViewModel.saveImportedRecipeByCamera(rawText)
                    navigateToScreen(ImportRecipeDestination.route)
                } catch (e: Exception) {
                    // show error to user
                }
            }
        },
        onError = { throwable ->
            // show error
        }
    )

    val items = remember {
        listOf(
            Triple(Icons.Filled.Archive, "Menu", "add_menu_route"), // Placeholder route

            Triple(Icons.AutoMirrored.Filled.Message, "Recipe", AddRecipeDestination.route),

            Triple(
                Icons.Filled.Snooze, "Shopping List", ShoppingListDestination.route
            ), // Assuming this navigates to add new
            Triple(
                Icons.Filled.Camera, "Recipe (Camera)", "capture_recipe_photo"
            ),
            Triple(
                Icons.Filled.People, "Recipe (URL)", "recipe_by_url_route"
            ), // Placeholder route
            Triple(Icons.Filled.Contacts, "Ingredient", AddIngredientDestination.route),
        )
    }

    if (visible) {
        if ((currentActivity == HomeDestination.route) || (currentActivity == IngredientsDestination.route)) {
            FloatingActionButtonMenu(
                expanded = expanded, // Control menu expansion with the same 'expanded' state
                button = {
                    ToggleFloatingActionButton(
                        modifier = Modifier
                            .semantics {
                                traversalIndex = -1f
                                stateDescription = if (expanded) "Expanded" else "Collapsed"
                            }
                            .animateFloatingActionButton(
                                visible = visible || expanded, // FAB should be visible if `visible` or if menu is expanded
                                alignment = Alignment.BottomEnd,
                            )
                            .focusRequester(focusRequester),
                        checked = expanded,
                        onCheckedChange = { expanded = it }, // Update 'expanded' directly
                    ) {
                        // Use the 'expanded' state directly for icon animation
                        val imageVector by remember {
                            derivedStateOf {
                                if (expanded) Icons.Filled.Close else Icons.Filled.Add
                            }
                        }
                        // ToggleFloatingActionButtonDefaults.animateIcon is typically used within the slot it provides
                        Icon(
                            imageVector = imageVector,
                            tint = Color.White,
                            contentDescription = if (expanded) "Close menu" else "Open menu", // Meaningful content description
                        )
                    }
                },
            ) {
                items.forEachIndexed { i, item ->
                    FloatingActionButtonMenuItem(
                        modifier = Modifier
                            .semantics {
                                isTraversalGroup = true
                                if (i == items.size - 1) {
                                    customActions = listOf(
                                        CustomAccessibilityAction(
                                            label = "Close menu",
                                            action = {
                                                expanded = false // Use 'expanded' here
                                                true
                                            },
                                        )
                                    )
                                }
                            }
                            .then(
                                if (i == 0) {
                                    Modifier.onKeyEvent {
                                        if (it.type == KeyEventType.KeyDown && (it.key == Key.DirectionUp || (it.isShiftPressed && it.key == Key.Tab))) {
                                            focusRequester.requestFocus()
                                            return@onKeyEvent true
                                        }
                                        return@onKeyEvent false
                                    }
                                } else {
                                    Modifier
                                }),
                        onClick = {
                            expanded = false // Close menu on item click
                            when (item.third) { // Use the route for navigation
                                AddRecipeDestination.route -> navigateToScreen(AddRecipeDestination.route)
                                AddIngredientDestination.route -> navigateToScreen(
                                    AddIngredientDestination.route
                                )

                                ShoppingListDestination.route -> navigateToScreen(
                                    ShoppingListDestination.route
                                )

                                "recipe_by_url_route" -> {
                                    sharedViewModel.showDialog(
                                        ImportRecipeUrlDialogAction(
                                            sharedViewModel = sharedViewModel,
                                            navigateToScreen = navigateToScreen,
                                            context = context,
                                            onDismiss = { }
                                        )
                                    )
                                }

                                "capture_recipe_photo" -> {
                                    launchScanner()
                                }
                                // Add more navigation cases for other items
                                else -> navigateToScreen(item.third) // Navigate to placeholder routes
                            }
                        },
                        icon = {
                            Icon(
                                item.first, contentDescription = null
                            )
                        }, // Add meaningful descriptions for each icon if needed
                        text = { Text(text = item.second) },
                    )
                }
            }
        }
    }
}