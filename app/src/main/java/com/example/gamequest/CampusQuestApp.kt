package com.example.gamequest

import android.app.Application
import com.example.gamequest.di.AppContainer

class CampusQuestApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
