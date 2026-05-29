package com.driveq.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String? = null,
    val googleId: String? = null,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
