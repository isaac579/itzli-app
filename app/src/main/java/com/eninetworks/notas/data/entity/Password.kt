package com.eninetworks.notas.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class Password(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val username: String,
    val encryptedPassword: String,
    val website: String = "",
    val notes: String = "",
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
