package com.uberfilter.domain

import com.uberfilter.model.*
import java.util.Calendar

object FinanceCalculator {

    fun computeSummary(
        transactions: List<Transaction>,
        periodStart: Long,
        periodEnd: Long
    ): FinancialSummary {
        val totalIncome = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val totalExpense = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        return FinancialSummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = totalIncome - totalExpense,
            transactionCount = transactions.size,
            periodStart = periodStart,
            periodEnd = periodEnd
        )
    }

    fun dailyAverageIncome(transactions: List<Transaction>, daysInPeriod: Int): Double {
        if (daysInPeriod <= 0) return 0.0
        val total = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        return total / daysInPeriod
    }

    fun dailyAverageExpense(transactions: List<Transaction>, daysInPeriod: Int): Double {
        if (daysInPeriod <= 0) return 0.0
        val total = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        return total / daysInPeriod
    }

    fun categoryBreakdown(transactions: List<Transaction>): Map<TransactionCategory, Double> {
        return transactions
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
    }

    fun projectedMonthly(totalSoFar: Double, daysElapsed: Int): Double {
        if (daysElapsed <= 0) return 0.0
        val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        return (totalSoFar / daysElapsed) * daysInMonth
    }

    // ── Helpers de período ──────────────────────────────────────────────────

    fun getDateRange(filter: PeriodFilter): DateRange {
        val cal = Calendar.getInstance()
        val endOfDay = { c: Calendar ->
            c.set(Calendar.HOUR_OF_DAY, 23)
            c.set(Calendar.MINUTE, 59)
            c.set(Calendar.SECOND, 59)
            c.set(Calendar.MILLISECOND, 999)
            c.timeInMillis
        }
        val startOfDay = { c: Calendar ->
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            c.timeInMillis
        }

        return when (filter) {
            PeriodFilter.TODAY -> {
                val start = Calendar.getInstance().apply { startOfDay(this) }
                val end = Calendar.getInstance().apply { endOfDay(this) }
                DateRange(start.timeInMillis, end.timeInMillis)
            }

            PeriodFilter.THIS_WEEK -> {
                val start = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    startOfDay(this)
                }
                val end = Calendar.getInstance().apply { endOfDay(this) }
                DateRange(start.timeInMillis, end.timeInMillis)
            }

            PeriodFilter.THIS_MONTH -> {
                val start = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    startOfDay(this)
                }
                val end = Calendar.getInstance().apply { endOfDay(this) }
                DateRange(start.timeInMillis, end.timeInMillis)
            }

            PeriodFilter.LAST_MONTH -> {
                val start = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -1)
                    set(Calendar.DAY_OF_MONTH, 1)
                    startOfDay(this)
                }
                val end = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    add(Calendar.DAY_OF_MONTH, -1)
                    endOfDay(this)
                }
                DateRange(start.timeInMillis, end.timeInMillis)
            }

            PeriodFilter.ALL -> {
                DateRange(0L, Long.MAX_VALUE)
            }
        }
    }
}
