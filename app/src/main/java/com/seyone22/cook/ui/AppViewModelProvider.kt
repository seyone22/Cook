package com.seyone22.cook.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.seyone22.cook.CookApplication
import com.seyone22.cook.ui.screen.home.HomeViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(
            )
        }
    }
}

fun CreationExtras.expenseApplication(): CookApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as CookApplication)