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
        val prevState: UiState? = null,
        val searchPattern: String = "Barcode"
    ) : UiState()
    data class IncomeMenu(
        val isSearchLoopActive: Boolean = true,
        val isBarcodeFieldActive: Boolean = false,
        val isTEModeActive: Boolean = false,
        val isThreeDotsActive: Boolean = false,
        val isBackBtnActive: Boolean = true,
        val prevState: UiState? = null,
    ) : UiState()
    data class IncomeSessionMenu(
        val isSearchLoopActive: Boolean = true,
        val isBarcodeFieldActive: Boolean = false,
        val isBarcodeScanActive: Boolean = true,
        val isTEModeActive: Boolean = false,
        val isTEBtnActive: Boolean = true,
        val isThreeDotsActive: Boolean = true,
        val isBackBtnActive: Boolean = true,
        val prevState: UiState? = null,
    ) : UiState()
    data class MoveMenu(
        val isSearchLoopActive: Boolean = true,
        val isBarcodeFieldActive: Boolean = false,
        val isTEModeActive: Boolean = false,
        val isThreeDotsActive: Boolean = false,
        val isBackBtnActive: Boolean = true,
        val prevState: UiState? = null,
    ) : UiState()
    data class MoveSessionMenu(
        val isSearchLoopActive: Boolean = true,
        val isBarcodeFieldActive: Boolean = false,
        val isBarcodeScanActive: Boolean = true,
        val isMovingModeActive: Boolean = false,
        val isThreeDotsActive: Boolean = false,
        val isBackBtnActive: Boolean = true,
        val isProtectionModeActive: Boolean = false,
        val prevState: UiState? = null,
    ) : UiState()
}