package com.seyone22.cook.ui.screen.crud.recipe.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.seyone22.cook.data.model.RecipeIngredientDetails
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeFormIngredientSection(
    recipeIngredients: List<RecipeIngredientDetails>,
    allIngredients: List<com.seyone22.cook.data.model.Ingredient?>,
    allMeasures: List<com.seyone22.cook.data.model.Measure?>,
    onAddRecipeIngredient: (RecipeIngredientDetails) -> Unit,
    onUpdateRecipeIngredient: (Int, RecipeIngredientDetails) -> Unit,
    onRemoveRecipeIngredient: (RecipeIngredientDetails) -> Unit,
    onNavigateToAddIngredient: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Ingredients",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 36.dp, bottom = 8.dp)
        )
        recipeIngredients.forEachIndexed { index, recipeIngredient ->
            var measuresExpanded by remember { mutableStateOf(false) }
            var ingredientExpanded by remember { mutableStateOf(false) }

            var ingredientFilter by remember {
                mutableStateOf(
                    allIngredients.find { it?.id == recipeIngredient.ingredientId }?.nameEn ?: ""
                )
            }

            val filteredIngredients = allIngredients.filter {
                (it?.nameEn ?: "").contains(ingredientFilter, true)
            }

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 36.dp)
                ) {
                    Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                        Icon(
                            modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp),
                            imageVector = Icons.Outlined.Tag,
                            contentDescription = null,
                        )
                    }
                    Row {
                        ExposedDropdownMenuBox(
                            expanded = ingredientExpanded, onExpandedChange = {
                                ingredientExpanded = !ingredientExpanded
                            }) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .padding(
                                        0.dp, 0.dp, 8.dp, 0.dp
                                    )
                                    .width(156.dp)
                                    .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                                    .clickable(enabled = true) {
                                        ingredientExpanded = true
                                    },
                                value = ingredientFilter,
                                onValueChange = { v -> ingredientFilter = v },
                                label = { Text("Ingredient") },
                                singleLine = true,
                                trailingIcon = {
                                    TrailingIcon(expanded = ingredientExpanded)
                                })

                            ExposedDropdownMenu(
                                expanded = ingredientExpanded,
                                onDismissRequest = { ingredientExpanded = false }) {
                                if (filteredIngredients.isNotEmpty()) {
                                    filteredIngredients.forEach { ingredient ->
                                        ingredient?.let {
                                            DropdownMenuItem(
                                                text = { Text(ingredient.nameEn) },
                                                onClick = {
                                                    ingredientFilter = ingredient.nameEn
                                                    onUpdateRecipeIngredient(
                                                        index, recipeIngredient.copy(
                                                            ingredientId = ingredient.id
                                                        )
                                                    )
                                                    ingredientExpanded = false
                                                })
                                        }
                                    }
                                } else {
                                    DropdownMenuItem(
                                        text = { Text("Add $ingredientFilter to database") },
                                        onClick = {
                                            onNavigateToAddIngredient(ingredientFilter)
                                            ingredientExpanded = false
                                            ingredientFilter = ""
                                        })
                                }
                            }
                        }
                        OutlinedTextField(
                            modifier = Modifier
                                .width(64.dp)
                                .padding(0.dp, 0.dp, 8.dp, 0.dp),
                            value = recipeIngredient.quantity,
                            singleLine = true,
                            onValueChange = { newQty ->
                                onUpdateRecipeIngredient(
                                    index, recipeIngredient.copy(quantity = newQty)
                                )
                            },
                            label = { Text("No") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
                            )
                        )
                        ExposedDropdownMenuBox(
                            expanded = measuresExpanded, onExpandedChange = {
                                measuresExpanded = !measuresExpanded
                            }) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .padding(
                                        0.dp, 0.dp, 8.dp, 0.dp
                                    )
                                    .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                                    .width(80.dp)
                                    .clickable(enabled = true) {
                                        measuresExpanded = true
                                    },
                                value = allMeasures.find { m -> m?.id == recipeIngredient.measureId }?.abbreviation
                                    ?: "",
                                readOnly = true,
                                onValueChange = { },
                                label = { Text("Unit") }, // Changed label to "Unit"
                                singleLine = true,
                                trailingIcon = {
                                    TrailingIcon(expanded = measuresExpanded)
                                })

                            ExposedDropdownMenu(
                                expanded = measuresExpanded,
                                onDismissRequest = { measuresExpanded = false }) {
                                allMeasures.forEach { measure ->
                                    measure?.let {
                                        DropdownMenuItem(
                                            text = { Text(measure.abbreviation) },
                                            onClick = {
                                                onUpdateRecipeIngredient(
                                                    index,
                                                    recipeIngredient.copy(measureId = measure.id)
                                                )
                                                measuresExpanded = false
                                            })
                                    }
                                }
                            }
                        }
                    }
                    Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                        IconButton(
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp),
                            onClick = { onRemoveRecipeIngredient(recipeIngredient) },
                            content = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Ingredient",
                                )
                            })
                    }
                }
            }
        }

        TextButton(
            modifier = Modifier.padding(start = 36.dp), onClick = {
                onAddRecipeIngredient(
                    RecipeIngredientDetails(
                        ingredientId = UUID.randomUUID(), // Temporarily random, will be updated when selected
                        measureId = -1, // No measure selected initially
                        quantity = "",
                        recipeId = UUID.randomUUID() // Temporarily random, will be updated on save
                    )
                )
            }) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
            Text(text = "Add Ingredient")
        }
    }
}