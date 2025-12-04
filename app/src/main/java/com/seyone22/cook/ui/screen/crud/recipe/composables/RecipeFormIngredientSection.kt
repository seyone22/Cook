package com.seyone22.cook.ui.screen.crud.recipe.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.RecipeIngredientDetails
import java.util.UUID

/**
 * A Composable that renders the ingredient editing section of a recipe form.
 *
 * This section allows users to view, add, update, and remove ingredients from a recipe.
 * It consists of a header, a list of ingredient input rows (name, quantity, unit), and an "Add" button.
 * Each row provides specific interactions for ingredient details, including a callback to trigger an external search.
 *
 * @param recipeIngredients The current list of ingredient details to display.
 * @param allMeasures A list of available measurement units (e.g., "kg", "cup") used for autocomplete suggestions. Should only contain reference data, not the full database of ingredients.
 * @param onAddRecipeIngredient Callback invoked when the user clicks the "Add Ingredient" button. Provides a new, empty [RecipeIngredientDetails] object.
 * @param onUpdateRecipeIngredient Callback invoked when any field within a specific ingredient row is modified. Provides the index and the updated [RecipeIngredientDetails].
 * @param onRemoveRecipeIngredient Callback invoked when the remove/close icon is clicked on a specific row.
 * @param onTriggerIngredientSearch Callback invoked when the search icon within an ingredient name field is clicked. Provides the index of the row and the current name text, typically used to open a selection bottom sheet.
 * @param modifier The [Modifier] to be applied to the root Column of this section.
 */
@Composable
fun RecipeFormIngredientSection(
    recipeIngredients: List<RecipeIngredientDetails>,
    allMeasures: List<Measure?>, // Only pass small lists (Units), not DB Ingredients
    onAddRecipeIngredient: (RecipeIngredientDetails) -> Unit,
    onUpdateRecipeIngredient: (Int, RecipeIngredientDetails) -> Unit,
    onRemoveRecipeIngredient: (RecipeIngredientDetails) -> Unit,
    onTriggerIngredientSearch: (Int, String) -> Unit, // Callback to open BottomSheet
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        Text(
            text = "Ingredients",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // List of Rows
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            recipeIngredients.forEachIndexed { index, recipeIngredient ->
                IngredientInputRow(
                    ingredient = recipeIngredient,
                    allMeasures = allMeasures,
                    onUpdate = { updated -> onUpdateRecipeIngredient(index, updated) },
                    onRemove = { onRemoveRecipeIngredient(recipeIngredient) },
                    onSearchClick = { onTriggerIngredientSearch(index, recipeIngredient.name) })
            }
        }

        // Add Button
        FilledTonalButton(
            onClick = {
                onAddRecipeIngredient(
                    RecipeIngredientDetails(
                        recipeId = UUID.randomUUID(), // Temp
                        ingredientId = UUID.randomUUID(), // Temp
                        foodDbId = "", name = "", quantity = "", unit = "", notes = null
                    )
                )
            }, modifier = Modifier.fillMaxWidth(), contentPadding = ButtonDefaults.ContentPadding
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Add Ingredient")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientInputRow(
    ingredient: RecipeIngredientDetails,
    allMeasures: List<Measure?>,
    onUpdate: (RecipeIngredientDetails) -> Unit,
    onRemove: () -> Unit,
    onSearchClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 1. Name Field (Weight 1 - takes available space)
        OutlinedTextField(
            value = ingredient.name,
            onValueChange = { onUpdate(ingredient.copy(name = it)) },
            label = { Text("Ingredient") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next
            ),
            // The "Link" button to trigger search/matching
            leadingIcon = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Database",
                        tint = if (!ingredient.foodDbId.isNullOrBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            })

        // 2. Quantity (Fixed Width)
        OutlinedTextField(
            value = ingredient.quantity,
            onValueChange = { onUpdate(ingredient.copy(quantity = it)) },
            label = { Text("Qty") },
            modifier = Modifier.width(70.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next
            )
        )

        // 3. Unit (Fixed Width with Autocomplete)
        var unitExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = unitExpanded,
            onExpandedChange = { unitExpanded = !unitExpanded },
            modifier = Modifier.width(90.dp)
        ) {
            OutlinedTextField(
                value = ingredient.unit,
                onValueChange = {
                    onUpdate(ingredient.copy(unit = it))
                    unitExpanded = true // Keep menu open while typing to filter
                },
                label = { Text("Unit") },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                trailingIcon = null // Removed icon to save space
            )

            // Simple filtering for units
            if (unitExpanded) {
                val filteredUnits = allMeasures.filter {
                    it?.abbreviation?.contains(
                        ingredient.unit, ignoreCase = true
                    ) == true || it?.name?.contains(ingredient.unit, ignoreCase = true) == true
                }

                if (filteredUnits.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                        filteredUnits.forEach { measure ->
                            measure?.let {
                                DropdownMenuItem(text = { Text(it.abbreviation) }, onClick = {
                                    onUpdate(ingredient.copy(unit = it.abbreviation))
                                    unitExpanded = false
                                })
                            }
                        }
                    }
                }
            }
        }

        // 4. Remove Button (Aligned Center Vertically)
        IconButton(
            onClick = onRemove,
            modifier = Modifier.padding(top = 8.dp) // Align visually with text fields
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
fun RecipeFormIngredientSectionPreview() {
    // Mock Data for Preview
    val mockMeasures = listOf(
        Measure(id = 1, name = "Kilogram", abbreviation = "kg", type = ""),
        Measure(id = 2, name = "Gram", abbreviation = "g", ""),
        Measure(id = 3, name = "Cup", abbreviation = "cup", ""),
        Measure(id = 4, name = "Tablespoon", abbreviation = "tbsp", ""),
        Measure(id = 5, name = "Teaspoon", abbreviation = "tsp", "")
    )

    val mockIngredients = listOf(
        RecipeIngredientDetails(
            recipeId = UUID.randomUUID(),
            ingredientId = UUID.randomUUID(),
            foodDbId = "123", // Simulate a linked ingredient
            name = "Flour",
            quantity = "2",
            unit = "cup",
            notes = null
        ),
        RecipeIngredientDetails(
            recipeId = UUID.randomUUID(),
            ingredientId = UUID.randomUUID(),
            foodDbId = "", // Simulate an unlinked ingredient
            name = "Sugar",
            quantity = "100",
            unit = "g",
            notes = null
        )
    )

    Surface {
        RecipeFormIngredientSection(
            recipeIngredients = mockIngredients,
            allMeasures = mockMeasures,
            onAddRecipeIngredient = {},
            onUpdateRecipeIngredient = { _, _ -> },
            onRemoveRecipeIngredient = {},
            onTriggerIngredientSearch = { _, _ -> }
        )
    }
}