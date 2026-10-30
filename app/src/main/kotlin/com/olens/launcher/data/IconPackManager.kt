package com.olens.launcher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

data class IconPackInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable?
)

object IconPackManager {
    private const val TAG = "IconPackManager"

    // Recognized icon pack intent filters
    private val ICON_PACK_INTENTS = listOf(
        "org.adw.launcher.THEMES",
        "com.novalauncher.THEME",
        "com.gau.go.launcherex.theme",
        "com.fede.launcher.THEME_ICONPACK",
        "com.anddoes.launcher.THEME",
        "com.teslacoilsw.launcher.THEME"
    )

    // In-memory cache of parsed icon pack components: "ComponentInfo{pkg/act}" -> "drawable_name"
    private var cachedPackPackage: String? = null
    private var componentToDrawableMap: Map<String, String> = emptyMap()
    private var packageToDrawableMap: Map<String, String> = emptyMap()

    /**
     * Find all installed icon packs on the device.
     */
    fun getInstalledIconPacks(context: Context): List<IconPackInfo> {
        val pm = context.packageManager
        val seen = mutableSetOf<String>()
        val result = mutableListOf<IconPackInfo>()

        for (action in ICON_PACK_INTENTS) {
            val intent = Intent(action)
            val resolveInfos = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)
            for (info in resolveInfos) {
                val pkg = info.activityInfo.packageName
                if (seen.add(pkg)) {
                    val label = info.loadLabel(pm).toString()
                    val icon = info.loadIcon(pm)
                    result.add(IconPackInfo(packageName = pkg, label = label, icon = icon))
                }
            }
        }
        return result
    }

    /**
     * Load and parse appfilter.xml from the selected icon pack.
     */
    @Synchronized
    fun loadIconPack(context: Context, iconPackPackage: String) {
        if (iconPackPackage.isBlank()) {
            cachedPackPackage = null
            componentToDrawableMap = emptyMap()
            packageToDrawableMap = emptyMap()
            return
        }

        if (cachedPackPackage == iconPackPackage && componentToDrawableMap.isNotEmpty()) {
            return // Already cached
        }

        val compMap = mutableMapOf<String, String>()
        val pkgMap = mutableMapOf<String, String>()

        try {
            val packRes = context.packageManager.getResourcesForApplication(iconPackPackage)
            val parser = getAppFilterParser(packRes, iconPackPackage)

            if (parser != null) {
                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                        val component = parser.getAttributeValue(null, "component")
                        val drawable = parser.getAttributeValue(null, "drawable")
                        if (!component.isNullOrBlank() && !drawable.isNullOrBlank()) {
                            compMap[component] = drawable
                            // Also index by package name
                            extractPackageFromComponent(component)?.let { pkg ->
                                if (!pkgMap.containsKey(pkg)) {
                                    pkgMap[pkg] = drawable
                                }
                            }
                        }
                    }
                    eventType = parser.next()
                }
            }
            cachedPackPackage = iconPackPackage
            componentToDrawableMap = compMap
            packageToDrawableMap = pkgMap
            Log.d(TAG, "Loaded icon pack $iconPackPackage with ${compMap.size} custom icons")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load icon pack $iconPackPackage", e)
            cachedPackPackage = null
            componentToDrawableMap = emptyMap()
            packageToDrawableMap = emptyMap()
        }
    }

    /**
     * Resolve a custom icon from the active icon pack for an app.
     */
    fun getCustomIcon(
        context: Context,
        iconPackPackage: String,
        packageName: String,
        activityName: String
    ): Drawable? {
        if (iconPackPackage.isBlank()) return null

        loadIconPack(context, iconPackPackage)
        if (componentToDrawableMap.isEmpty() && packageToDrawableMap.isEmpty()) return null

        val exactKey = "ComponentInfo{$packageName/$activityName}"
        val drawableName = componentToDrawableMap[exactKey] ?: packageToDrawableMap[packageName] ?: return null

        return try {
            val packRes = context.packageManager.getResourcesForApplication(iconPackPackage)
            val resId = packRes.getIdentifier(drawableName, "drawable", iconPackPackage)
            if (resId != 0) {
                packRes.getDrawable(resId, null)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getAppFilterParser(resources: Resources, packageName: String): XmlPullParser? {
        // 1. Try reading from res/xml/appfilter.xml
        val xmlId = resources.getIdentifier("appfilter", "xml", packageName)
        if (xmlId != 0) {
            return resources.getXml(xmlId)
        }

        // 2. Try reading from assets/appfilter.xml
        return try {
            val assetStream: InputStream = resources.assets.open("appfilter.xml")
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(assetStream, "utf-8")
            parser
        } catch (e: Exception) {
            null
        }
    }

    private fun extractPackageFromComponent(componentStr: String): String? {
        // Format: ComponentInfo{com.example.app/com.example.app.MainActivity}
        val start = componentStr.indexOf('{')
        val slash = componentStr.indexOf('/')
        return if (start != -1 && slash != -1 && slash > start) {
            componentStr.substring(start + 1, slash).trim()
        } else {
            null
        }
    }
}
