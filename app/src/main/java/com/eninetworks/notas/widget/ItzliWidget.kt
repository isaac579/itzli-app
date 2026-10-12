package com.eninetworks.notas.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.eninetworks.notas.MainActivity
import com.eninetworks.notas.NotasApp
import com.eninetworks.notas.data.entity.NoteType
import com.eninetworks.notas.data.entity.noteType
import kotlinx.coroutines.flow.first

class ItzliWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = (context.applicationContext as NotasApp).database
        val notes = db.noteDao().getAll().first().take(3)

        provideContent {
            val primary   = ColorProvider(Color(0xFF5C4E9C))
            val onSurface = ColorProvider(Color(0xFF1C1B1F))
            val subtle    = ColorProvider(Color(0xFF49454F))
            val surface   = ColorProvider(Color(0xFFFFFBFE))

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(surface)
                    .padding(12.dp)
            ) {
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    Text(
                        "Itzli",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = primary),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Text(
                        "+ Nueva nota",
                        style = TextStyle(fontSize = 12.sp, color = primary),
                        modifier = GlanceModifier.clickable(
                            actionStartActivity(
                                Intent(context, MainActivity::class.java).apply {
                                    action = "com.eninetworks.notas.NEW_NOTE"
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                            )
                        )
                    )
                }
                Spacer(GlanceModifier.height(8.dp))
                if (notes.isEmpty()) {
                    Text(
                        "Sin notas aún",
                        style = TextStyle(fontSize = 13.sp, color = subtle)
                    )
                } else {
                    notes.forEach { note ->
                        val preview = when (note.noteType()) {
                            NoteType.SECURE -> "Contenido protegido"
                            else -> note.title.ifBlank { note.content.take(40) }
                        }
                        Text(
                            "• $preview",
                            style = TextStyle(fontSize = 13.sp, color = onSurface),
                            maxLines = 1,
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable(
                                    actionStartActivity(
                                        Intent(context, MainActivity::class.java).apply {
                                            action = "com.eninetworks.notas.OPEN_NOTE"
                                            putExtra("noteId", note.id)
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        }
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}
