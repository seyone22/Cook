package com.seyone22.cook.data.repository.mealEntry.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlusOne
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.seyone22.cook.R
import com.seyone22.cook.data.model.MealEntryWithDetails
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.screen.meals.MealsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealEntryBottomSheet(
    onDismiss: () -> Unit,
    mealEntry: MealEntryWithDetails,
    viewModel: MealsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    var notes by remember { mutableStateOf(mealEntry.entry.notes ?: "") }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    // Launch in expanded state
    LaunchedEffect(Unit) {
        sheetState.expand()
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.updateMealNotes(mealEntry.entry.id, notes)
            onDismiss()
        }, sheetState = sheetState, modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets(top = 64.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.ime) // makes space for keyboard
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = mealEntry.entry.imageUri,
                    placeholder = painterResource(id = R.drawable.placeholder),
                    error = painterResource(id = R.drawable.placeholder)
                ),
                contentDescription = "Meal Image",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                AssistChip(
                    onClick = { /* Handle tag click */ },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = ""

                            )},
                    label = { Text("Add Tag") },
                )
                mealEntry.tags.forEach { tag ->
                    AssistChip(onClick = { /* Handle tag click */ }, label = { Text(tag.name) })
                }
            }

            Text(
                text = "Date: ${mealEntry.entry.entryDate}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            if (mealEntry.entry.recipeId != null) {
                Text(
                    text = "View Linked Recipe",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable {
                            viewModel.navigateToRecipe(mealEntry.entry.recipeId)
                        })
            }

            OutlinedTextField(
                modifier = Modifier
                    .onFocusChanged {
                        if (it.isFocused) {
                        }
                    }
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                singleLine = false,
                maxLines = 5
            )
        }
    }
}
