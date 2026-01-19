package com.example.wmswherther.Classes

sealed class UiState {
    data class MainMenu(
        val isSearchLoopActive: Boolean = true,
        val isBarcodeFieldActive: Boolean = false,
        val isTEModeActive: Boolean = false,
        val isThreeDotsActive: Boolean = false,
        val isBackBtnActive: Boolean = false
    ) : UiState()
    data class SearchMenu(
        val isSearchLoopActive: Boolean = false,
        val isBarcodeFieldActive: Boolean = true,
        val isTEModeActive: Boolean = false,
        val isThreeDotsActive: Boolean = true,
        val isBackBtnActive: Boolean = true,
        val prevState: UiState? = null
    ) : UiState()
    object IncomeMenu : UiState()
    object IncomeSessionMenu : UiState()
    object MoveMenu : UiState()
    object MoveSessionMenu : UiState()
}