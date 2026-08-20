package com.ooredoost.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ooredoost_prefs")

class PreferencesManager(private val context: Context) {

    // ═══════════════════════════════════════════
    // Keys
    // ═══════════════════════════════════════════
    companion object {
        val KEY_MODE = stringPreferencesKey("cycle_mode")
        val KEY_MANUAL_INTERVAL = intPreferencesKey("manual_interval")
        val KEY_AIRPLANE_ON_DURATION = intPreferencesKey("airplane_on_duration")
        val KEY_MONITORING_SENSITIVITY = intPreferencesKey("monitoring_sensitivity")
        val KEY_DATA_TIMEOUT = intPreferencesKey("data_timeout")
        val KEY_AUTO_START = booleanPreferencesKey("auto_start")
        val KEY_SHOW_NOTIFICATION_DETAILS = booleanPreferencesKey("show_notification_details")
        val KEY_SETUP_COMPLETED = booleanPreferencesKey("setup_completed")
        val KEY_LANGUAGE = stringPreferencesKey("language")
    }

    // ═══════════════════════════════════════════
    // Flows
    // ═══════════════════════════════════════════

    val cycleMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_MODE] ?: "smart"
    }

    val manualInterval: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_MANUAL_INTERVAL] ?: 10
    }

    val airplaneOnDuration: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_AIRPLANE_ON_DURATION] ?: 1000
    }

    val monitoringSensitivity: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_MONITORING_SENSITIVITY] ?: 100
    }

    val dataTimeout: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_DATA_TIMEOUT] ?: 3000
    }

    val autoStart: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_START] ?: false
    }

    val showNotificationDetails: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_NOTIFICATION_DETAILS] ?: true
    }

    val setupCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SETUP_COMPLETED] ?: false
    }

    val language: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: "ar"
    }

    // ═══════════════════════════════════════════
    // Setters
    // ═══════════════════════════════════════════

    suspend fun setCycleMode(mode: String) {
        context.dataStore.edit { it[KEY_MODE] = mode }
    }

    suspend fun setManualInterval(seconds: Int) {
        context.dataStore.edit { it[KEY_MANUAL_INTERVAL] = seconds }
    }

    suspend fun setAirplaneOnDuration(millis: Int) {
        context.dataStore.edit { it[KEY_AIRPLANE_ON_DURATION] = millis }
    }

    suspend fun setMonitoringSensitivity(millis: Int) {
        context.dataStore.edit { it[KEY_MONITORING_SENSITIVITY] = millis }
    }

    suspend fun setDataTimeout(millis: Int) {
        context.dataStore.edit { it[KEY_DATA_TIMEOUT] = millis }
    }

    suspend fun setAutoStart(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_START] = enabled }
    }

    suspend fun setShowNotificationDetails(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_NOTIFICATION_DETAILS] = enabled }
    }

    suspend fun setSetupCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_SETUP_COMPLETED] = completed }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = lang }
    }
}
