package com.seyone22.cook.ui.screen.shoppingList.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.ShoppingListItem
import com.seyone22.cook.ui.screen.ingredients.detail.IngredientDetailDestination
import com.seyone22.cook.ui.screen.shoppingList.ShoppingListViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShoppingItemList(
    item: ShoppingListItem,
    ingredients: List<Ingredient?>,
    measures: List<Measure?>,
    navController: NavController,
    viewModel: ShoppingListViewModel,
) {
    var checked by remember { mutableStateOf(item.checked) }

    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditShoppingListItemDialog(onConfirm = {
            viewModel.addToShoppingList(it.copy(shoppingListId = item.id))
            viewModel.fetchData()
            showEditDialog = false
        },
            onDismiss = { showEditDialog = false },
            ingredients = ingredients,
            measures = measures,
            createNewIngredient = { uri -> navController.navigate(uri) },
            item = item,
            onDelete = {
                viewModel.removeFromShoppingList(it)
                viewModel.fetchData()
                showEditDialog = false
            })
    }

    ListItem(modifier = Modifier
        .padding(0.dp)
        .combinedClickable(enabled = true, onLongClick = {
            showEditDialog = true
        }, onClick = {
            navController.navigate("${IngredientDetailDestination.route}/${item.ingredientId}")
        }), leadingContent = {
        Checkbox(modifier = Modifier.height(32.dp),
            enabled = true,
            checked = checked,
            onCheckedChange = {
                checked = !checked
                viewModel.changePurchaseStatus(item)
            })
    }, headlineContent = {
        Text(
            modifier = Modifier
                .padding(4.dp, 0.dp, 16.dp, 0.dp)
                .width(120.dp),
            text = ingredients.find { i -> i?.id == item.ingredientId }?.name ?: "",
            style = MaterialTheme.typography.bodyLarge.copy(
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (checked) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }, trailingContent = {
        Text(
            modifier = Modifier.padding(4.dp, 0.dp, 16.dp, 0.dp),
            text = "${
                String.format("%.2f", item.quantity)
            } ${measures.find { m -> m?.id == item.measureId }?.abbreviation}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    })
}