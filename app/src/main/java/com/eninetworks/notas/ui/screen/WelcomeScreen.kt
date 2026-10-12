package com.eninetworks.notas.ui.screen

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(onAccept: () -> Unit) {
    val ctx = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(56.dp))

        Text("Itzli", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text(
            "Tu mente, en orden.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )

        Spacer(Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                TermItem(
                    title = "Completamente gratuita",
                    body = "Itzli no requiere pago, suscripción ni cuenta. Sin anuncios. Sin costos ocultos."
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                TermItem(
                    title = "Sin conexión a internet",
                    body = "La app no usa datos móviles ni WiFi. Nada sale de tu dispositivo hacia servidores externos."
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                TermItem(
                    title = "Privacidad total",
                    body = "Tus notas, listas y contraseñas se almacenan únicamente en tu teléfono. No hay sincronización en la nube ni rastreo de ningún tipo."
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                TermItem(
                    title = "Tu PIN es tu llave",
                    body = "Si olvidas tu PIN y no tienes biometría configurada, no hay forma de recuperar el acceso. Guárdalo en un lugar seguro."
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                TermItem(
                    title = "Uso libre y personal",
                    body = "Puedes usar, compartir y adaptar esta aplicación libremente para uso personal, sin restricciones."
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    .edit().putBoolean("terms_accepted", true).apply()
                onAccept()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Entendido, usar Itzli", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Desarrollado por ICH · Isaac Calva Hernandez",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TermItem(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(
            body,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 19.sp
        )
    }
}
