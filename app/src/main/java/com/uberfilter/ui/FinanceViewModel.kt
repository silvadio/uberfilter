package com.uberfilter.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uberfilter.data.FinanceDatabase
import com.uberfilter.data.FinanceRepository
import com.uberfilter.data.GoalHistoryRepository
import com.uberfilter.data.GoalStore
import com.uberfilter.domain.FinanceCalculator
import com.uberfilter.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class FinanceUiState(
    val transactions: List<Transaction> = emptyList(),
    val summary: FinancialSummary? = null,
    val selectedPeriod: PeriodFilter = PeriodFilter.THIS_MONTH,
    val isLoading: Boolean = true
)

class FinanceViewModel(app: Application) : AndroidViewModel(app) {

    private val db = FinanceDatabase.getInstance(app)
    private val repository = FinanceRepository(db.transactionDao())
    private val goalStore = GoalStore(app)
    private val historyRepo = GoalHistoryRepository(db.goalHistoryDao())

    // ── Período selecionado (filtro do ecrã) ──────────────────────────────────

    private val _period = MutableStateFlow(PeriodFilter.TODAY)
    val selectedPeriod: StateFlow<PeriodFilter> = _period.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<FinanceUiState> = _period
        .flatMapLatest { filter ->
            val range = FinanceCalculator.getDateRange(filter)
            repository.getByPeriod(range.startMillis, range.endMillis)
                .map { transactions ->
                    val summary = FinanceCalculator.computeSummary(
                        transactions, range.startMillis, range.endMillis
                    )
                    FinanceUiState(
                        transactions = transactions,
                        summary = summary,
                        selectedPeriod = filter,
                        isLoading = false
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FinanceUiState()
        )

    fun setPeriod(filter: PeriodFilter) {
        _period.value = filter
    }

    // ── Saldo líquido da semana (fixo, independente do período selecionado) ────

    @OptIn(ExperimentalCoroutinesApi::class)
    val weeklyBalance: StateFlow<Double> = run {
        val weekRange = FinanceCalculator.getDateRange(PeriodFilter.THIS_WEEK)
        combine(
            repository.sumIncome(weekRange.startMillis, weekRange.endMillis),
            repository.sumExpense(weekRange.startMillis, weekRange.endMillis)
        ) { income, expense -> income - expense }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0.0
    )

    // ── Meta ─────────────────────────────────────────────────────────────────

    val goal: StateFlow<Goal> = goalStore.goalFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Goal()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val goalProgress: StateFlow<GoalProgress?> = goal
        .flatMapLatest { g ->
            if (g.targetAmount <= 0.0) {
                flowOf(null)
            } else {
                val periodFilter = when (g.type) {
                    GoalType.WEEKLY  -> PeriodFilter.THIS_WEEK
                    GoalType.MONTHLY -> PeriodFilter.THIS_MONTH
                }
                val range = FinanceCalculator.getDateRange(periodFilter)
                combine(
                    repository.sumIncome(range.startMillis, range.endMillis),
                    repository.sumExpense(range.startMillis, range.endMillis)
                ) { income, expense ->
                    val balance = income - expense
                    val pct = ((balance / g.targetAmount) * 100).toInt().coerceAtLeast(0)
                    GoalProgress(
                        currentBalance = balance,
                        targetAmount = g.targetAmount,
                        percentage = pct,
                        isAchieved = pct >= 100,
                        remaining = (g.targetAmount - balance).coerceAtLeast(0.0)
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    // ── Histórico de metas (com snapshot automático) ─────────────────────────

    @OptIn(ExperimentalCoroutinesApi::class)
    val goalHistory: StateFlow<List<GoalHistoryEntry>> = goal
        .flatMapLatest { g ->
            if (g.targetAmount <= 0.0) {
                flowOf(emptyList())
            } else {
                // Dispara snapshot do período anterior se necessário
                flow {
                    snapshotPreviousPeriod(g)
                    emit(Unit)
                }.flatMapLatest {
                    historyRepo.getByType(g.type)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private suspend fun snapshotPreviousPeriod(goal: Goal) {
        val previousKey = previousPeriodKey(goal.type)
        val existing = historyRepo.getByKey(previousKey)
        if (existing != null) return  // já foi salvo

        val range = previousPeriodRange(goal.type)
        val income  = firstOrNull(repository.sumIncome(range.startMillis, range.endMillis)) ?: 0.0
        val expense = firstOrNull(repository.sumExpense(range.startMillis, range.endMillis)) ?: 0.0
        val balance = income - expense

        if (balance == 0.0 && income == 0.0 && expense == 0.0) return  // período vazio

        val pct = ((balance / goal.targetAmount) * 100).toInt().coerceAtLeast(0)
        historyRepo.saveSnapshot(
            GoalHistoryEntry(
                periodKey = previousKey,
                goalType = goal.type,
                periodStart = range.startMillis,
                periodEnd = range.endMillis,
                targetAmount = goal.targetAmount,
                achievedBalance = balance,
                achievedPct = pct,
                wasAchieved = pct >= 100
            )
        )
    }

    fun setGoal(type: GoalType, amount: Double) {
        viewModelScope.launch {
            goalStore.save(Goal(type = type, targetAmount = amount))
        }
    }

    fun clearGoal() {
        viewModelScope.launch {
            goalStore.clear()
        }
    }

    // ── Histórico de transações ────────────────────────────────────────────

    private val _transactionPeriod = MutableStateFlow(PeriodFilter.TODAY)
    val selectedTransactionPeriod: StateFlow<PeriodFilter> = _transactionPeriod.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val periodTransactions: StateFlow<List<Transaction>> = _transactionPeriod
        .flatMapLatest { filter ->
            val range = FinanceCalculator.getDateRange(filter)
            repository.getByPeriod(range.startMillis, range.endMillis)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun setTransactionPeriod(filter: PeriodFilter) {
        _transactionPeriod.value = filter
    }

    // ── Transações ───────────────────────────────────────────────────────────

    fun addTransaction(
        type: TransactionType,
        category: TransactionCategory,
        amount: Double,
        description: String,
        date: Long
    ) {
        viewModelScope.launch {
            repository.insert(
                Transaction(
                    type = type,
                    category = category,
                    amount = amount,
                    description = description,
                    date = date
                )
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.delete(transaction)
        }
    }

    // ── Helpers de período ───────────────────────────────────────────────────

    companion object {
        /** "2025-W21" ou "2025-05" — período ANTERIOR ao atual */
        fun previousPeriodKey(type: GoalType): String {
            val cal = Calendar.getInstance()
            return when (type) {
                GoalType.WEEKLY -> {
                    cal.add(Calendar.WEEK_OF_YEAR, -1)
                    val weekYear = cal.get(Calendar.YEAR)
                    val weekNum  = cal.get(Calendar.WEEK_OF_YEAR)
                    // Ajuste: se weekNum=1 e mês=Dez, o ano ISO pode ser o seguinte
                    "$weekYear-W${weekNum.toString().padStart(2, '0')}"
                }
                GoalType.MONTHLY -> {
                    cal.add(Calendar.MONTH, -1)
                    val year  = cal.get(Calendar.YEAR)
                    val month = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
                    "$year-$month"
                }
            }
        }

        fun previousPeriodRange(type: GoalType): DateRange {
            val cal = Calendar.getInstance()
            return when (type) {
                GoalType.WEEKLY -> {
                    // Volta para a semana passada
                    cal.add(Calendar.WEEK_OF_YEAR, -1)
                    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.add(Calendar.DAY_OF_YEAR, 6)
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    val end = cal.timeInMillis
                    DateRange(start, end)
                }
                GoalType.MONTHLY -> {
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.add(Calendar.MONTH, -1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    cal.set(Calendar.DAY_OF_MONTH, lastDay)
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    val end = cal.timeInMillis
                    DateRange(start, end)
                }
            }
        }

        private suspend fun <T> firstOrNull(flow: Flow<T>): T? {
            var value: T? = null
            flow.take(1).collect { value = it }
            return value
        }
    }
}
