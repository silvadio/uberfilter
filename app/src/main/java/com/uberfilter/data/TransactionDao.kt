package com.uberfilter.data

import androidx.room.*
import com.uberfilter.model.Transaction
import com.uberfilter.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // ── Consultas de lista ──────────────────────────────────────────────────

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getByType(type: TransactionType): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getByPeriod(start: Long, end: Long): Flow<List<Transaction>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE type = :type AND date BETWEEN :start AND :end
        ORDER BY date DESC
        """
    )
    fun getByTypeAndPeriod(type: TransactionType, start: Long, end: Long): Flow<List<Transaction>>

    // ── Agregações ──────────────────────────────────────────────────────────

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE type = 'INCOME' AND date BETWEEN :start AND :end
        """
    )
    fun sumIncome(start: Long, end: Long): Flow<Double>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE type = 'EXPENSE' AND date BETWEEN :start AND :end
        """
    )
    fun sumExpense(start: Long, end: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM transactions WHERE date BETWEEN :start AND :end")
    fun countInPeriod(start: Long, end: Long): Flow<Int>

    // ── Escrita ─────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?
}
