package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "issues")
data class Issue(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reporterId: String,
    val reporterName: String,
    val photoPath: String?, // Location of local image file (or "sample:category" for seeded issues)
    val latitude: Double,
    val longitude: Double,
    val category: String, // pothole, water_leak, streetlight, waste, drainage, other
    val severity: Int, // 1-5
    val description: String,
    val votes: Int = 0,
    val status: String, // reported, in_progress, fixed, dismissed
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val hasVoted: Boolean = false // Stores whether the local user has upvoted this issue
)
