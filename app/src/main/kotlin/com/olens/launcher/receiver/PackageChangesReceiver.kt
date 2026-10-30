package com.olens.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.olens.launcher.OlensApplication

class PackageChangesReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val packageName = intent.data?.schemeSpecificPart
        Log.d("OlensReceiver", "Package event: $action for $packageName")
        
        try {
            OlensApplication.instance.appRepository.reloadApps()
        } catch (e: Exception) {
            Log.e("OlensReceiver", "Failed to reload apps after package change", e)
        }
    }
}
