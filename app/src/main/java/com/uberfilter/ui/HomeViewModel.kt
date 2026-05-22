package com.uberfilter.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uberfilter.data.FinanceDatabase
import com.uberfilter.data.RideHistoryRepository
import com.uberfilter.domain.FinanceCalculator
import com.uberfilter.domain.RideStatsCalculator
import com.uberfilter.model.PeriodFilter
import com.uberfilter.model.RideStats
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val stats: RideStats? = null,
    val selectedPeriod: PeriodFilter = PeriodFilter.TODAY,
    val isLoading: Boolean = true
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = RideHistoryRepository(
        FinanceDatabase.getInstance(app).rideHistoryDao()
    )

    private val _period = MutableStateFlow(PeriodFilter.TODAY)
    val selectedPeriod: StateFlow<PeriodFilter> = _period.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = _period
        .flatMapLatest { filter ->
            val range = FinanceCalculator.getDateRange(filter)
            repository.getByPeriod(range.startMillis, range.endMillis)
                .map { records ->
                    HomeUiState(
                        stats = RideStatsCalculator.compute(records),
                        selectedPeriod = filter,
                        isLoading = false
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    fun setPeriod(filter: PeriodFilter) {
        _period.value = filter
    }
}
