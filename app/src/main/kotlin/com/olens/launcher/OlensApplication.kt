package com.olens.launcher

import android.app.Application
import com.olens.launcher.core.NativeEngine
import com.olens.launcher.data.AppRepository

class OlensApplication : Application() {

    lateinit var appRepository: AppRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize Native Rust Engine
        NativeEngine.init()
        
        // Initialize App Repository
        appRepository = AppRepository(this)
    }

    companion object {
        lateinit var instance: OlensApplication
            private set
    }
}

