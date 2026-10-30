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
}
