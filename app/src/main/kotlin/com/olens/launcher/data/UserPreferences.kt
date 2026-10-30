package com.olens.launcher.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
        val ICON_SIZE_KEY = intPreferencesKey("icon_size_dp")
        val HIDE_NAMES_KEY = booleanPreferencesKey("hide_app_names")
        val SHOW_WEATHER_KEY = booleanPreferencesKey("show_weather_glance")
        val FONT_FAMILY_KEY = stringPreferencesKey("font_family")
        val ACCENT_COLOR_KEY = stringPreferencesKey("accent_color")
        val ICON_PACK_KEY = stringPreferencesKey("icon_pack_package")
        val ICON_SHAPE_KEY = stringPreferencesKey("icon_shape")
        val WALLPAPER_DIM_KEY = intPreferencesKey("wallpaper_dim_percent")
        val CLOCK_24H_KEY = booleanPreferencesKey("clock_24h_format")
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

    val iconSize: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[ICON_SIZE_KEY] ?: 40
    }

    val hideAppNames: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HIDE_NAMES_KEY] ?: false
    }

    val showWeather: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SHOW_WEATHER_KEY] ?: true
    }

    val fontFamily: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[FONT_FAMILY_KEY] ?: "Default"
    }

    val accentColor: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[ACCENT_COLOR_KEY] ?: "Niagara Cyan"
    }

    val iconPackPackage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[ICON_PACK_KEY] ?: ""
    }

    val iconShape: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[ICON_SHAPE_KEY] ?: "Circle"
    }

    val wallpaperDimPercent: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[WALLPAPER_DIM_KEY] ?: 25
    }

    val clock24hFormat: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[CLOCK_24H_KEY] ?: true
    }

    suspend fun setFontFamily(font: String) {
        context.dataStore.edit { prefs -> prefs[FONT_FAMILY_KEY] = font }
    }

    suspend fun setAccentColor(colorName: String) {
        context.dataStore.edit { prefs -> prefs[ACCENT_COLOR_KEY] = colorName }
    }

    suspend fun setIconPackPackage(pkg: String) {
        context.dataStore.edit { prefs -> prefs[ICON_PACK_KEY] = pkg }
    }

    suspend fun setIconShape(shape: String) {
        context.dataStore.edit { prefs -> prefs[ICON_SHAPE_KEY] = shape }
    }

    suspend fun setWallpaperDimPercent(percent: Int) {
        context.dataStore.edit { prefs -> prefs[WALLPAPER_DIM_KEY] = percent }
    }

    suspend fun setClock24hFormat(is24h: Boolean) {
        context.dataStore.edit { prefs -> prefs[CLOCK_24H_KEY] = is24h }
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

    suspend fun setIconSize(sizeDp: Int) {
        context.dataStore.edit { prefs ->
            prefs[ICON_SIZE_KEY] = sizeDp
        }
    }

    suspend fun setHideAppNames(hide: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HIDE_NAMES_KEY] = hide
        }
    }

    suspend fun setShowWeather(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SHOW_WEATHER_KEY] = show
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
}

