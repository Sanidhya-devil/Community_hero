package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.AppDatabase
import com.example.data.CivicRepository
import com.example.data.Issue
import com.example.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class Tab {
    MAP, REPORTS, DASHBOARD, LEADERBOARD, PROFILE
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MainViewModel"
    private val repository: CivicRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CivicRepository(database.issueDao(), database.userDao())
        
        // Seed the database with Jaipur sample data when initializing
        viewModelScope.launch {
            try {
                repository.seedDatabaseIfEmpty()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to seed database: ${e.message}", e)
            }
        }
    }

    // --- UI Navigation State ---
    private val _currentTab = MutableStateFlow(Tab.MAP)
    val currentTab: StateFlow<Tab> = _currentTab.asStateFlow()

    private val _selectedIssue = MutableStateFlow<Issue?>(null)
    val selectedIssue: StateFlow<Issue?> = _selectedIssue.asStateFlow()

    // --- Filter States ---
    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<String?>(null)
    val selectedStatusFilter: StateFlow<String?> = _selectedStatusFilter.asStateFlow()

    // --- Issue Data Stream ---
    val allIssues: StateFlow<List<Issue>> = repository.allIssues
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered Issues for map or list views
    val filteredIssues: StateFlow<List<Issue>> = combine(
        allIssues,
        _selectedCategoryFilter,
        _selectedStatusFilter
    ) { issues, catFilter, statusFilter ->
        issues.filter { issue ->
            val matchesCat = catFilter == null || issue.category == catFilter
            val matchesStatus = statusFilter == null || issue.status == statusFilter
            matchesCat && matchesStatus
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Leaderboard Data Stream ---
    val topUsers: StateFlow<List<User>> = repository.topUsers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Current Local User Profile ---
    private val _localUser = MutableStateFlow<User?>(null)
    val localUser: StateFlow<User?> = _localUser.asStateFlow()

    init {
        // Load the local user ("user_1")
        viewModelScope.launch {
            repository.topUsers.collect { _ ->
                val user = repository.getUserById("user_1")
                _localUser.value = user
            }
        }
    }

    // --- Reporting Form State ---
    private val _isReporting = MutableStateFlow(false)
    val isReporting: StateFlow<Boolean> = _isReporting.asStateFlow()

    private val _capturedBitmap = MutableStateFlow<Bitmap?>(null)
    val capturedBitmap: StateFlow<Bitmap?> = _capturedBitmap.asStateFlow()

    private val _userDescription = MutableStateFlow("")
    val userDescription: StateFlow<String> = _userDescription.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submissionResult = MutableStateFlow<String?>(null)
    val submissionResult: StateFlow<String?> = _submissionResult.asStateFlow()

    // --- Actions ---
    fun setCurrentTab(tab: Tab) {
        _currentTab.value = tab
        // Reset selected issue or form when switching tabs
        if (tab != Tab.MAP) {
            _selectedIssue.value = null
        }
    }

    fun setSelectedIssue(issue: Issue?) {
        _selectedIssue.value = issue
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun setStatusFilter(status: String?) {
        _selectedStatusFilter.value = status
    }

    fun startReporting(bitmap: Bitmap) {
        _capturedBitmap.value = bitmap
        _userDescription.value = ""
        _submissionResult.value = null
        _isReporting.value = true
    }

    fun cancelReporting() {
        _isReporting.value = false
        _capturedBitmap.value = null
        _userDescription.value = ""
    }

    fun updateUserDescription(desc: String) {
        _userDescription.value = desc
    }

    fun submitReport(latitude: Double, longitude: Double) {
        val bitmap = _capturedBitmap.value ?: return
        _isSubmitting.value = true
        _submissionResult.value = null

        viewModelScope.launch {
            try {
                // Save captured bitmap locally to persistent storage
                val photoPath = saveBitmapToCache(bitmap)

                // Call Gemini Vision API to auto-categorize and score severity
                val analysis = GeminiClient.analyzeCivicIssue(bitmap, _userDescription.value)

                // Create the new issue
                val newIssue = Issue(
                    reporterId = "user_1",
                    reporterName = "You (Community Hero)",
                    photoPath = photoPath,
                    latitude = latitude,
                    longitude = longitude,
                    category = analysis.category,
                    severity = analysis.severity,
                    description = analysis.description,
                    votes = 0,
                    status = "reported"
                )

                // Insert into Room
                repository.insertIssue(newIssue)

                // Update Local User points (+10 for report)
                updateLocalUserStats(pointsEarned = 10, isNewReport = true)

                _submissionResult.value = "Success! Issue categorized as ${analysis.category.replace("_", " ").uppercase()} (Severity: ${analysis.severity}/5)"
                _capturedBitmap.value = null
                _userDescription.value = ""
                _isReporting.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Failed to submit report: ${e.message}", e)
                _submissionResult.value = "Error: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun toggleVote(issueId: Int) {
        viewModelScope.launch {
            val issue = repository.getIssueById(issueId) ?: return@launch
            val wasVoted = issue.hasVoted
            val updatedVotes = if (wasVoted) issue.votes - 1 else issue.votes + 1
            
            // Toggle local vote status
            val updatedIssue = issue.copy(
                votes = updatedVotes.coerceAtLeast(0),
                hasVoted = !wasVoted,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateIssue(updatedIssue)

            // Adjust voter points (+1 point for participation on vote, -1 point if unvoted)
            updateLocalUserStats(pointsEarned = if (wasVoted) -1 else 1, isNewReport = false)
        }
    }

    fun updateIssueStatus(issueId: Int, newStatus: String) {
        viewModelScope.launch {
            val issue = repository.getIssueById(issueId) ?: return@launch
            val updatedIssue = issue.copy(
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateIssue(updatedIssue)
        }
    }

    private suspend fun updateLocalUserStats(pointsEarned: Int, isNewReport: Boolean) {
        val user = repository.getUserById("user_1") ?: User(
            id = "user_1",
            name = "You (Community Hero)",
            points = 0,
            issuesReported = 0,
            badges = "First Responder"
        )

        val updatedPoints = (user.points + pointsEarned).coerceAtLeast(0)
        val updatedReports = if (isNewReport) user.issuesReported + 1 else user.issuesReported

        // Re-evaluate gamification badges dynamically
        val badgesList = user.badges.split(", ").map { it.trim() }.toMutableSet()
        if (updatedReports >= 1) {
            badgesList.add("First Responder")
        }
        if (updatedReports >= 3) {
            badgesList.add("Civic Guardian")
        }
        if (updatedReports >= 5) {
            badgesList.add("Pothole Hunter")
        }
        if (updatedPoints >= 100) {
            badgesList.add("Elite Vigilante")
        }

        val updatedUser = user.copy(
            points = updatedPoints,
            issuesReported = updatedReports,
            badges = badgesList.joinToString(", ")
        )
        repository.insertUser(updatedUser)
        _localUser.value = updatedUser
    }

    private fun saveBitmapToCache(bitmap: Bitmap): String {
        val context = getApplication<Application>()
        val directory = File(context.filesDir, "issue_photos")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, "img_${System.currentTimeMillis()}.jpg")
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
        return file.absolutePath
    }

    fun clearSubmissionResult() {
        _submissionResult.value = null
    }
}
