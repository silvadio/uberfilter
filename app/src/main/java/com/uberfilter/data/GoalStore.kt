package com.uberfilter.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.uberfilter.model.Goal
import com.uberfilter.model.GoalType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.goalDataStore by preferencesDataStore(name = "goal_settings")

class GoalStore(private val context: Context) {

    companion object {
        val GOAL_TYPE   = stringPreferencesKey("goal_type")
        val TARGET_AMOUNT = doublePreferencesKey("target_amount")
    }

    val goalFlow: Flow<Goal> = context.goalDataStore.data.map { prefs ->
        val typeStr = prefs[GOAL_TYPE] ?: GoalType.WEEKLY.name
        val type = try { GoalType.valueOf(typeStr) } catch (_: Exception) { GoalType.WEEKLY }
        val amount = prefs[TARGET_AMOUNT] ?: 0.0
        Goal(type = type, targetAmount = amount)
    }

    suspend fun save(goal: Goal) {
        context.goalDataStore.edit { prefs ->
            prefs[GOAL_TYPE] = goal.type.name
            prefs[TARGET_AMOUNT] = goal.targetAmount
        }
    }

    suspend fun clear() {
        context.goalDataStore.edit { prefs ->
            prefs.remove(GOAL_TYPE)
            prefs.remove(TARGET_AMOUNT)
        }
    }
}
