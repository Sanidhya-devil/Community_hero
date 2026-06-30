package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String, // e.g. "user_1", "user_2"
    val name: String,
    val points: Int = 0,
    val issuesReported: Int = 0,
    val badges: String, // Comma-separated list (e.g. "Pothole Hunter, Civic Guardian")
    val createdAt: Long = System.currentTimeMillis()
)
