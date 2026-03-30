package com.example.motivationalmornings.Presentation

fun String.truncateForChipLabel(maxLength: Int = 20): String {
    if (length <= maxLength) return this
    return take(maxLength) + "..."
}
