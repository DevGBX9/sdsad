package com.ooredoost.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.ooredoost.app.data.SessionDatabase

class OoreDoostApp : Application() {

    val database: SessionDatabase by lazy {
        SessionDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val serviceChannel = NotificationChannel(
            CHANNEL_SERVICE,
            "OoreDoost Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when OoreDoost is actively cycling airplane mode"
            setShowBadge(false)
        }

        val alertChannel = NotificationChannel(
            CHANNEL_ALERTS,
            "OoreDoost Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Important alerts about service status"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(serviceChannel)
        notificationManager.createNotificationChannel(alertChannel)
    }

    companion object {
        const val CHANNEL_SERVICE = "ooredoost_service"
        const val CHANNEL_ALERTS = "ooredoost_alerts"
    }
}
