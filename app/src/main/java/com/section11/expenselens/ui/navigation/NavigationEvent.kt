package com.section11.expenselens.ui.navigation

sealed class NavigationEvent {
    data object AddExpenseTapped : NavigationEvent()
    data class TextExtractedFromImage(val extractedText: String) : NavigationEvent()
}
