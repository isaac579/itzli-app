<h1 align="center">
  <br>
  🖤 Itzli
  <br>
</h1>

<p align="center">
  <strong>App de notas personal con cifrado, biometría y privacidad total.</strong><br>
  Sin cuenta. Sin internet. Sin anuncios.
</p>

<p align="center">
  <a href="https://github.com/isaac579/itzli-app/releases/download/v1.0/itzli-v1.0.apk">
    <img src="https://img.shields.io/badge/⬇️%20Descargar%20APK-v1.0-black?style=for-the-badge&logo=android&logoColor=white" alt="Descargar APK">
  </a>
  &nbsp;
  <img src="https://img.shields.io/badge/Android-8.0%2B-black?style=for-the-badge&logo=android" alt="Android 8.0+">
  &nbsp;
  <img src="https://img.shields.io/badge/Kotlin-2.0-black?style=for-the-badge&logo=kotlin" alt="Kotlin 2.0">
</p>

---

## ¿Qué es Itzli?

**Itzli** (del náhuatl: *obsidiana*) es una app de notas privada para Android. Todo lo que escribas se queda en tu teléfono — nada sale a servidores externos.

Incluye tres módulos principales:

| Módulo | Descripción |
|--------|-------------|
| 📝 **Notas** | Texto libre, listas de tareas, notas cifradas y notas con imagen |
| ✅ **Listas** | Checklists independientes con drag & drop y contadores |
| 🔐 **Contraseñas** | Bóveda cifrada con indicador de seguridad y categorías |

---

## Funciones

- **Notas de color** — 7 tonos oscuros estilo obsidiana para organizar visualmente
- **Notas seguras** — contenido cifrado con AES/GCM, protegido por tu PIN
- **Notas con imagen** — adjunta fotos directamente desde la galería
- **Recordatorios** — alarmas exactas con notificación push
- **Etiquetas** — organiza y filtra notas con etiquetas personalizadas
- **Papelera** — recupera notas eliminadas hasta 30 días después
- **Búsqueda global** — busca en notas, listas y contraseñas al mismo tiempo
- **Widget** — muestra tus últimas 3 notas en la pantalla principal
- **Biometría** — desbloquea con huella dactilar o reconocimiento facial
- **PIN de 4 dígitos** — bloqueo de app con PIN como respaldo

---

## Seguridad

| Capa | Tecnología |
|------|-----------|
| Cifrado de notas seguras | AES/GCM — AndroidKeyStore |
| Almacenamiento de PIN | SHA-256 + EncryptedSharedPreferences |
| Bóveda de contraseñas | AES/GCM — AndroidKeyStore |
| Sin red | La app no solicita permiso de internet |

---

## Descarga e instalación

> **Requiere Android 8.0 (API 26) o superior.**

1. Descarga el APK:

<p align="center">
  <a href="https://github.com/isaac579/itzli-app/releases/download/v1.0/itzli-v1.0.apk">
    <img src="https://img.shields.io/badge/⬇️%20Descargar%20Itzli%20v1.0.apk-Gratis-black?style=for-the-badge&logo=android&logoColor=white&labelColor=111111" alt="Descargar APK">
  </a>
</p>

2. En tu teléfono ve a **Ajustes → Seguridad → Instalar apps desconocidas** y actívalo para tu navegador o administrador de archivos.
3. Abre el APK descargado e instala.
4. Al abrir la app crea tu PIN de 4 dígitos y listo.

---

## Stack técnico

- **Lenguaje:** Kotlin 2.0
- **UI:** Jetpack Compose + Material 3
- **Arquitectura:** MVVM con StateFlow
- **Base de datos:** Room (SQLite) v4
- **Seguridad:** AndroidKeyStore · EncryptedSharedPreferences · Biometric API
- **Navegación:** Navigation Compose
- **Widget:** Glance AppWidget
- **Imágenes:** Coil 2.6

---

## Privacidad

- No requiere cuenta ni registro
- No usa internet (sin permiso `INTERNET` en el manifiesto)
- Todos los datos se almacenan localmente en el dispositivo
- Las notas cifradas y contraseñas no pueden leerse sin el PIN

---

<p align="center">
  Desarrollado por <strong>ICH · Isaac Calva Hernandez</strong>
</p>
