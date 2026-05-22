package com.uberfilter.data

import androidx.room.*
import com.uberfilter.model.EvaluationColor
import com.uberfilter.model.RideRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface RideHistoryDao {

    @Query("SELECT * FROM ride_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<RideRecord>>

    @Query("SELECT * FROM ride_history WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getByPeriod(start: Long, end: Long): Flow<List<RideRecord>>

    @Query("SELECT * FROM ride_history WHERE color = :color ORDER BY timestamp DESC")
    fun getByColor(color: EvaluationColor): Flow<List<RideRecord>>

    @Query("SELECT COUNT(*) FROM ride_history WHERE timestamp BETWEEN :start AND :end")
    fun countInPeriod(start: Long, end: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(totalValue + bonusValue), 0) FROM ride_history WHERE timestamp BETWEEN :start AND :end")
    fun sumEffectiveValue(start: Long, end: Long): Flow<Double>

    @Query("SELECT * FROM ride_history WHERE offerId = :offerId LIMIT 1")
    suspend fun getByOfferId(offerId: String): RideRecord?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: RideRecord): Long

    @Delete
    suspend fun delete(record: RideRecord)
}
