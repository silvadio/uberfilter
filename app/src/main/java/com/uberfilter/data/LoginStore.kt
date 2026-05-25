package com.uberfilter.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session")

class LoginStore(private val context: Context) {

    companion object {
        val KEY_LOGGED_IN    = booleanPreferencesKey("logged_in")
        val KEY_LOGGED_EMAIL = stringPreferencesKey("logged_email")
        val KEY_LOGGED_NAME  = stringPreferencesKey("logged_name")
    }

    val isLoggedInFlow: Flow<Boolean> = context.sessionDataStore.data.map { prefs ->
        prefs[KEY_LOGGED_IN] == true
    }

    val loggedEmailFlow: Flow<String?> = context.sessionDataStore.data.map { prefs ->
        prefs[KEY_LOGGED_EMAIL]
    }

    val loggedNameFlow: Flow<String?> = context.sessionDataStore.data.map { prefs ->
        prefs[KEY_LOGGED_NAME]
    }

    suspend fun setLoggedIn(name: String, email: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_LOGGED_IN]    = true
            prefs[KEY_LOGGED_NAME]  = name
            prefs[KEY_LOGGED_EMAIL] = email
        }
    }

    suspend fun logout() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(KEY_LOGGED_IN)
            prefs.remove(KEY_LOGGED_NAME)
            prefs.remove(KEY_LOGGED_EMAIL)
        }
    }
}
