package com.seyone22.cook.ui.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CookToolbar(
    onMadeItClicked: () -> Unit,
    onScaleRecipeClicked: () -> Unit,
    onCookingModeClicked: () -> Unit,
    onAddToShoppingListClicked: () -> Unit,
    scrollBehavior: FloatingToolbarScrollBehavior,
    recipeLink: String?,
    context: Context
) {
    var expanded by rememberSaveable { mutableStateOf(true) }

    HorizontalFloatingToolbar(
        scrollBehavior = scrollBehavior,
        expanded = expanded,
        modifier = Modifier.offset(-40.dp, 0.dp),
    ) {
        Row {
            IconButton(
                onClick = onMadeItClicked,
                content = { Icon(Icons.Default.ThumbUp, contentDescription = "I made it") },
            )
            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = onScaleRecipeClicked,
                content = { Icon(Icons.Default.Scale, contentDescription = "Scale recipe") },
            )
            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = onCookingModeClicked,
                content = { Icon(Icons.Default.Fullscreen, contentDescription = "Cooking mode") },
            )
            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = onAddToShoppingListClicked,
                content = {
                    Icon(
                        Icons.Default.AddShoppingCart,
                        contentDescription = "Add all to new shopping list"
                    )
                },
            )
            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = {
                    val urlIntent = Intent(
                        Intent.ACTION_VIEW, Uri.parse(recipeLink)
                    )
                    context.startActivity(urlIntent)
                },
                content = { Icon(Icons.Default.Link, contentDescription = "Reference Link") },
                )
        }
    }
}