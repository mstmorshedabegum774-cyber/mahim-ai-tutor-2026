package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TutorDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("UPDATE chat_messages SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateMessageBookmark(id: Long, isBookmarked: Boolean)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getMessageCount(): Int

    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserStats(stats: UserStatsEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUserAccount(user: UserAccountEntity)

    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getUserAccountByEmail(email: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE LOWER(email) = LOWER(:identifier) OR LOWER(username) = LOWER(:identifier) LIMIT 1")
    suspend fun getUserAccountByIdentifier(identifier: String): UserAccountEntity?
}
