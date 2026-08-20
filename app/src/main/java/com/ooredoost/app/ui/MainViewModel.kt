package com.ooredoost.app.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ooredoost.app.OoreDoostApp
import com.ooredoost.app.data.CycleMode
import com.ooredoost.app.data.PreferencesManager
import com.ooredoost.app.data.SessionEntity
import com.ooredoost.app.data.ServiceState
import com.ooredoost.app.service.OoreDoostService
import com.ooredoost.app.util.AirplaneModeHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as OoreDoostApp).database
    private val sessionDao = db.sessionDao()
    val preferencesManager = PreferencesManager(application)

    // ═══════════════════════════════════════════
    // Service State (shared via companion object)
    // ═══════════════════════════════════════════
    val serviceState: StateFlow<ServiceState> = OoreDoostService.serviceState

    // ═══════════════════════════════════════════
    // Session History
    // ═══════════════════════════════════════════
    val allSessions: Flow<List<SessionEntity>> = sessionDao.getAllSessions()

    val recentSessions: Flow<List<SessionEntity>> = sessionDao.getRecentSessions(20)

    val totalDataBytes: StateFlow<Long> = sessionDao.getTotalDataBytes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalCycles: StateFlow<Int> = sessionDao.getTotalCycles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalBursts: StateFlow<Int> = sessionDao.getTotalBursts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val sessionCount: StateFlow<Int> = sessionDao.getSessionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ═══════════════════════════════════════════
    // Preferences
    // ═══════════════════════════════════════════
    val manualInterval: StateFlow<Int> = preferencesManager.manualInterval
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10)

    val setupCompleted: StateFlow<Boolean> = preferencesManager.setupCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // ═══════════════════════════════════════════
    // Permission Check
    // ═══════════════════════════════════════════
    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    init {
        checkPermission()
    }

    fun checkPermission() {
        _hasPermission.value = AirplaneModeHelper.hasPermission(getApplication())
    }

    // ═══════════════════════════════════════════
    // Service Control
    // ═══════════════════════════════════════════

    fun startSmartMode(context: Context) {
        val intent = Intent(context, OoreDoostService::class.java).apply {
            action = OoreDoostService.ACTION_START_SMART
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun startManualMode(context: Context, intervalSeconds: Int) {
        val intent = Intent(context, OoreDoostService::class.java).apply {
            action = OoreDoostService.ACTION_START_MANUAL
            putExtra(OoreDoostService.EXTRA_INTERVAL, intervalSeconds)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopService(context: Context) {
        val intent = Intent(context, OoreDoostService::class.java).apply {
            action = OoreDoostService.ACTION_STOP
        }
        context.startService(intent)
    }

    // ═══════════════════════════════════════════
    // Preferences Updates
    // ═══════════════════════════════════════════

    fun setManualInterval(seconds: Int) {
        viewModelScope.launch {
            preferencesManager.setManualInterval(seconds)
        }
    }

    fun setSetupCompleted() {
        viewModelScope.launch {
            preferencesManager.setSetupCompleted(true)
        }
    }

    // ═══════════════════════════════════════════
    // Session Management
    // ═══════════════════════════════════════════

    fun clearHistory() {
        viewModelScope.launch {
            sessionDao.clearAll()
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            sessionDao.deleteSession(sessionId)
        }
    }
}
