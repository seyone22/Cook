package com.seyone22.cook.ui.screen.ingredients.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.IngredientImage

@Composable
fun IngredientListItem(modifier: Modifier, ingredient: Ingredient, image: IngredientImage?) {
    Card(modifier = modifier.padding(4.dp)) {
        Column {
            if (image != null) {
                AsyncImage(
                    model = image.imagePath,
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = modifier
                        .fillMaxWidth()
                        .aspectRatio(1f) // Maintain aspect ratio
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            Text(
                text = ingredient.nameEn,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = modifier.padding(8.dp),
            )
        }
    }
}