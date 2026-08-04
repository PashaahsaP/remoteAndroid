package com.example.wmswherther

import android.app.Application
import android.provider.Settings
import com.example.wmswherther.Classes.RemoteLoggingTree
import com.google.android.datatransport.BuildConfig
import timber.log.Timber

public class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val serverLogUrl = "http://192.168.0.11:3000/api/logs/tsd"

        // Инициализируем один раз на весь жизненный цикл приложения
        Timber.plant(RemoteLoggingTree(serverLogUrl, deviceId))

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}