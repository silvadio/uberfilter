package com.uberfilter.data

import androidx.room.*
import com.uberfilter.model.GoalHistoryEntry
import com.uberfilter.model.GoalType
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalHistoryDao {

    @Query("SELECT * FROM goal_history WHERE goalType = :type ORDER BY periodEnd DESC")
    fun getByType(type: GoalType): Flow<List<GoalHistoryEntry>>

    @Query("SELECT * FROM goal_history ORDER BY periodEnd DESC")
    fun getAll(): Flow<List<GoalHistoryEntry>>

    @Query("SELECT * FROM goal_history WHERE periodKey = :key LIMIT 1")
    suspend fun getByKey(key: String): GoalHistoryEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: GoalHistoryEntry)

    @Delete
    suspend fun delete(entry: GoalHistoryEntry)
}
