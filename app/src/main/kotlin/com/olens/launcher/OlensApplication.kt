package com.olens.launcher

import android.app.Application
import com.olens.launcher.data.AppRepository

class OlensApplication : Application() {

    lateinit var appRepository: AppRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize App Repository
        appRepository = AppRepository(this)
    }

    companion object {
        lateinit var instance: OlensApplication
            private set
    }
}

