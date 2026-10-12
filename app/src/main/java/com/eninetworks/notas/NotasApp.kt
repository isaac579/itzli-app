package com.eninetworks.notas

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.eninetworks.notas.data.db.AppDatabase
import com.eninetworks.notas.receiver.ReminderReceiver

class NotasApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ReminderReceiver.CHANNEL_ID,
                "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Recordatorios de notas de Itzli" }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }
}
