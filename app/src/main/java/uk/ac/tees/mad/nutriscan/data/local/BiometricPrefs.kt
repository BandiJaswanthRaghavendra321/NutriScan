package uk.ac.tees.mad.nutriscan.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("nutriscan_prefs")

object BiometricPrefs {
    private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")

    suspend fun setEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun isEnabled(context: Context): Boolean {
        return context.dataStore.data.map { it[BIOMETRIC_ENABLED] ?: false }.first()
    }
}
