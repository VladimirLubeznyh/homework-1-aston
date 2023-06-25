package ru.lyubeznyh.musicplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        createPlayerNotificationChanel()
    }

    private fun createPlayerNotificationChanel() {
        val name = "Player view"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(PLAYER_CHANEL, name, importance)

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    companion object {
        const val PLAYER_CHANEL = "PlayerNotificationChanel"
    }
}
