package com.olens.launcher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Process
import android.os.UserHandle
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

    private val userPreferences = UserPreferences(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

    private val _favoriteApps = MutableStateFlow<List<AppItem>>(emptyList())
    val favoriteApps: StateFlow<List<AppItem>> = _favoriteApps.asStateFlow()

    private val _hiddenApps = MutableStateFlow<List<AppItem>>(emptyList())
    val hiddenApps: StateFlow<List<AppItem>> = _hiddenApps.asStateFlow()

    val preferences: UserPreferences
        get() = userPreferences

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

        val favoritesSet = userPreferences.favoritePackages.first()
        val hiddenSet = userPreferences.hiddenPackages.first()

        val appList = mutableListOf<AppItem>()

        if (launcherApps != null) {
            val activities = launcherApps.getActivityList(null, userHandle)
            for (activity in activities) {
                val pkg = activity.applicationInfo.packageName
                if (pkg == context.packageName) continue // Skip Olens itself

                val label = activity.label?.toString() ?: activity.applicationInfo.loadLabel(pm).toString()
                val icon = activity.getIcon(0)

                val item = AppItem(
                    packageName = pkg,
                    activityName = activity.name,
                    label = label,
                    icon = icon,
                    isFavorite = favoritesSet.contains(pkg),
                    isHidden = hiddenSet.contains(pkg)
                )
                appList.add(item)
            }
        } else {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            for (info in resolveInfos) {
                val pkg = info.activityInfo.packageName
                if (pkg == context.packageName) continue

                val label = info.loadLabel(pm).toString()
                val icon = info.loadIcon(pm)

                val item = AppItem(
                    packageName = pkg,
                    activityName = info.activityInfo.name,
                    label = label,
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
     * Search apps using the high-performance Rust fuzzy engine
     */
    fun searchApps(query: String): List<AppItem> {
        val current = _installedApps.value
        if (query.isBlank()) return current

        val labels = current.map { it.displayLabel }.toTypedArray()
        val packages = current.map { it.packageName }.toTypedArray()

        val rankedIndices = NativeEngine.fuzzySearch(query, labels, packages)
        return rankedIndices.mapNotNull { current.getOrNull(it) }
    }

    fun launchApp(app: AppItem) {
        try {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            if (launcherApps != null) {
                val component = ComponentName(app.packageName, app.activityName)
                launcherApps.startMainActivity(component, Process.myUserHandle(), null, null)
            } else {
                val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                }
                if (intent != null) {
                    context.startActivity(intent)
                }
            }
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
        userPreferences.setFavorite(app.packageName, newStatus)
        reloadApps()
    }

    suspend fun toggleHide(app: AppItem) {
        val newStatus = !app.isHidden
        userPreferences.setHidden(app.packageName, newStatus)
        reloadApps()
    }
}

