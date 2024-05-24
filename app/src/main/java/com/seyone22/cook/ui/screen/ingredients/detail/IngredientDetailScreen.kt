package com.seyone22.cook.ui.screen.ingredients.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.helper.ImageHelper
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.navigation.NavigationDestination
import com.seyone22.cook.ui.screen.ingredients.IngredientsViewModel
import java.io.File

object IngredientDetailDestination : NavigationDestination {
    override val route = "Ingredient Details"
    override val titleRes = R.string.app_name
    override val routeId = 20
}

@Composable
fun IngredientDetailScreen(
    viewModel: IngredientsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    backStackEntry: String,
    navController: NavController
) {
    // Call the ViewModel function to fetch ingredients when the screen is first displayed
    viewModel.fetchIngredientsAndImages()

    // Observe the ingredientList StateFlow to display ingredients
    val ingredientsViewState by viewModel.ingredientsViewState.collectAsState()

    val ingredient =
        ingredientsViewState.ingredients.find { i -> i?.id.toString() == backStackEntry }
    val images = ingredientsViewState.images.filter { i -> i?.id.toString() == backStackEntry }

    val imageHelper = ImageHelper(LocalContext.current)

    var bitmap by remember { mutableStateOf(createBitmap(1, 1)) }

    LaunchedEffect(images) {
        if (images.isNotEmpty()) {
            bitmap = File(images[0]?.imagePath).takeIf { it.exists() }
                ?.let { imageHelper.loadImageFromUri(it.toUri()) }!!
        }
    }


    LazyColumn(

    ) {
        item {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(1f) // Maintain aspect ratio
                    .clip(shape = RoundedCornerShape(8.dp))
            )
        }
    }
}