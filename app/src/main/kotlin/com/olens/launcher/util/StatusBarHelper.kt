package com.olens.launcher.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log

object StatusBarHelper {
    private const val TAG = "StatusBarHelper"

    /**
     * Expands the Android status bar notification panel via reflection.
     * Equivalent to user pulling down from top of the screen.
     */
    @SuppressLint("WrongConstant")
    fun expandNotificationShade(context: Context) {
        try {
            val statusBarService = context.getSystemService("statusbar") ?: return
            val statusBarManagerClass = Class.forName("android.app.StatusBarManager")
            val expandMethod = try {
                statusBarManagerClass.getMethod("expandNotificationsPanel")
            } catch (e: NoSuchMethodException) {
                statusBarManagerClass.getMethod("expand")
            }
            expandMethod.invoke(statusBarService)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to expand notification shade", e)
        }
    }
}
