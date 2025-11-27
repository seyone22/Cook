package com.seyone22.cook.ui.screen.home.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeImage
import java.time.Instant
import java.util.UUID
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RecipeCard(modifier: Modifier, recipe: Recipe, image: RecipeImage?) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(216.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (image != null) {
                AsyncImage(
                    model = image.imagePath,
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0x99000000)),
                            startY = 0f,
                            endY = 500f // Adjust the endY for your gradient effect
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp, 0.dp, 0.dp, 16.dp)
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = Color.White,
                )
                Row {
                    Text(
                        text = "${recipe.prepTime + recipe.cookTime} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                    )
                    Text(
                        text = " • ${recipe.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
fun RecipeCardPreview() {
    val recipe = Recipe(
        name = "Spaghetti Carbonara",
        description = "A classic Italian pasta dish.",
        prepTime = 10,
        cookTime = 15,
        servingSize = 4,
        author = "Seyone",
        id = UUID.randomUUID(),
        reference = "",
        timesMade = 3,
        videoUrl = "",
        dateCreated = Instant.now(),
        dateModified = Instant.now(),
        dateAccessed = Instant.now(),
    )
    RecipeCard(
        modifier = Modifier, recipe = recipe, image = null
    )
}