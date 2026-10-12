package com.eninetworks.notas.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onChangePinClick: () -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Spacer(Modifier.height(8.dp))

            Text(
                "Seguridad",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Card(
                onClick = onChangePinClick,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                ListItem(
                    leadingContent = {
                        Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary)
                    },
                    headlineContent = { Text("Cambiar PIN") },
                    supportingContent = { Text("Actualiza tu PIN de acceso a la app") },
                    trailingContent = {
                        Icon(
                            Icons.Default.ChevronRight,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                )
            }
        }
    }
}
