package com.eninetworks.notas.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.eninetworks.notas.receiver.ReminderReceiver

object ReminderManager {

    fun schedule(context: Context, noteId: Long, title: String, triggerAt: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = buildPendingIntent(context, noteId, title)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancel(context: Context, noteId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context, noteId.toInt(),
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pi)
    }

    private fun buildPendingIntent(context: Context, noteId: Long, title: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("noteId", noteId)
            putExtra("title", title)
        }
        return PendingIntent.getBroadcast(
            context, noteId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
