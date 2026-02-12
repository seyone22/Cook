package com.seyone22.cook.ui.screen.shoppingList.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seyone22.cook.data.model.ShoppingList
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ShoppingListCard(
    item: ShoppingList, onClick: (Long) -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 4.dp)
            .clickable { onClick(item.id) },
        headlineContent = { Text(text = item.name) },
        supportingContent = {
            Text(
                text = LocalDate.parse(
                    item.dateCreated, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
                ).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            )
        },
        leadingContent = {
            if (item.completed) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
        },
    )
}