package com.example.fruitrek.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "fruitrek_passport")

class PassportRepository(private val context: Context) {

    private val unlockedKey = stringSetPreferencesKey("unlocked_fruits")

    val unlockedFruits: Flow<Set<String>> = context.dataStore.data
        .map { prefs: Preferences ->
            val value: Set<String>? = prefs[unlockedKey]
            value ?: emptySet()
        }

    suspend fun unlockFruit(fruitId: String) {
        context.dataStore.edit { prefs: MutablePreferences ->
            val current: Set<String> = prefs[unlockedKey] ?: emptySet()
            val updated: Set<String> = current.toMutableSet().also { it.add(fruitId) }
            prefs[unlockedKey] = updated
        }
    }
}