package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CivicRepository(
    private val issueDao: IssueDao,
    private val userDao: UserDao
) {
    val allIssues: Flow<List<Issue>> = issueDao.getAllIssues()
    val topUsers: Flow<List<User>> = userDao.getTopUsers()

    suspend fun insertIssue(issue: Issue): Long {
        return issueDao.insertIssue(issue)
    }

    suspend fun updateIssue(issue: Issue) {
        issueDao.updateIssue(issue)
    }

    suspend fun getIssueById(id: Int): Issue? {
        return issueDao.getIssueById(id)
    }

    suspend fun getUserById(id: String): User? {
        return userDao.getUserById(id)
    }

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun seedDatabaseIfEmpty() {
        // Check issues count
        val currentIssues = allIssues.first()
        if (currentIssues.isEmpty()) {
            // Seed Issues
            val sampleIssues = listOf(
                Issue(
                    reporterId = "user_2",
                    reporterName = "Anjali Sharma",
                    photoPath = "sample:pothole",
                    latitude = 26.9124,
                    longitude = 75.7873,
                    category = "pothole",
                    severity = 4,
                    description = "Huge pothole near the Malviya Nagar main market crossing. Extremely dangerous for two-wheelers during night.",
                    votes = 23,
                    status = "reported",
                    createdAt = System.currentTimeMillis() - 86400000L * 3 // 3 days ago
                ),
                Issue(
                    reporterId = "user_3",
                    reporterName = "Amit Patel",
                    photoPath = "sample:water_leak",
                    latitude = 26.9210,
                    longitude = 75.8070,
                    category = "water_leak",
                    severity = 3,
                    description = "Major freshwater pipe leakage near C-Scheme park. Hundreds of gallons being wasted daily.",
                    votes = 18,
                    status = "in_progress",
                    createdAt = System.currentTimeMillis() - 86400000L * 5
                ),
                Issue(
                    reporterId = "user_2",
                    reporterName = "Anjali Sharma",
                    photoPath = "sample:streetlight",
                    latitude = 26.9124,
                    longitude = 75.8263,
                    category = "streetlight",
                    severity = 5,
                    description = "Broken streetlight on the flyover near Jaipur Railway Station. Highly unsafe for women commuting late.",
                    votes = 31,
                    status = "fixed",
                    createdAt = System.currentTimeMillis() - 86400000L * 7
                ),
                Issue(
                    reporterId = "user_4",
                    reporterName = "Neha Gupta",
                    photoPath = "sample:waste",
                    latitude = 26.8975,
                    longitude = 75.7654,
                    category = "waste",
                    severity = 3,
                    description = "Piles of uncollected household garbage near Ram Nagar community center. Stench is unbearable.",
                    votes = 12,
                    status = "reported",
                    createdAt = System.currentTimeMillis() - 86400000L * 1
                ),
                Issue(
                    reporterId = "user_5",
                    reporterName = "Vikram Singh",
                    photoPath = "sample:water_leak",
                    latitude = 26.9245,
                    longitude = 75.8180,
                    category = "water_leak",
                    severity = 4,
                    description = "Broken pipeline on Mirza Ismail Road creating waterlogging on the left lane.",
                    votes = 15,
                    status = "reported",
                    createdAt = System.currentTimeMillis() - 86400000L * 2
                ),
                Issue(
                    reporterId = "user_4",
                    reporterName = "Neha Gupta",
                    photoPath = "sample:drainage",
                    latitude = 26.8732,
                    longitude = 75.7612,
                    category = "drainage",
                    severity = 4,
                    description = "Sewer overflow onto the road in Mansarovar Sector 3, causing massive traffic slowdowns.",
                    votes = 9,
                    status = "reported",
                    createdAt = System.currentTimeMillis() - 43200000L // 12 hours ago
                ),
                Issue(
                    reporterId = "user_2",
                    reporterName = "Anjali Sharma",
                    photoPath = "sample:pothole",
                    latitude = 26.9031,
                    longitude = 75.7382,
                    category = "pothole",
                    severity = 5,
                    description = "Massive ditch in the middle of Vaishali Nagar main block road. Municipal barricades have fallen inside.",
                    votes = 14,
                    status = "in_progress",
                    createdAt = System.currentTimeMillis() - 86400000L * 4
                ),
                Issue(
                    reporterId = "user_6",
                    reporterName = "Priya Mehta",
                    photoPath = "sample:waste",
                    latitude = 26.9118,
                    longitude = 75.8193,
                    category = "waste",
                    severity = 2,
                    description = "Discarded commercial packaging dumped open on the sidewalk opposite Albert Hall Museum.",
                    votes = 27,
                    status = "reported",
                    createdAt = System.currentTimeMillis() - 86400000L * 6
                ),
                Issue(
                    reporterId = "user_3",
                    reporterName = "Amit Patel",
                    photoPath = "sample:streetlight",
                    latitude = 26.8821,
                    longitude = 75.7995,
                    category = "streetlight",
                    severity = 3,
                    description = "Three streetlights in a row are non-functional on Tonk Road opposite the central shopping complex.",
                    votes = 19,
                    status = "reported",
                    createdAt = System.currentTimeMillis() - 86400000L * 1
                ),
                Issue(
                    reporterId = "user_5",
                    reporterName = "Vikram Singh",
                    photoPath = "sample:drainage",
                    latitude = 26.8984,
                    longitude = 75.8291,
                    category = "drainage",
                    severity = 4,
                    description = "Clogged drainage channel in Raja Park block C has been cleared by community effort, now flowing fine.",
                    votes = 8,
                    status = "fixed",
                    createdAt = System.currentTimeMillis() - 86400000L * 10
                ),
                Issue(
                    reporterId = "user_6",
                    reporterName = "Priya Mehta",
                    photoPath = "sample:pothole",
                    latitude = 26.9239,
                    longitude = 75.8267,
                    category = "pothole",
                    severity = 5,
                    description = "Deep crater on the Hawa Mahal outer circle road. Road was repaired and filled yesterday.",
                    votes = 35,
                    status = "fixed",
                    createdAt = System.currentTimeMillis() - 86400000L * 8
                )
            )
            sampleIssues.forEach { issueDao.insertIssue(it) }
        }

        // Check if users exist, seed leaderboard
        val firstUser = userDao.getUserById("user_2")
        if (firstUser == null) {
            val sampleUsers = listOf(
                User("user_2", "Anjali Sharma", 185, 12, "Pothole Hunter, Civic Guardian"),
                User("user_3", "Amit Patel", 145, 10, "Water Savior, Active Citizen"),
                User("user_4", "Neha Gupta", 120, 8, "Civic Guardian, Active Citizen"),
                User("user_5", "Vikram Singh", 95, 6, "First Responder"),
                User("user_6", "Priya Mehta", 80, 4, "Eco Defender"),
                User("user_1", "You (Community Hero)", 10, 1, "First Responder") // Local active user profile
            )
            sampleUsers.forEach { userDao.insertUser(it) }
        }
    }
}
