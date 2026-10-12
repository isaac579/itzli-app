package com.eninetworks.notas.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.eninetworks.notas.NotasApp
import com.eninetworks.notas.util.ReminderManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = (context.applicationContext as NotasApp).database
                val now = System.currentTimeMillis()
                db.noteDao().getNotesWithReminder(now).forEach { note ->
                    ReminderManager.schedule(context, note.id, note.title, note.reminderAt!!)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
