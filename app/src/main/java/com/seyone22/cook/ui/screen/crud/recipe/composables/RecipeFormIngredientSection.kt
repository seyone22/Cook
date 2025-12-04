package com.seyone22.cook.ui.screen.crud.recipe.composables

import android.util.Log
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.Measure
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
    Log.d("RecipeFormIngredientSection", "recipeIngredients: $recipeIngredients")


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
                    allIngredients.find { it?.id == recipeIngredient.ingredientId }?.name ?: ""
                )
            }

            val filteredIngredients = allIngredients.filter {
                (it?.name ?: "").contains(ingredientFilter, true)
            }

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 36.dp)
                ) {
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
                                                text = { Text(ingredient.name) },
                                                onClick = {
                                                    ingredientFilter = ingredient.name
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
                                value = recipeIngredient.unit,
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
                        quantity = "",
                        recipeId = UUID.randomUUID(), // Temporarily random, will be updated on save
                        unit = "",
                        foodDbId = "",
                        name = "",
                        notes = null
                    )
                )
            }) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
            Text(text = "Add Ingredient")
        }
    }
}



@Preview(showBackground = true)
@Composable
fun RecipeFormIngredientSectionPreview() {
    val sampleIngredients = listOf(
        Ingredient(id = UUID.randomUUID(), name = "Red Apple"),
        Ingredient(id = UUID.randomUUID(), name = "Lemon Juice"),
        Ingredient(id = UUID.randomUUID(), name = "Granulated Sugar"),
        Ingredient(id = UUID.randomUUID(), name = "Butter"),
        Ingredient(id = UUID.randomUUID(), name = "Flour")
    )

    val sampleMeasures = listOf(
        Measure(id = 3, abbreviation = "g", name = "gram", type = "weight"),
        Measure(id = 4, abbreviation = "kg", name = "kilogram", type = "weight"),
        Measure(id = 5, abbreviation = "cup", type = "volume", name = "cup"),
        Measure(id = 6, abbreviation = "tbsp", name = "tablespoon", type = "volume"),
        Measure(id = 7, abbreviation = "tsp", name = "teaspoon", type = "volume"),
        Measure(id = 8, abbreviation = "lb", name = "pound", type = "weight")
    )

    val sampleRecipeIngredients =remember{mutableStateListOf(
        RecipeIngredientDetails(
            id = 0,
            recipeId = UUID.randomUUID(),
            ingredientId = sampleIngredients[0].id,
            foodDbId = UUID.randomUUID().toString(),
            name = "Red Apple",
            quantity = "3.0",
            unit = "lb",
            notes = "peeled, cored, thinly sliced"
        ),
        RecipeIngredientDetails(
            id = 1,
            recipeId = UUID.randomUUID(),
            ingredientId = sampleIngredients[1].id,
            foodDbId = UUID.randomUUID().toString(),
            name = "Lemon Juice",
            quantity = "2.0",
            unit = "tbsp",
            notes = null
        ),
        RecipeIngredientDetails(
            id = 2,
            recipeId = UUID.randomUUID(),
            ingredientId = sampleIngredients[2].id,
            foodDbId = UUID.randomUUID().toString(),
            name = "Granulated Sugar",
            quantity = "1.0",
            unit = "cup",
            notes = null
        )
    )}

    RecipeFormIngredientSection(
        recipeIngredients = sampleRecipeIngredients,
        allIngredients = sampleIngredients,
        allMeasures = sampleMeasures,
        onAddRecipeIngredient = { sampleRecipeIngredients.add(it) },
        onUpdateRecipeIngredient = { index, updated ->
            sampleRecipeIngredients[index] = updated
        },
        onRemoveRecipeIngredient = { sampleRecipeIngredients.remove(it) },
        onNavigateToAddIngredient = { name -> println("Navigate to add ingredient: $name") }
    )
}