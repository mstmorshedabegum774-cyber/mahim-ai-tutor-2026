package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey
    val id: Int = 1,
    val starsCount: Int = 5,
    val totalQuestionsAsked: Int = 0,
    val earnedBadgesCsv: String = "কৌতূহলী শিক্ষার্থী,ছোট্ট গবেষক"
)
