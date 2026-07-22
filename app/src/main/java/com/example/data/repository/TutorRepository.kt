package com.example.data.repository

import android.util.Log
import com.example.data.local.ChatMessageEntity
import com.example.data.local.TutorDao
import com.example.data.local.UserAccountEntity
import com.example.data.local.UserStatsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class TutorRepository(private val tutorDao: TutorDao) {

    val allMessages: Flow<List<ChatMessageEntity>> = tutorDao.getAllMessages()
        .catch { e ->
            Log.e("TutorRepository", "Error reading chat messages from Room DB", e)
            emit(emptyList())
        }

    val bookmarkedMessages: Flow<List<ChatMessageEntity>> = tutorDao.getBookmarkedMessages()
        .catch { e ->
            Log.e("TutorRepository", "Error reading bookmarked messages from Room DB", e)
            emit(emptyList())
        }

    val userStats: Flow<UserStatsEntity?> = tutorDao.getUserStats()
        .catch { e ->
            Log.e("TutorRepository", "Error reading user stats from Room DB", e)
            emit(null)
        }

    suspend fun insertMessage(message: ChatMessageEntity): Result<Long> {
        return try {
            val id = tutorDao.insertMessage(message)
            Result.success(id)
        } catch (e: Exception) {
            Log.e("TutorRepository", "Failed to insert chat message into Room DB", e)
            Result.failure(e)
        }
    }

    suspend fun updateBookmark(messageId: Long, isBookmarked: Boolean): Result<Unit> {
        return try {
            tutorDao.updateMessageBookmark(messageId, isBookmarked)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TutorRepository", "Failed to update bookmark status", e)
            Result.failure(e)
        }
    }

    suspend fun clearAllMessages(): Result<Unit> {
        return try {
            tutorDao.clearAllMessages()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TutorRepository", "Failed to clear chat messages", e)
            Result.failure(e)
        }
    }

    suspend fun ensureWelcomeMessageIfEmpty(): Result<Unit> {
        return try {
            val count = tutorDao.getMessageCount()
            if (count == 0) {
                val welcomeMsg = ChatMessageEntity(
                    sender = "MAHIM",
                    text = "হ্যালো ছোট্ট বন্ধু! 👋 আমি মাহিম এআই টিউটর (Mahim AI Tutor)। তোমার আজ কী শিখতে বা জানতে ইচ্ছে করছে? যেকোনো প্রশ্ন করতে পারো—বিজ্ঞান, গণিত, মহাকাশ বা পশুপাখির গল্প!",
                    categoryTag = "Welcome"
                )
                tutorDao.insertMessage(welcomeMsg)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TutorRepository", "Failed to ensure welcome message", e)
            Result.failure(e)
        }
    }

    suspend fun insertOrUpdateUserStats(stats: UserStatsEntity): Result<Unit> {
        return try {
            tutorDao.insertOrUpdateUserStats(stats)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TutorRepository", "Failed to update user stats in Room DB", e)
            Result.failure(e)
        }
    }

    suspend fun insertUserAccount(user: UserAccountEntity): Result<Unit> {
        return try {
            tutorDao.insertUserAccount(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TutorRepository", "Failed to insert user account", e)
            Result.failure(e)
        }
    }

    suspend fun getUserAccountByEmail(email: String): Result<UserAccountEntity?> {
        return try {
            val user = tutorDao.getUserAccountByEmail(email)
            Result.success(user)
        } catch (e: Exception) {
            Log.e("TutorRepository", "Failed to fetch user account by email", e)
            Result.failure(e)
        }
    }

    suspend fun getUserAccountByIdentifier(identifier: String): Result<UserAccountEntity?> {
        return try {
            val user = tutorDao.getUserAccountByIdentifier(identifier)
            Result.success(user)
        } catch (e: Exception) {
            Log.e("TutorRepository", "Failed to fetch user account by identifier", e)
            Result.failure(e)
        }
    }
}
