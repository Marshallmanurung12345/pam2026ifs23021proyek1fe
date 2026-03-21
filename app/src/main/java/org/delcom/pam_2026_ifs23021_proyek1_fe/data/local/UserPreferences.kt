package org.delcom.pam_2026_ifs23021_proyek1_fe.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
        val USERNAME_KEY = stringPreferencesKey("username")
        val NAME_KEY = stringPreferencesKey("name")
        val ROLE_KEY = stringPreferencesKey("role")
        val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    val authToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    val username: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USERNAME_KEY]
    }

    val name: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[NAME_KEY]
    }

    val role: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[ROLE_KEY]
    }

    val userId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_ID_KEY]
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    suspend fun saveUserInfo(id: Int, name: String, username: String, role: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = id.toString()
            prefs[NAME_KEY] = name
            prefs[USERNAME_KEY] = username
            prefs[ROLE_KEY] = role
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}