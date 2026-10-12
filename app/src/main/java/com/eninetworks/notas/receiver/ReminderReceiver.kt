package com.eninetworks.notas.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.eninetworks.notas.MainActivity
import com.eninetworks.notas.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getLongExtra("noteId", -1L)
        val title  = intent.getStringExtra("title").orEmpty().ifBlank { "Recordatorio" }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("noteId", noteId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            context, noteId.toInt(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText("Tienes un recordatorio en Itzli")
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        context.getSystemService(NotificationManager::class.java)
            ?.notify(noteId.toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "itzli_reminders"
    }
}
