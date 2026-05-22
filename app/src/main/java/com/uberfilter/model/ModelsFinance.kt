package com.uberfilter.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// ── Tipos e categorias ─────────────────────────────────────────────────────────

enum class TransactionType {
    INCOME,   // Receita
    EXPENSE   // Despesa
}

enum class TransactionCategory(val label: String) {
    // Receitas
    RIDE("Corrida"),
    TIP("Gorjeta"),
    BONUS("Bônus / Promoção"),
    REIMBURSEMENT("Reembolso"),
    OTHER_INCOME("Outra receita"),

    // Despesas
    FUEL("Combustível"),
    MAINTENANCE("Manutenção"),
    TOLL("Pedágio"),
    FOOD("Alimentação"),
    INSURANCE("Seguro"),
    FINANCING("Financiamento"),
    CLEANING("Lavagem"),
    OTHER_EXPENSE("Outra despesa");

    companion object {
        fun incomeCategories(): List<TransactionCategory> =
            entries.filter { it.name.startsWith("OTHER_") || it.name in setOf("RIDE", "TIP", "BONUS", "REIMBURSEMENT") }
                .filter { it != OTHER_EXPENSE }

        fun expenseCategories(): List<TransactionCategory> =
            listOf(FUEL, MAINTENANCE, TOLL, FOOD, INSURANCE, FINANCING, CLEANING, OTHER_EXPENSE)
    }
}

// ── Entidade Room ──────────────────────────────────────────────────────────────

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val category: TransactionCategory,
    val amount: Double,         // sempre positivo
    val description: String,
    val date: Long,             // epoch millis
    val createdAt: Long = System.currentTimeMillis()
)

// ── Resumo financeiro ──────────────────────────────────────────────────────────

data class FinancialSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val transactionCount: Int,
    val periodStart: Long,
    val periodEnd: Long
)

// ── Período ────────────────────────────────────────────────────────────────────

enum class PeriodFilter(val label: String) {
    TODAY("Hoje"),
    THIS_WEEK("Esta semana"),
    THIS_MONTH("Este mês"),
    LAST_MONTH("Mês passado"),
    ALL("Tudo")
}

data class DateRange(
    val startMillis: Long,
    val endMillis: Long
)

// ── Metas ───────────────────────────────────────────────────────────────────────

enum class GoalType(val label: String) {
    WEEKLY("Semana"),
    MONTHLY("Mês")
}

data class Goal(
    val type: GoalType = GoalType.WEEKLY,
    val targetAmount: Double = 0.0       // 0 = sem meta
)

data class GoalProgress(
    val currentBalance: Double,           // receitas − despesas no período da meta
    val targetAmount: Double,
    val percentage: Int,                  // 0..100+
    val isAchieved: Boolean,              // percentage >= 100
    val remaining: Double                 // quanto falta (0 se batido)
)

// ── Histórico de metas ──────────────────────────────────────────────────────────

@Entity(tableName = "goal_history")
data class GoalHistoryEntry(
    @PrimaryKey
    val periodKey: String,               // "2025-W21" ou "2025-05"
    val goalType: GoalType,
    val periodStart: Long,
    val periodEnd: Long,
    val targetAmount: Double,
    val achievedBalance: Double,
    val achievedPct: Int,
    val wasAchieved: Boolean,
    val savedAt: Long = System.currentTimeMillis()
)
