package com.olens.launcher.util

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import android.util.Log

data class AppShortcutItem(
    val id: String,
    val packageName: String,
    val shortLabel: String,
    val longLabel: String?,
    val icon: Drawable?
)

object AppShortcutsHelper {
    private const val TAG = "AppShortcutsHelper"

    /**
     * Query static and dynamic deep shortcuts for a specific app package.
     * Mirrors Niagara Launcher's pop-up shortcut list.
     */
    fun getShortcutsForApp(context: Context, packageName: String): List<AppShortcutItem> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            return emptyList()
        }

        return try {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
                ?: return emptyList()

            // Verify if launcher has permission to query shortcuts
            if (!launcherApps.hasShortcutHostPermission()) {
                return emptyList()
            }

            val query = LauncherApps.ShortcutQuery().apply {
                setPackage(packageName)
                setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                )
            }

            val shortcuts = launcherApps.getShortcuts(query, Process.myUserHandle()) ?: return emptyList()
            val density = context.resources.displayMetrics.densityDpi

            // Limit to top 4 most relevant shortcuts
            shortcuts.take(4).map { info ->
                val iconDrawable = try {
                    launcherApps.getShortcutIconDrawable(info, density)
                } catch (e: Exception) {
                    null
                }

                val label = info.shortLabel?.toString()
                    ?: info.longLabel?.toString()
                    ?: "Shortcut"

                AppShortcutItem(
                    id = info.id,
                    packageName = info.`package`,
                    shortLabel = label,
                    longLabel = info.longLabel?.toString(),
                    icon = iconDrawable
                )
            }
        } catch (e: Exception) {
            Log.d(TAG, "Cannot load shortcuts for $packageName: ${e.message}")
            emptyList()
        }
    }

    /**
     * Launch a specific app shortcut directly into the target activity flow.
     */
    fun launchShortcut(context: Context, packageName: String, shortcutId: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return

        try {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return
            launcherApps.startShortcut(packageName, shortcutId, null, null, Process.myUserHandle())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch shortcut $shortcutId for $packageName", e)
        }
    }
}
