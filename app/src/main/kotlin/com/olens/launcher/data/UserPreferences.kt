package com.olens.launcher.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "olens_settings")

class UserPreferences(private val context: Context) {

    companion object {
        val FAVORITES_KEY = stringSetPreferencesKey("favorite_packages")
        val HIDDEN_KEY = stringSetPreferencesKey("hidden_packages")
        val HAPTICS_KEY = booleanPreferencesKey("enable_haptics")
        val SCRUBBER_SIDE_KEY = booleanPreferencesKey("scrubber_on_right")
        val MAX_FAVORITES_KEY = intPreferencesKey("max_favorites")
        val PURE_AMOLED_KEY = booleanPreferencesKey("pure_amoled_mode")
    }

    val favoritePackages: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[FAVORITES_KEY] ?: setOf(
            "com.google.android.youtube",
            "com.android.chrome",
            "com.google.android.apps.messaging",
            "com.google.android.dialer",
            "com.google.android.apps.photos"
        )
    }

    val hiddenPackages: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[HIDDEN_KEY] ?: emptySet()
    }

    val enableHaptics: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HAPTICS_KEY] ?: true
    }

    val scrubberOnRight: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SCRUBBER_SIDE_KEY] ?: true
    }

    val maxFavorites: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[MAX_FAVORITES_KEY] ?: 8
    }

    val pureAmoledMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PURE_AMOLED_KEY] ?: true
    }

    suspend fun setFavorite(packageName: String, isFavorite: Boolean) {
        context.dataStore.edit { prefs ->
            val current = (prefs[FAVORITES_KEY] ?: emptySet()).toMutableSet()
            if (isFavorite) {
                current.add(packageName)
            } else {
                current.remove(packageName)
            }
            prefs[FAVORITES_KEY] = current
        }
    }

    suspend fun setHidden(packageName: String, isHidden: Boolean) {
        context.dataStore.edit { prefs ->
            val current = (prefs[HIDDEN_KEY] ?: emptySet()).toMutableSet()
            if (isHidden) {
                current.add(packageName)
            } else {
                current.remove(packageName)
            }
            prefs[HIDDEN_KEY] = current
        }
    }

    suspend fun setEnableHaptics(enable: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HAPTICS_KEY] = enable
        }
    }

    suspend fun setScrubberOnRight(right: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SCRUBBER_SIDE_KEY] = right
        }
    }

    suspend fun setPureAmoledMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PURE_AMOLED_KEY] = enabled
        }
    }

    suspend fun setCustomLabel(packageName: String, label: String?) {
        context.dataStore.edit { prefs ->
            val key = androidx.datastore.preferences.core.stringPreferencesKey("custom_label_$packageName")
            if (label.isNullOrBlank()) {
                prefs.remove(key)
            } else {
                prefs[key] = label
            }
        }
    }

    suspend fun getAllCustomLabels(): Map<String, String> {
        val prefs = context.dataStore.data.first()
        val result = mutableMapOf<String, String>()
        for ((key, value) in prefs.asMap()) {
            if (key.name.startsWith("custom_label_") && value is String) {
                result[key.name.removePrefix("custom_label_")] = value
            }
        }
        return result
    }

    fun getCustomLabelFlow(packageName: String): Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[androidx.datastore.preferences.core.stringPreferencesKey("custom_label_$packageName")]
    }
}

