package com.seyone22.cook.ui.screen.cooking

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CookingViewModel(

) : ViewModel() {
    private val _cookingViewState = MutableStateFlow(CookingViewState())
    val cookingViewState: StateFlow<CookingViewState> get() = _cookingViewState

    fun fetchData() {

    }
}

data class CookingViewState(
    val temp: String = ""
)