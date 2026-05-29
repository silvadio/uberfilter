package com.driveq.data

import com.driveq.model.GoalHistoryEntry
import com.driveq.model.GoalType
import kotlinx.coroutines.flow.Flow

class GoalHistoryRepository(private val dao: GoalHistoryDao) {

    fun getByType(type: GoalType): Flow<List<GoalHistoryEntry>> = dao.getByType(type)

    fun getAll(): Flow<List<GoalHistoryEntry>> = dao.getAll()

    suspend fun getByKey(key: String): GoalHistoryEntry? = dao.getByKey(key)

    suspend fun saveSnapshot(entry: GoalHistoryEntry) = dao.insert(entry)
}
