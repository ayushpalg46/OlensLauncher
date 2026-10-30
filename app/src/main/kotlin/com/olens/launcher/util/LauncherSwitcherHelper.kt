package com.olens.launcher.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.widget.Toast

data class OtherLauncherItem(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable?
)

object LauncherSwitcherHelper {

    /**
     * Finds other installed launcher applications on this device (e.g. stock One UI, Pixel Launcher, etc.)
     */
    fun getOtherLaunchers(context: Context): List<OtherLauncherItem> {
        val pm = context.packageManager
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfos = pm.queryIntentActivities(homeIntent, PackageManager.MATCH_ALL)
        return resolveInfos
            .filter { it.activityInfo.packageName != context.packageName }
            .map { info ->
                OtherLauncherItem(
                    packageName = info.activityInfo.packageName,
                    activityName = info.activityInfo.name,
                    label = info.loadLabel(pm).toString(),
                    icon = info.loadIcon(pm)
                )
            }
    }

    /**
     * Opens system Home settings or system chooser to let user switch back to their old launcher
     */
    fun openSystemHomeSettings(context: Context) {
        try {
            context.packageManager.clearPackagePreferredActivities(context.packageName)
        } catch (e: Exception) {}

        // 1. Android 10+ standard Home Settings
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        } catch (e: Exception) {}

        // 2. Default Apps Settings
        try {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        } catch (e: Exception) {}

        // 3. Home chooser fallback
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(homeIntent, "Select Home Launcher").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Please change default home app in Phone Settings", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Directly launches a specific other launcher
     */
    fun launchSpecificLauncher(context: Context, launcher: OtherLauncherItem) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                component = ComponentName(launcher.packageName, launcher.activityName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openSystemHomeSettings(context)
        }
    }
}
