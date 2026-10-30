package com.olens.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.olens.launcher.ui.HomeScreen
import com.olens.launcher.ui.theme.OlensTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Modern Android edge-to-edge transparent bars
        enableEdgeToEdge()

        val repository = (application as OlensApplication).appRepository

        setContent {
            val pureAmoled by repository.preferences.pureAmoledMode.collectAsState(initial = true)
            val accentColor by repository.preferences.accentColor.collectAsState(initial = "Niagara Cyan")
            val fontFamily by repository.preferences.fontFamily.collectAsState(initial = "Default")

            OlensTheme(
                pureAmoled = pureAmoled,
                accentColorName = accentColor,
                fontFamilyName = fontFamily
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    HomeScreen(repository = repository)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh apps list when returning home
        (application as OlensApplication).appRepository.reloadApps()
    }
}

