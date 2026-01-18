package com.example.wmswherther.Classes

sealed class UiState {
    object MainMenu : UiState()
    object SearchMenu : UiState()
    object IncomeMenu : UiState()
    object IncomeSessionMenu : UiState()
    object MoveMenu : UiState()
    object MoveSessionMenu : UiState()
}