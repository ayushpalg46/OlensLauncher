package com.olens.launcher.model

import android.graphics.drawable.Drawable

data class AppItem(
    val packageName: String,
    val activityName: String,
    val label: String,
    val customLabel: String? = null,
    val icon: Drawable? = null,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val unreadNotifications: Int = 0,
    val installTime: Long = 0L
) {
    val displayLabel: String
        get() = customLabel?.takeIf { it.isNotBlank() } ?: label

    val sectionHeader: String
        get() {
            val first = displayLabel.trim().firstOrNull() ?: '#'
            return if (first.isLetter()) {
                first.uppercaseChar().toString()
            } else {
                "#"
            }
        }
}
