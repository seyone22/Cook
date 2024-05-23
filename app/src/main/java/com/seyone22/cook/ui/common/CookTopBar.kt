package com.seyone22.cook.ui.common

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.protobuf.StringValue
import com.seyone22.cook.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookTopBar(
) {
    CenterAlignedTopAppBar(title = { Text(stringResource(id = R.string.app_name) ) })
}