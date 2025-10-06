package com.seyone22.cook.ui.screen.crud.recipe.composables

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.InstructionSection
import java.util.UUID


@Composable
fun RecipeFormInstructionSection(
    instructions: List<Instruction>,
    instructionSections: List<InstructionSection>,
    onAddInstruction: (Instruction) -> Unit,
    onUpdateInstruction: (Int, Instruction) -> Unit,
    onRemoveInstruction: (Instruction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Instructions",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 36.dp, bottom = 8.dp)
        )
        instructions.forEachIndexed { index, instruction ->
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 36.dp)
                ) {
                    Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                        Text(
                            modifier = Modifier.padding(8.dp, 0.dp, 16.dp, 0.dp),
                            text = (index + 1).toString(), // Step number from index
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    OutlinedTextField(
                        modifier = Modifier.width(310.dp),
                        value = instruction.description,
                        onValueChange = { newInstructionDescription ->
                            onUpdateInstruction(
                                index, instruction.copy(description = newInstructionDescription)
                            )
                        },
                        label = { Text("Describe the step") },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                    )
                    Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                        IconButton(
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp),
                            onClick = { onRemoveInstruction(instruction) },
                            content = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Instruction",
                                )
                            })
                    }
                }
            }
        }
        TextButton(
            modifier = Modifier.padding(start = 36.dp), onClick = {
                onAddInstruction(
                    Instruction(
                        description = "",
                        stepNumber = instructions.size + 1,
                        recipeId = UUID.randomUUID(), // Temp, will be updated on save
                        sectionId = null
                    )
                )
            }) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
            Text(text = "Add Step")
        }
    }
}