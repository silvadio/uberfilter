package com.driveq.data

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
        val KEY_LOGGED_IN     = booleanPreferencesKey("logged_in")
        val KEY_LOGGED_EMAIL  = stringPreferencesKey("logged_email")
        val KEY_LOGGED_NAME   = stringPreferencesKey("logged_name")
        val KEY_GOOGLE_ID     = stringPreferencesKey("google_id")
        val KEY_PHOTO_URL     = stringPreferencesKey("photo_url")
        val KEY_IS_GOOGLE     = booleanPreferencesKey("is_google_login")
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

    val googleIdFlow: Flow<String?> = context.sessionDataStore.data.map { prefs ->
        prefs[KEY_GOOGLE_ID]
    }

    val photoUrlFlow: Flow<String?> = context.sessionDataStore.data.map { prefs ->
        prefs[KEY_PHOTO_URL]
    }

    val isGoogleLoginFlow: Flow<Boolean> = context.sessionDataStore.data.map { prefs ->
        prefs[KEY_IS_GOOGLE] == true
    }

    /** Login por email/senha */
    suspend fun setLoggedIn(name: String, email: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_LOGGED_IN]    = true
            prefs[KEY_LOGGED_NAME]  = name
            prefs[KEY_LOGGED_EMAIL] = email
            prefs[KEY_IS_GOOGLE]    = false
        }
    }

    /** Login via Google */
    suspend fun setLoggedInWithGoogle(
        name: String,
        email: String,
        googleId: String,
        photoUrl: String?
    ) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_LOGGED_IN]    = true
            prefs[KEY_LOGGED_NAME]  = name
            prefs[KEY_LOGGED_EMAIL] = email
            prefs[KEY_GOOGLE_ID]    = googleId
            prefs[KEY_IS_GOOGLE]    = true
            if (photoUrl != null) {
                prefs[KEY_PHOTO_URL] = photoUrl
            } else {
                prefs.remove(KEY_PHOTO_URL)
            }
        }
    }

    suspend fun logout() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(KEY_LOGGED_IN)
            prefs.remove(KEY_LOGGED_NAME)
            prefs.remove(KEY_LOGGED_EMAIL)
            prefs.remove(KEY_GOOGLE_ID)
            prefs.remove(KEY_PHOTO_URL)
            prefs.remove(KEY_IS_GOOGLE)
        }
    }
}
