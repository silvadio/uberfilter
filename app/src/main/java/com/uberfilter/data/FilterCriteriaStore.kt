package com.uberfilter.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.uberfilter.model.FilterCriteria
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "filter_criteria")

class FilterCriteriaStore(private val context: Context) {

    companion object {
        val MIN_TOTAL_VALUE      = doublePreferencesKey("min_total_value")
        val MIN_VALUE_PER_KM     = doublePreferencesKey("min_value_per_km")
        val MIN_VALUE_PER_HOUR   = doublePreferencesKey("min_value_per_hour")
        val MIN_PASSENGER_RATING = doublePreferencesKey("min_passenger_rating")
        val MAX_PICKUP_DIST_KM   = doublePreferencesKey("max_pickup_dist_km")
        val MAX_PICKUP_MINUTES   = intPreferencesKey("max_pickup_minutes")
        val MIN_TRIP_DIST_KM     = doublePreferencesKey("min_trip_dist_km")
        val MAX_TRIP_DURATION    = intPreferencesKey("max_trip_duration")
        val ASSISTANT_ENABLED    = booleanPreferencesKey("assistant_enabled")
    }

    val criteriaFlow: Flow<FilterCriteria> = context.dataStore.data.map { prefs ->
        val defaults = FilterCriteria()
        FilterCriteria(
            minTotalValue       = prefs[MIN_TOTAL_VALUE]      ?: defaults.minTotalValue,
            minValuePerKm       = prefs[MIN_VALUE_PER_KM]     ?: defaults.minValuePerKm,
            minValuePerHour     = prefs[MIN_VALUE_PER_HOUR]   ?: defaults.minValuePerHour,
            minPassengerRating  = prefs[MIN_PASSENGER_RATING] ?: defaults.minPassengerRating,
            maxPickupDistanceKm = prefs[MAX_PICKUP_DIST_KM]   ?: defaults.maxPickupDistanceKm,
            maxPickupMinutes    = prefs[MAX_PICKUP_MINUTES]   ?: defaults.maxPickupMinutes,
            minTripDistanceKm   = prefs[MIN_TRIP_DIST_KM]     ?: defaults.minTripDistanceKm,
            maxTripDurationMin  = prefs[MAX_TRIP_DURATION]    ?: defaults.maxTripDurationMin
        )
    }

    val assistantEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ASSISTANT_ENABLED] ?: true // habilitado por padrão
    }

    suspend fun setAssistantEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ASSISTANT_ENABLED] = enabled
        }
    }

    suspend fun save(criteria: FilterCriteria) {
        context.dataStore.edit { prefs ->
            prefs[MIN_TOTAL_VALUE]      = criteria.minTotalValue
            prefs[MIN_VALUE_PER_KM]     = criteria.minValuePerKm
            prefs[MIN_VALUE_PER_HOUR]   = criteria.minValuePerHour
            prefs[MIN_PASSENGER_RATING] = criteria.minPassengerRating
            prefs[MAX_PICKUP_DIST_KM]   = criteria.maxPickupDistanceKm
            prefs[MAX_PICKUP_MINUTES]   = criteria.maxPickupMinutes
            prefs[MIN_TRIP_DIST_KM]     = criteria.minTripDistanceKm
            prefs[MAX_TRIP_DURATION]    = criteria.maxTripDurationMin
        }
    }
}
