package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey
    val email: String,
    val username: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)
