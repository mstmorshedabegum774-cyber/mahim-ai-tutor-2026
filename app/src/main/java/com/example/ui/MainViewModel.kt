package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.UserAccountEntity
import com.example.data.local.UserStatsEntity
import com.example.data.remote.GeminiRepository
import com.example.data.repository.TutorRepository
import com.example.util.TtsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = TutorRepository(db.tutorDao())
    private val geminiRepo = GeminiRepository()

    val ttsHelper = TtsHelper(application)

    private val prefs = application.getSharedPreferences("mahim_tutor_prefs", Context.MODE_PRIVATE)

    // Dark Mode Theme State
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        prefs.edit().putBoolean("is_dark_mode", newValue).apply()
    }

    // User Auth State
    private val _currentUser = MutableStateFlow<UserAccountEntity?>(null)
    val currentUser: StateFlow<UserAccountEntity?> = _currentUser.asStateFlow()

    private val _isGuest = MutableStateFlow(prefs.getBoolean("is_guest_mode", false))
    val isGuest: StateFlow<Boolean> = _isGuest.asStateFlow()

    // UI States - Local Room Database persistence
    val messages: StateFlow<List<ChatMessageEntity>> = repository.allMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val bookmarkedMessages: StateFlow<List<ChatMessageEntity>> = repository.bookmarkedMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userStats: StateFlow<UserStatsEntity?> = repository.userStats
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _learningMode = MutableStateFlow("GENERAL") // "GENERAL", "QUIZ", "STORY"
    val learningMode: StateFlow<String> = _learningMode.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Chat, 1: Favorites, 2: Badges
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _showCelebrationDialog = MutableStateFlow<String?>(null)
    val showCelebrationDialog: StateFlow<String?> = _showCelebrationDialog.asStateFlow()

    init {
        // Restore active user session from Room / SharedPreferences
        val savedEmail = prefs.getString("logged_in_email", null)
        if (!savedEmail.isNullOrEmpty()) {
            viewModelScope.launch {
                repository.getUserAccountByEmail(savedEmail).onSuccess { user ->
                    if (user != null) {
                        _currentUser.value = user
                    }
                }
            }
        }

        // Initialize default user stats in Room if missing
        viewModelScope.launch {
            repository.userStats.collect { stats ->
                if (stats == null) {
                    repository.insertOrUpdateUserStats(
                        UserStatsEntity(
                            id = 1,
                            starsCount = 5,
                            totalQuestionsAsked = 0,
                            earnedBadgesCsv = "কৌতূহলী শিক্ষার্থী,প্রথম কথপোকথন"
                        )
                    )
                }
            }
        }

        // Ensure welcome message in Room database if chat history is empty
        viewModelScope.launch {
            repository.ensureWelcomeMessageIfEmpty()
        }
    }

    fun login(identifier: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.getUserAccountByIdentifier(identifier).onSuccess { user ->
                if (user == null) {
                    onResult(false, "এই ইমেইল বা ইউজারনেম পাওয়া যায়নি।")
                } else if (user.passwordHash != password) {
                    onResult(false, "পাসওয়ার্ড সঠিক নয়।")
                } else {
                    _currentUser.value = user
                    _isGuest.value = false
                    prefs.edit().putString("logged_in_email", user.email).putBoolean("is_guest_mode", false).apply()
                    onResult(true, null)
                }
            }.onFailure { e ->
                Log.e("MainViewModel", "Login error", e)
                onResult(false, "লগইন করার সময় ভুল হয়েছে: ${e.message}")
            }
        }
    }

    fun signUp(username: String, email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.getUserAccountByEmail(email).onSuccess { existing ->
                if (existing != null) {
                    onResult(false, "এই ইমেইল দিয়ে ইতোমধ্যে অ্যাকাউন্ট আছে। লগইন করার চেষ্টা করুন।")
                    return@onSuccess
                }

                val newUser = UserAccountEntity(
                    email = email,
                    username = username,
                    passwordHash = password
                )
                repository.insertUserAccount(newUser).onSuccess {
                    _currentUser.value = newUser
                    _isGuest.value = false
                    prefs.edit().putString("logged_in_email", email).putBoolean("is_guest_mode", false).apply()
                    onResult(true, null)
                }.onFailure { e ->
                    Log.e("MainViewModel", "SignUp error", e)
                    onResult(false, "অ্যাকাউন্ট তৈরি সম্ভব হয়নি। অন্য ইমেইল দিয়ে চেষ্টা করুন।")
                }
            }.onFailure { e ->
                Log.e("MainViewModel", "SignUp error", e)
                onResult(false, "অ্যাকাউন্ট তৈরি সম্ভব হয়নি। অন্য ইমেইল দিয়ে চেষ্টা করুন।")
            }
        }
    }

    fun loginAsGuest() {
        _isGuest.value = true
        prefs.edit().putBoolean("is_guest_mode", true).apply()
    }

    fun logout() {
        _currentUser.value = null
        _isGuest.value = false
        prefs.edit().remove("logged_in_email").putBoolean("is_guest_mode", false).apply()
    }

    fun setLearningMode(mode: String) {
        _learningMode.value = mode
    }

    fun setSelectedTab(index: Int) {
        _selectedTab.value = index
    }

    fun dismissCelebration() {
        _showCelebrationDialog.value = null
    }

    fun sendMessage(userText: String, imageUri: String? = null) {
        if (userText.isBlank() && imageUri.isNullOrBlank()) return
        if (_isLoading.value) return

        val trimmedText = userText.trim()
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            // 1. Save User message to Room database
            val userMsg = ChatMessageEntity(
                sender = "USER",
                text = trimmedText,
                categoryTag = _learningMode.value,
                imageUri = imageUri
            )
            repository.insertMessage(userMsg)

            // 2. Update user stats in Room database
            val currentStats = userStats.value ?: UserStatsEntity()
            val newQuestionsCount = currentStats.totalQuestionsAsked + 1
            val newStars = currentStats.starsCount + 1

            val existingBadges = currentStats.earnedBadgesCsv.split(",").toMutableList()
            var newlyEarnedBadge: String? = null

            if (newQuestionsCount == 5 && !existingBadges.contains("বিজ্ঞানী মন")) {
                existingBadges.add("বিজ্ঞানী মন")
                newlyEarnedBadge = "বিজ্ঞানী মন"
            } else if (newQuestionsCount == 10 && !existingBadges.contains("জ্ঞান পিপাসু")) {
                existingBadges.add("জ্ঞান পিপাসু")
                newlyEarnedBadge = "জ্ঞান পিপাসু"
            } else if (_learningMode.value == "QUIZ" && !existingBadges.contains("ধাঁধা মাষ্টার")) {
                existingBadges.add("ধাঁধা মাষ্টার")
                newlyEarnedBadge = "ধাঁধা মাষ্টার"
            }

            repository.insertOrUpdateUserStats(
                currentStats.copy(
                    starsCount = newStars,
                    totalQuestionsAsked = newQuestionsCount,
                    earnedBadgesCsv = existingBadges.joinToString(",")
                )
            )

            if (newlyEarnedBadge != null) {
                _showCelebrationDialog.value = newlyEarnedBadge
            }

            // Read image bytes if imageUri is present
            val imageBytes: ByteArray? = if (!imageUri.isNullOrBlank()) {
                kotlin.runCatching {
                    val uri = android.net.Uri.parse(imageUri)
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use {
                        it.readBytes()
                    }
                }.getOrNull()
            } else null

            // 3. Build context history from recent messages in Room
            val recentList = messages.value
                .filter { it.categoryTag != "Error" && it.categoryTag != "Welcome" }
                .takeLast(6)
                .map { Pair(it.sender, it.text) }

            // 4. Call Gemini Repository
            val result = geminiRepo.generateTutorResponse(
                prompt = trimmedText,
                history = recentList,
                learningMode = _learningMode.value,
                imageBytes = imageBytes
            )

            result.onSuccess { tutorReply ->
                val mahimMsg = ChatMessageEntity(
                    sender = "MAHIM",
                    text = tutorReply,
                    categoryTag = _learningMode.value
                )
                repository.insertMessage(mahimMsg).onSuccess { msgId ->
                    ttsHelper.speak(tutorReply, msgId)
                }
                _isLoading.value = false
            }.onFailure { err ->
                Log.e("MainViewModel", "Error getting tutor response", err)
                val fallbackText = if (err.message?.contains("key not configured") == true) {
                    "ছোট্ট বন্ধু! AI Studio-র Secrets প্যানেলে GEMINI_API_KEY যুক্ত করলে আমি তোমার সব নতুন প্রশ্নের ঝটপট সুন্দর উত্তর দেব! তোমার প্রশ্নটি ছিল: \"$trimmedText\" - এটি খুব বুদ্ধিমত্তার প্রশ্ন!"
                } else {
                    "আহা! নেটওয়ার্ক কানেকশন একটু ব্যস্ত ছিল। তবে বন্ধু, তোমার প্রশ্ন: \"$trimmedText\" চমৎকার! আর একবার প্রশ্ন করে দেখো তো, আমি প্রস্তুত!"
                }
                val fallbackMsg = ChatMessageEntity(
                    sender = "MAHIM",
                    text = fallbackText,
                    categoryTag = _learningMode.value
                )
                repository.insertMessage(fallbackMsg).onSuccess { msgId ->
                    ttsHelper.speak(fallbackText, msgId)
                }
                _isLoading.value = false
            }
        }
    }

    fun toggleBookmark(messageId: Long, currentBookmarked: Boolean) {
        viewModelScope.launch {
            repository.updateBookmark(messageId, !currentBookmarked)
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearAllMessages()
            val welcomeMsg = ChatMessageEntity(
                sender = "MAHIM",
                text = "হ্যালো ছোট্ট বন্ধু! 👋 আমি মাহিম এআই টিউটর (Mahim AI Tutor)। নতুন করে আলোচনা শুরু করতে যেকোনো প্রশ্ন করো!",
                categoryTag = "Welcome"
            )
            repository.insertMessage(welcomeMsg)
        }
    }

    fun speakMessage(text: String, messageId: Long) {
        if (ttsHelper.currentlySpeakingId.value == messageId && ttsHelper.isSpeaking.value) {
            ttsHelper.stop()
        } else {
            ttsHelper.speak(text, messageId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
    }
}
