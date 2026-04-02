package com.seyone22.cook.ui.screen.ingredients.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.IngredientImage

@Composable
fun IngredientListItem(
    modifier: Modifier = Modifier,
    ingredient: Ingredient,
    image: IngredientImage?
) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp), // More expressive "extra large" corners
        modifier = modifier.padding(6.dp),
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Image Section
                if (image != null) {
                    AsyncImage(
                        model = image.imagePath,
                        contentScale = ContentScale.Crop,
                        contentDescription = ingredient.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.2f) // Slightly wider than square
                            .clip(RoundedCornerShape(24.dp))
                    )
                }

                // Overlaid Category Badge
                if (ingredient.category.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = ingredient.category,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Show Price and Unit if available
                if (ingredient.price > 0) {
                    Text(
                        text = "${ingredient.currency} ${ingredient.price} / ${ingredient.product_unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}