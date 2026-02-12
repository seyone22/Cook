package com.seyone22.cook.ui.screen.crud.recipe.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.seyone22.cook.data.model.Tag
import com.seyone22.cook.data.model.TagType
import com.seyone22.cook.ui.screen.crud.recipe.RecipeFormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeFormFieldsSection(
    formState: RecipeFormState,
    onFormStateChange: (RecipeFormState) -> Unit,
    allTags: List<Tag> = emptyList(),
    navController: NavController? = null
) {
    var tagFilter by remember { mutableStateOf("") }
    var tagsExpanded by remember { mutableStateOf(false) }

    // Section for general data (name)
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            Icon(
                modifier = Modifier.padding(end = 12.dp),
                imageVector = Icons.Outlined.Edit,
                contentDescription = null,
            )
        }
        OutlinedTextField(
            modifier = Modifier.width(310.dp),
            value = formState.name,
            onValueChange = { onFormStateChange(formState.copy(name = it)) },
            label = { Text("Name") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )
    }

    // Section for quick details
    Column(
        modifier = Modifier
            .width(346.dp)
            .padding(start = 36.dp)
    ) {
        Row {
            OutlinedTextField(
                value = formState.prepTime,
                onValueChange = { onFormStateChange(formState.copy(prepTime = it.filter { it.isDigit() })) },
                label = { Text("Prep") },
                modifier = Modifier
                    .width(107.dp)
                    .padding(end = 8.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
                )
            )
            OutlinedTextField(
                value = formState.cookTime,
                onValueChange = { onFormStateChange(formState.copy(cookTime = it.filter { it.isDigit() })) },
                label = { Text("Cook") },
                modifier = Modifier
                    .width(107.dp)
                    .padding(end = 8.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
                )
            )
            OutlinedTextField(
                value = formState.servingSize,
                onValueChange = { onFormStateChange(formState.copy(servingSize = it.filter { it.isDigit() })) },
                label = { Text("Serves") },
                modifier = Modifier.width(107.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
                )
            )
        }

        OutlinedTextField(
            value = formState.description,
            onValueChange = { onFormStateChange(formState.copy(description = it)) },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            modifier = Modifier.width(310.dp),
            value = formState.reference,
            onValueChange = { onFormStateChange(formState.copy(reference = it)) },
            label = { Text("Reference URL") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )

        // Tag selector
        ExposedDropdownMenuBox(
            expanded = tagsExpanded,
            onExpandedChange = { tagsExpanded = !tagsExpanded }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                    .clickable { tagsExpanded = true },
                value = tagFilter,
                onValueChange = { tagFilter = it },
                label = { Text("Select a tag") },
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tagsExpanded) }
            )

            val filteredTags = allTags.filter {
                it.name.contains(tagFilter, ignoreCase = true)
            }

            ExposedDropdownMenu(expanded = tagsExpanded, onDismissRequest = { tagsExpanded = false }) {
                if (filteredTags.isNotEmpty()) {
                    filteredTags.forEach { tag ->
                        DropdownMenuItem(
                            text = { Text(tag.name) },
                            onClick = {
                                if (!formState.recipeTags.any { it.id == tag.id }) {
                                    formState.recipeTags.add(tag)
                                }
                                tagFilter = ""
                                tagsExpanded = false
                            }
                        )
                    }
                } else if (tagFilter.isNotBlank() && navController != null) {
                    DropdownMenuItem(
                        text = { Text("Add $tagFilter to database") },
                        onClick = {
                            navController.navigate("Add Tag/$tagFilter")
                        }
                    )
                }
            }
        }

        LazyRow {
            formState.recipeTags.forEach { tag ->
                item(key = tag.name) {
                    FilterChip(
                        modifier = Modifier.padding(end = 4.dp),
                        selected = true,
                        onClick = {
                            formState.recipeTags.remove(tag)
                        },
                        label = { Text(text = tag.name) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Tag"
                            )
                        }
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}
