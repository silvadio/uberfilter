package com.uberfilter.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uberfilter.data.FilterCriteriaStore
import com.uberfilter.model.FilterCriteria
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val store = FilterCriteriaStore(app)

    val criteria = store.criteriaFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FilterCriteria()
    )

    fun save(criteria: FilterCriteria) {
        viewModelScope.launch { store.save(criteria) }
    }
}
