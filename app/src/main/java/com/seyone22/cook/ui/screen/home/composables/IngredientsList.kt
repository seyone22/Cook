package com.seyone22.cook.ui.screen.home.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.IngredientProduct
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.ui.screen.home.HomeViewModel
import com.seyone22.cook.ui.screen.home.detail.RecipeDetailViewModel
import com.seyone22.cook.ui.screen.ingredients.detail.IngredientDetailDestination

@Composable
fun IngredientsList(
    navController: NavController,
    list: List<RecipeIngredient>,
    measures: List<Measure>,
    ingredients: List<Ingredient>,
    variants: List<IngredientProduct?>,
    scaleFactor: Double,
    serves: Int,
    modifier: Modifier = Modifier,
    ingredientPrices: Map<String, Double>, // <--- ADD THIS
) {
    var qtySelected by remember { mutableStateOf(true) }

    Column(modifier = Modifier.padding(bottom = 16.dp)) {

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(onClick = { qtySelected = !qtySelected }) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (!qtySelected) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.elevatedCardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                list.forEach { recipeIngredient ->

                    val checked = remember {
                        mutableStateOf(
                            false
                        )
                    }

                    val safeServes = if (serves == 0) 1 else serves
                    val quantity = (recipeIngredient.quantity / safeServes) * scaleFactor

                    // Read directly from the passed map
                    val price = ingredientPrices[recipeIngredient.foodDbId] ?: 0.0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Checkbox + Name
                            Row(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(end = 8.dp),
                                    enabled = true,
                                    checked = checked.value,
                                    onCheckedChange = { checked.value = !checked.value })

                                Text(
                                    modifier = Modifier.clickable {
                                        navController.navigate("${IngredientDetailDestination.route}/${recipeIngredient.foodDbId}")
                                    },
                                    text = recipeIngredient.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        textDecoration = if (checked.value) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    color = if (checked.value) MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = 0.5f
                                    )
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            val color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)

                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .drawBehind {
                                        val strokeWidth = 2f
                                        val dash = 2f
                                        val gap = 8f
                                        val y = 18f
                                        drawLine(
                                            color = color,
                                            start = androidx.compose.ui.geometry.Offset(0f, y),
                                            end = androidx.compose.ui.geometry.Offset(
                                                size.width, y
                                            ),
                                            strokeWidth = strokeWidth,
                                            cap = StrokeCap.Round,
                                            pathEffect = PathEffect.dashPathEffect(
                                                floatArrayOf(
                                                    dash, gap
                                                ), 0f
                                            )
                                        )
                                    })

                            // Quantity / Price
                            Text(
                                modifier = Modifier
                                    .padding(start = 8.dp, end = 8.dp)
                                    .wrapContentWidth()
                                    .align(Alignment.CenterVertically),
                                text = if (qtySelected) {
                                    "${
                                        String.format(
                                            "%.2f", quantity
                                        )
                                    }${recipeIngredient.unit}"
                                } else {
                                    "Rs.${String.format("%.2f", price)}"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
