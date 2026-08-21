package com.ooredoost.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ooredoost.app.MainActivity
import com.ooredoost.app.OoreDoostApp
import com.ooredoost.app.R
import com.ooredoost.app.data.CycleMode
import com.ooredoost.app.data.ServiceState
import com.ooredoost.app.data.ServiceStatus
import com.ooredoost.app.data.SessionEntity
import com.ooredoost.app.util.AirplaneModeHelper
import com.ooredoost.app.util.DataFormatter
import com.ooredoost.app.util.TrafficTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Main foreground service that handles airplane mode cycling and traffic monitoring.
 * Supports two modes:
 * - Smart Mode: Automatically detects when free data burst ends and re-cycles
 * - Manual Mode: Cycles at a fixed user-defined interval
 */
class OoreDoostService : Service() {

    private val TAG = "OoreDoostService"
    private val NOTIFICATION_ID = 1001

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var cycleJob: Job? = null
    private val trafficTracker = TrafficTracker()
    private var wakeLock: android.os.PowerManager.WakeLock? = null

    companion object {
        const val ACTION_START_SMART = "com.ooredoost.START_SMART"
        const val ACTION_START_MANUAL = "com.ooredoost.START_MANUAL"
        const val ACTION_STOP = "com.ooredoost.STOP"
        const val EXTRA_INTERVAL = "extra_interval"

        private val _serviceState = MutableStateFlow(ServiceState())
        val serviceState: StateFlow<ServiceState> = _serviceState.asStateFlow()

        fun isRunning(): Boolean = _serviceState.value.isRunning
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        acquireWakeLock()
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock == null) {
                val powerManager = getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
                wakeLock = powerManager.newWakeLock(
                    android.os.PowerManager.PARTIAL_WAKE_LOCK,
                    "OoreDoost::ServiceWakeLock"
                ).apply {
                    setReferenceCounted(false)
                    acquire(24 * 60 * 60 * 1000L) // 24 hours max safety timeout
                }
                Log.d(TAG, "WakeLock acquired for background execution")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire WakeLock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
                Log.d(TAG, "WakeLock released")
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release WakeLock", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        acquireWakeLock()
        when (intent?.action) {
            ACTION_START_SMART -> {
                startForeground(NOTIFICATION_ID, createNotification("جاري التحضير..."))
                startSmartMode()
            }
            ACTION_START_MANUAL -> {
                val interval = intent.getIntExtra(EXTRA_INTERVAL, 10)
                startForeground(NOTIFICATION_ID, createNotification("جاري التحضير..."))
                startManualMode(interval)
            }
            ACTION_STOP -> {
                stopCycling()
            }
            else -> {
                // If service was restarted by the system while running
                if (_serviceState.value.isRunning) {
                    startForeground(NOTIFICATION_ID, createNotification("استئناف الخدمة..."))
                    if (_serviceState.value.mode == CycleMode.SMART) {
                        startSmartMode()
                    } else {
                        startManualMode(_serviceState.value.manualIntervalSeconds)
                    }
                } else {
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    // ═══════════════════════════════════════════
    // Smart Mode
    // ═══════════════════════════════════════════

    private fun startSmartMode() {
        // Check permission first
        if (!AirplaneModeHelper.hasPermission(this)) {
            updateState {
                it.copy(
                    status = ServiceStatus.PERMISSION_ERROR,
                    statusMessage = "صلاحية WRITE_SECURE_SETTINGS غير ممنوحة"
                )
            }
            stopSelf()
            return
        }

        cycleJob?.cancel()
        trafficTracker.resetSession()

        updateState {
            ServiceState(
                isRunning = true,
                mode = CycleMode.SMART,
                status = ServiceStatus.ACTIVATING_AIRPLANE,
                sessionStartTime = System.currentTimeMillis(),
                statusMessage = "بدء الوضع الذكي"
            )
        }

        cycleJob = serviceScope.launch {
            Log.d(TAG, "🚀 Smart mode started")
            var cycleCount = 0

            while (isActive) {
                cycleCount++
                Log.d(TAG, "═══ Cycle #$cycleCount ═══")

                // Step 1: Enable airplane mode
                updateState {
                    it.copy(
                        status = ServiceStatus.ACTIVATING_AIRPLANE,
                        cycleCount = cycleCount,
                        statusMessage = "تفعيل وضع الطيران..."
                    )
                }
                updateNotification("✈️ وضع الطيران مفعل | دورة #$cycleCount")

                val enabled = AirplaneModeHelper.enableAirplaneMode(this@OoreDoostService)
                if (!enabled) {
                    updateState {
                        it.copy(
                            status = ServiceStatus.PERMISSION_ERROR,
                            statusMessage = "فشل تفعيل وضع الطيران"
                        )
                    }
                    break
                }

                // Step 2: Wait with airplane mode ON (1 second)
                updateState {
                    it.copy(
                        status = ServiceStatus.AIRPLANE_ON,
                        statusMessage = "وضع الطيران مفعل..."
                    )
                }
                delay(1000)

                // Step 3: Disable airplane mode
                updateState {
                    it.copy(
                        status = ServiceStatus.DEACTIVATING_AIRPLANE,
                        statusMessage = "إلغاء وضع الطيران..."
                    )
                }
                AirplaneModeHelper.disableAirplaneMode(this@OoreDoostService)

                // Step 4: Wait for mobile data to reconnect
                updateState {
                    it.copy(
                        status = ServiceStatus.WAITING_RECONNECT,
                        statusMessage = "انتظار إعادة الاتصال..."
                    )
                }
                updateNotification("📡 انتظار الاتصال... | دورة #$cycleCount")
                delay(2000)

                // Step 5: Start monitoring traffic
                trafficTracker.startMonitoring()
                updateState {
                    it.copy(
                        status = ServiceStatus.MONITORING,
                        statusMessage = "مراقبة تدفق البيانات..."
                    )
                }
                updateNotification("🔍 مراقبة البيانات... | دورة #$cycleCount")

                // Step 6: Monitor loop - check every 100ms
                var noDataCycles = 0
                val maxWaitWithoutData = 80 // 80 * 100ms = 8 seconds max wait for initial data
                var gotDataThisCycle = false

                while (isActive) {
                    val snapshot = trafficTracker.snapshot(dataTimeoutMs = 2500)

                    if (snapshot.isDataFlowing) {
                        gotDataThisCycle = true
                        noDataCycles = 0

                        updateState {
                            it.copy(
                                status = ServiceStatus.DATA_FLOWING,
                                isDataFlowing = true,
                                currentBurstBytes = snapshot.currentBurstBytes,
                                totalSessionBytes = snapshot.totalSessionBytes,
                                burstCount = snapshot.burstCount,
                                dataFlowStartTime = trafficTracker.burstStartTime,
                                statusMessage = "📡 بيانات متدفقة: ${DataFormatter.formatBytes(snapshot.currentBurstBytes)}"
                            )
                        }
                        updateNotification("📡 ${DataFormatter.formatBytes(snapshot.totalSessionBytes)} | دورة #$cycleCount")
                    } else if (snapshot.burstJustEnded) {
                        // Burst ended - time for a new cycle
                        updateState {
                            it.copy(
                                status = ServiceStatus.DATA_STOPPED,
                                isDataFlowing = false,
                                dataFlowEndTime = System.currentTimeMillis(),
                                statusMessage = "توقف التدفق. بدء دورة جديدة..."
                            )
                        }
                        Log.d(TAG, "Burst ended. Starting new cycle...")
                        delay(300) // Brief pause before next cycle
                        break
                    } else if (!gotDataThisCycle) {
                        noDataCycles++
                        if (noDataCycles >= maxWaitWithoutData) {
                            // No data received at all, retry cycle
                            Log.d(TAG, "No data received after ${maxWaitWithoutData * 100}ms, retrying...")
                            updateState {
                                it.copy(
                                    status = ServiceStatus.DATA_STOPPED,
                                    statusMessage = "لم تصل بيانات. إعادة المحاولة..."
                                )
                            }
                            delay(200)
                            break
                        }
                    }

                    delay(100) // 100ms polling interval for precision
                }
            }

            // Service stopped or loop ended
            saveSession()
        }
    }

    // ═══════════════════════════════════════════
    // Manual Mode
    // ═══════════════════════════════════════════

    private fun startManualMode(intervalSeconds: Int) {
        if (!AirplaneModeHelper.hasPermission(this)) {
            updateState {
                it.copy(
                    status = ServiceStatus.PERMISSION_ERROR,
                    statusMessage = "صلاحية WRITE_SECURE_SETTINGS غير ممنوحة"
                )
            }
            stopSelf()
            return
        }

        cycleJob?.cancel()
        trafficTracker.resetSession()

        updateState {
            ServiceState(
                isRunning = true,
                mode = CycleMode.MANUAL,
                status = ServiceStatus.ACTIVATING_AIRPLANE,
                sessionStartTime = System.currentTimeMillis(),
                manualIntervalSeconds = intervalSeconds,
                statusMessage = "بدء الوضع اليدوي (كل ${intervalSeconds} ثانية)"
            )
        }

        cycleJob = serviceScope.launch {
            Log.d(TAG, "🚀 Manual mode started (interval: ${intervalSeconds}s)")
            var cycleCount = 0

            while (isActive) {
                cycleCount++

                // Step 1: Enable airplane mode
                updateState {
                    it.copy(
                        status = ServiceStatus.ACTIVATING_AIRPLANE,
                        cycleCount = cycleCount,
                        statusMessage = "تفعيل وضع الطيران..."
                    )
                }
                updateNotification("✈️ دورة #$cycleCount | كل ${intervalSeconds}ث")
                AirplaneModeHelper.enableAirplaneMode(this@OoreDoostService)

                // Step 2: Wait 1 second
                updateState {
                    it.copy(status = ServiceStatus.AIRPLANE_ON, statusMessage = "وضع الطيران مفعل...")
                }
                delay(1000)

                // Step 3: Disable airplane mode
                updateState {
                    it.copy(status = ServiceStatus.DEACTIVATING_AIRPLANE, statusMessage = "إلغاء وضع الطيران...")
                }
                AirplaneModeHelper.disableAirplaneMode(this@OoreDoostService)

                // Step 4: Monitor during interval
                trafficTracker.startMonitoring()
                updateState {
                    it.copy(
                        status = ServiceStatus.WAITING_INTERVAL,
                        statusMessage = "انتظار ${intervalSeconds} ثانية..."
                    )
                }
                updateNotification("⏱️ انتظار ${intervalSeconds}ث | ${DataFormatter.formatBytes(trafficTracker.sessionBytes)}")

                // Monitor traffic during the wait interval
                val intervalMs = intervalSeconds * 1000L
                val startWait = System.currentTimeMillis()

                while (isActive && (System.currentTimeMillis() - startWait) < intervalMs) {
                    val snapshot = trafficTracker.snapshot()

                    updateState {
                        it.copy(
                            totalSessionBytes = snapshot.totalSessionBytes,
                            currentBurstBytes = snapshot.currentBurstBytes,
                            isDataFlowing = snapshot.isDataFlowing,
                            burstCount = snapshot.burstCount,
                            lastCycleTimeMs = System.currentTimeMillis()
                        )
                    }

                    delay(200)
                }
            }

            saveSession()
        }
    }

    // ═══════════════════════════════════════════
    // Service Control
    // ═══════════════════════════════════════════

    private fun stopCycling() {
        Log.d(TAG, "Stopping service...")
        cycleJob?.cancel()

        // Make sure airplane mode is OFF when stopping
        if (AirplaneModeHelper.isAirplaneModeOn(this)) {
            AirplaneModeHelper.disableAirplaneMode(this)
        }

        // Save session before stopping
        serviceScope.launch {
            saveSession()
            updateState { ServiceState() } // Reset to idle
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private suspend fun saveSession() {
        val state = _serviceState.value
        if (state.sessionStartTime > 0 && state.totalSessionBytes > 0) {
            val session = SessionEntity(
                startTime = state.sessionStartTime,
                endTime = System.currentTimeMillis(),
                dataBytes = state.totalSessionBytes,
                cycleCount = state.cycleCount,
                burstCount = state.burstCount,
                mode = if (state.mode == CycleMode.SMART) "smart" else "manual",
                manualInterval = state.manualIntervalSeconds
            )
            try {
                val db = (application as OoreDoostApp).database
                db.sessionDao().insert(session)
                Log.d(TAG, "Session saved: ${DataFormatter.formatBytes(session.dataBytes)}, ${session.cycleCount} cycles")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save session", e)
            }
        }
    }

    // ═══════════════════════════════════════════
    // Notification
    // ═══════════════════════════════════════════

    private fun createNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, OoreDoostService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, OoreDoostApp.CHANNEL_SERVICE)
            .setContentTitle("OoreDoost")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_notification, "Stop", stopIntent)
            .setOngoing(true)
            .setSilent(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(text: String) {
        try {
            val notification = createNotification(text)
            val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification", e)
        }
    }

    // ═══════════════════════════════════════════
    // State Management
    // ═══════════════════════════════════════════

    private fun updateState(transform: (ServiceState) -> ServiceState) {
        _serviceState.value = transform(_serviceState.value)
    }

    /**
     * Called when the user swipes the app away from recents.
     * Re-starts the service to ensure it continues running in background.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "Task removed (app swiped away) - restarting service")
        if (_serviceState.value.isRunning) {
            val restartIntent = Intent(this, OoreDoostService::class.java).apply {
                action = if (_serviceState.value.mode == CycleMode.SMART) {
                    ACTION_START_SMART
                } else {
                    ACTION_START_MANUAL
                }
                putExtra(EXTRA_INTERVAL, _serviceState.value.manualIntervalSeconds)
            }
            startService(restartIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cycleJob?.cancel()
        releaseWakeLock()
        // Ensure airplane mode is OFF
        if (AirplaneModeHelper.isAirplaneModeOn(this)) {
            AirplaneModeHelper.disableAirplaneMode(this)
        }
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")
    }
}

