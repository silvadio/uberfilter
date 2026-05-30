package com.driveq.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.driveq.data.FilterCriteriaStore
import com.driveq.data.UndesiredLocationStore
import com.driveq.model.FilterCriteria
import com.driveq.model.GeofenceEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val criteriaStore = FilterCriteriaStore(app)
    private val locationStore = UndesiredLocationStore(app)

    val criteria = criteriaStore.criteriaFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FilterCriteria()
    )

    val assistantEnabled = criteriaStore.assistantEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true
    )

    val blockedLocations = locationStore.textLocationsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val geofences = locationStore.geofencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun save(criteria: FilterCriteria) {
        viewModelScope.launch { criteriaStore.save(criteria) }
    }

    fun setAssistantEnabled(enabled: Boolean) {
        viewModelScope.launch { criteriaStore.setAssistantEnabled(enabled) }
    }

    fun addBlockedLocation(name: String) {
        viewModelScope.launch { locationStore.addTextLocation(name) }
    }

    fun removeBlockedLocation(name: String) {
        viewModelScope.launch { locationStore.removeTextLocation(name) }
    }

    fun addGeofence(entry: GeofenceEntry) {
        viewModelScope.launch { locationStore.addGeofence(entry) }
    }

    fun removeGeofence(entry: GeofenceEntry) {
        viewModelScope.launch { locationStore.removeGeofence(entry) }
    }

    fun updateGeofence(updated: GeofenceEntry) {
        viewModelScope.launch { locationStore.updateGeofence(updated) }
    }
}
