package com.seyone22.cook.ui.screen.home.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.data.model.IngredientVariant
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.helper.ImageHelper
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.navigation.NavigationDestination
import com.seyone22.cook.ui.screen.home.HomeViewModel
import java.io.File

object RecipeDetailDestination : NavigationDestination {
    override val route = "Recipe Details"
    override val titleRes = R.string.app_name
    override val routeId = 21
}

@Composable
fun RecipeDetailScreen(
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    backStackEntry: String,
    navController: NavController
) {
    // Call the ViewModel function to fetch ingredients when the screen is first displayed
    viewModel.fetchData()

    // Observe the ingredientList StateFlow to display ingredients
    val homeViewState by viewModel.homeViewState.collectAsState()

    val recipe =
        homeViewState.recipes.find { r -> r?.id.toString() == backStackEntry }
    val images = homeViewState.images.filter { i -> i?.id.toString() == backStackEntry }
    val instructions = homeViewState.instructions.filter { i -> i?.recipeId.toString() == backStackEntry }

    val imageHelper = ImageHelper(LocalContext.current)

    var bitmap by remember { mutableStateOf(createBitmap(1, 1)) }

    LaunchedEffect(images) {
        if (images.isNotEmpty()) {
            bitmap = File(images[0]?.imagePath).takeIf { it.exists() }
                ?.let { imageHelper.loadImageFromUri(it.toUri()) }!!
        }
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (recipe != null) {
            item {
                HeaderImage(bitmap = bitmap.asImageBitmap())

                Text(text = recipe.name, style = MaterialTheme.typography.headlineLarge)
                Text(text = recipe.description ?: "")
            }
        }
        if (instructions.isNotEmpty()) {
            item {
                InstructionList(list = instructions)
            }
        }
    }
}

@Composable
fun HeaderImage(bitmap: ImageBitmap) {
    Image(
        bitmap = bitmap,
        contentDescription = null,
        modifier = Modifier
            .clip(shape = RoundedCornerShape(64.dp))
            .fillMaxWidth()
            .fillMaxHeight()
    )
}

@Composable
fun InstructionList(list: List<Instruction?>) {
    list.forEach {
        InstructionCard(instruction = it!!)
    }
}

@Composable
fun InstructionCard(instruction: Instruction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column {
            Text(text = instruction.description ?: "")
            Text(text = instruction.stepNumber.toString() ?: "")
        }
    }
}