package com.eninetworks.notas.data.entity

enum class NoteType(val label: String, val emoji: String) {
    TEXT("Texto", "📝"),
    PINNED("Fijada", "📌"),
    LIST("Lista", "☑️"),
    SECURE("Segura", "🔒")
}

fun Note.noteType(): NoteType = try { NoteType.valueOf(type) } catch (e: Exception) { NoteType.TEXT }

fun parseListItems(content: String): List<Pair<String, Boolean>> {
    if (content.isBlank()) return emptyList()
    return content.split("\n").mapNotNull { line ->
        val i = line.lastIndexOf("::")
        if (i >= 0) Pair(line.substring(0, i), line.substring(i + 2).toBooleanStrictOrNull() ?: false)
        else if (line.isNotBlank()) Pair(line, false)
        else null
    }
}

fun buildListContent(items: List<Pair<String, Boolean>>): String =
    items.joinToString("\n") { "${it.first}::${it.second}" }
