package com.eninetworks.notas.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.eninetworks.notas.ui.theme.VaultContainerDark
import com.eninetworks.notas.ui.theme.VaultContainerLight
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNotesClick: () -> Unit,
    onListsClick: () -> Unit,
    onPasswordsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onTrashClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, "Buscar")
                    }
                    IconButton(onClick = onTrashClick) {
                        Icon(Icons.Default.Delete, "Papelera")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, "Configuración")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Itzli", fontSize = 36.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Text(
            "Tu mente, en orden.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(52.dp))

        HomeCard(
            title = "Notas",
            subtitle = "Ideas, recordatorios, texto libre",
            icon = Icons.Default.EditNote,
            onClick = onNotesClick,
            bg = MaterialTheme.colorScheme.primaryContainer
        )
        Spacer(Modifier.height(12.dp))
        HomeCard(
            title = "Listas",
            subtitle = "Supermercado, tareas, pendientes",
            icon = Icons.Default.Checklist,
            onClick = onListsClick,
            bg = MaterialTheme.colorScheme.secondaryContainer
        )
        Spacer(Modifier.height(12.dp))
        HomeCard(
            title = "Contraseñas",
            subtitle = "Bóveda cifrada y segura",
            icon = Icons.Default.ShieldMoon,
            onClick = onPasswordsClick,
            bg = if (isSystemInDarkTheme()) VaultContainerDark else VaultContainerLight
        )

        Spacer(Modifier.weight(1f))
        IchSignature()
        Spacer(Modifier.height(24.dp))
    }
    } // Scaffold
}

@Composable
private fun IchSignature() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 48.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "ICH",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun HomeCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit, bg: Color) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Ícono pequeño en círculo translúcido — sutil, no invasivo
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            // Flecha sutil — indica que es navegable
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            )
        }
    }
}
