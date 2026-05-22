package com.uberfilter.data

import com.uberfilter.model.Transaction
import com.uberfilter.model.TransactionType
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val dao: TransactionDao) {

    // ── Fluxos reativos ─────────────────────────────────────────────────────

    fun getAll(): Flow<List<Transaction>> = dao.getAll()

    fun getByType(type: TransactionType): Flow<List<Transaction>> = dao.getByType(type)

    fun getByPeriod(start: Long, end: Long): Flow<List<Transaction>> =
        dao.getByPeriod(start, end)

    fun getByTypeAndPeriod(type: TransactionType, start: Long, end: Long): Flow<List<Transaction>> =
        dao.getByTypeAndPeriod(type, start, end)

    fun sumIncome(start: Long, end: Long): Flow<Double> = dao.sumIncome(start, end)

    fun sumExpense(start: Long, end: Long): Flow<Double> = dao.sumExpense(start, end)

    fun countInPeriod(start: Long, end: Long): Flow<Int> = dao.countInPeriod(start, end)

    // ── Operações de escrita ────────────────────────────────────────────────

    suspend fun insert(transaction: Transaction): Long = dao.insert(transaction)

    suspend fun update(transaction: Transaction) = dao.update(transaction)

    suspend fun delete(transaction: Transaction) = dao.delete(transaction)

    suspend fun getById(id: Long): Transaction? = dao.getById(id)
}
