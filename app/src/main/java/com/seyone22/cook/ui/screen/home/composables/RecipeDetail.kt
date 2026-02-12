package com.seyone22.cook.ui.screen.home.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.InstructionSection
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.parser.decodeHtmlEntities

@Composable
fun ExpandableDescription(
    text: String?, previewCharCount: Int = 120, modifier: Modifier
) {
    if (text.isNullOrBlank()) {
        Text(
            modifier = modifier.padding(8.dp),
            text = "No description given.",
            fontStyle = FontStyle.Italic,
            color = Color.Gray
        )
    } else {
        var expanded by remember { mutableStateOf(false) }
        val trimmed = decodeHtmlEntities(text).trim()

        if (expanded || trimmed.length <= previewCharCount) {
            // Show full text
            Text(
                text = trimmed,
                style = MaterialTheme.typography.bodyLarge,
                modifier = modifier.padding(vertical = 8.dp)
            )
        } else {
            // Show preview + "View More"
            val preview = trimmed.take(previewCharCount)
            val annotated = buildAnnotatedString {
                append(preview)
                append("…")
                pushStringAnnotation(tag = "view_more", annotation = "view_more")
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append("View More")
                }
                pop()
            }

            ClickableText(
                text = annotated,
                style = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
                modifier = modifier.padding(vertical = 8.dp),
                onClick = { offset ->
                    annotated.getStringAnnotations(tag = "view_more", start = offset, end = offset)
                        .firstOrNull()?.let {
                            expanded = true
                        }
                })
        }
    }
}


@Composable
fun RecipeStats(
    recipe: Recipe, scaleFactor: Double, cost: Double, modifier: Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        RecipeStat(
            icon = Icons.Default.Check, text = "${recipe.timesMade} times"
        )
        RecipeStat(
            icon = Icons.Outlined.Timer, text = "${recipe.prepTime} mins"
        )
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp, 8.dp),
        horizontalArrangement = Arrangement.Start,
    ) {
        RecipeStat(
            icon = Icons.Default.PriceCheck, text = "Rs. ${cost}"
        )
        RecipeStat(
            icon = Icons.Default.RiceBowl, text = "Serves ${scaleFactor.toInt()}"
        )
    }
}

@Composable
fun RecipeStat(
    icon: ImageVector, text: String, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.width(200.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .background(color = MaterialTheme.colorScheme.secondaryFixedDim)
                .padding(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSecondaryFixed
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
fun InstructionList(
    list: List<Instruction?>, sections: List<InstructionSection?>, modifier: Modifier = Modifier
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    Column(modifier = modifier.padding(0.dp)) {
        sections.forEach { section ->
            // Filter instructions for this section
            val sectionInstructions = list.filter { it?.sectionId == section?.sectionNumber }

            if (sectionInstructions.isNotEmpty()) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.elevatedCardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Section title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = section?.name ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Instructions in this section
                        sectionInstructions.forEachIndexed { index, instruction ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                // Step number in a circle
                                if (sectionInstructions.size > 1) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.size(32.dp),
                                        tonalElevation = 2.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${(instruction?.stepNumber ?: 0) + 1}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Instruction description
                                Text(
                                    text = instruction?.description ?: "",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .weight(1f)
                                        .combinedClickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null, // remove ripple on long click
                                            onClick = { /* optional single-click behavior */ },
                                            onLongClick = {
                                                clipboardManager.setText(
                                                    AnnotatedString(instruction?.description ?: "")
                                                )
                                            })
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}