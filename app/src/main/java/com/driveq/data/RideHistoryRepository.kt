package com.driveq.data

import com.driveq.model.EvaluationColor
import com.driveq.model.RideRecord
import kotlinx.coroutines.flow.Flow

class RideHistoryRepository(private val dao: RideHistoryDao) {

    fun getAll(): Flow<List<RideRecord>> = dao.getAll()

    fun getByPeriod(start: Long, end: Long): Flow<List<RideRecord>> =
        dao.getByPeriod(start, end)

    fun getByColor(color: EvaluationColor): Flow<List<RideRecord>> =
        dao.getByColor(color)

    fun countInPeriod(start: Long, end: Long): Flow<Int> =
        dao.countInPeriod(start, end)

    fun sumEffectiveValue(start: Long, end: Long): Flow<Double> =
        dao.sumEffectiveValue(start, end)

    suspend fun getByOfferId(offerId: String): RideRecord? =
        dao.getByOfferId(offerId)

    suspend fun insert(record: RideRecord): Long = dao.insert(record)

    suspend fun delete(record: RideRecord) = dao.delete(record)
}
