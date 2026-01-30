package com.opticalshop.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val THEME_KEY = booleanPreferencesKey("theme_mode")

    val isDarkTheme: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_KEY]
        }

    suspend fun setThemeMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = isDark
        }
    }
}
