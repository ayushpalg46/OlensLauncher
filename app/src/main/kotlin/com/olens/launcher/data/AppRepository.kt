package com.olens.launcher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Process
import android.provider.Settings
import com.olens.launcher.core.NativeEngine
import com.olens.launcher.model.AppItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    val preferences = UserPreferences(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

    private val _favoriteApps = MutableStateFlow<List<AppItem>>(emptyList())
    val favoriteApps: StateFlow<List<AppItem>> = _favoriteApps.asStateFlow()

    private val _hiddenApps = MutableStateFlow<List<AppItem>>(emptyList())
    val hiddenApps: StateFlow<List<AppItem>> = _hiddenApps.asStateFlow()

    init {
        reloadApps()
    }

    fun reloadApps() {
        scope.launch {
            loadAppsFromSystem()
        }
    }

    private suspend fun loadAppsFromSystem() = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
        val userHandle = Process.myUserHandle()

        val favoritesSet = preferences.favoritePackages.first()
        val hiddenSet = preferences.hiddenPackages.first()
        val customLabelsMap = preferences.getAllCustomLabels()
        val selectedIconPack = preferences.iconPackPackage.first()

        val appList = mutableListOf<AppItem>()

        if (launcherApps != null) {
            val activities = launcherApps.getActivityList(null, userHandle)
            for (activity in activities) {
                val pkg = activity.applicationInfo.packageName
                if (pkg == context.packageName) continue // Skip Olens itself

                val label = activity.label?.toString() ?: activity.applicationInfo.loadLabel(pm).toString()
                val customIcon = if (selectedIconPack.isNotBlank()) {
                    IconPackManager.getCustomIcon(context, selectedIconPack, pkg, activity.name)
                } else null
                val icon = customIcon ?: activity.getIcon(0)

                val item = AppItem(
                    packageName = pkg,
                    activityName = activity.name,
                    label = label,
                    customLabel = customLabelsMap[pkg],
                    icon = icon,
                    isFavorite = favoritesSet.contains(pkg),
                    isHidden = hiddenSet.contains(pkg)
                )
                appList.add(item)
            }
        }

        // Sort alphabetically by label
        appList.sortBy { it.displayLabel.lowercase() }

        val visibleApps = appList.filter { !it.isHidden }
        val favorites = appList.filter { it.isFavorite && !it.isHidden }
        val hidden = appList.filter { it.isHidden }

        _installedApps.value = visibleApps
        _favoriteApps.value = favorites
        _hiddenApps.value = hidden
    }

    /**
     * Update active notification previews on app icons in real-time
     */
    fun updateNotification(packageName: String, summary: String?) {
        _installedApps.value = _installedApps.value.map { app ->
            if (app.packageName == packageName) {
                app.copy(
                    notificationSummary = summary,
                    unreadNotifications = if (summary != null) 1 else 0
                )
            } else app
        }
        _favoriteApps.value = _favoriteApps.value.map { app ->
            if (app.packageName == packageName) {
                app.copy(
                    notificationSummary = summary,
                    unreadNotifications = if (summary != null) 1 else 0
                )
            } else app
        }
    }

    /**
     * Search apps using the high-performance Rust fuzzy engine
     */
    fun searchApps(query: String): List<AppItem> {
        val current = _installedApps.value
        if (query.isBlank()) return current

        val labels = current.map { it.displayLabel }.toTypedArray()
        val packages = current.map { it.packageName }.toTypedArray()

        val rankedIndices = NativeEngine.fuzzySearch(query, labels, packages)
        val result = ArrayList<AppItem>(rankedIndices.size)
        for (idx in rankedIndices) {
            val app = current.getOrNull(idx)
            if (app != null) {
                result.add(app)
            }
        }
        return result
    }

    fun launchApp(app: AppItem) {
        try {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            val component = ComponentName(app.packageName, app.activityName)
            launcherApps?.startMainActivity(component, Process.myUserHandle(), null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openAppInfo(app: AppItem) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", app.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun uninstallApp(app: AppItem) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", app.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun toggleFavorite(app: AppItem) {
        val newStatus = !app.isFavorite
        preferences.setFavorite(app.packageName, newStatus)
        reloadApps()
    }

    suspend fun toggleHide(app: AppItem) {
        val newStatus = !app.isHidden
        preferences.setHidden(app.packageName, newStatus)
        reloadApps()
    }

    suspend fun renameApp(app: AppItem, newName: String?) {
        preferences.setCustomLabel(app.packageName, newName)
        reloadApps()
    }
}

