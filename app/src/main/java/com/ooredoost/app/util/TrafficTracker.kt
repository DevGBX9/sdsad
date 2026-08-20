package com.ooredoost.app.util

import android.net.TrafficStats
import android.util.Log

/**
 * High-precision traffic tracker that monitors mobile data usage.
 * Uses TrafficStats.getMobileRxBytes() to detect data bursts from Ooredoo.
 *
 * The tracker takes snapshots at configurable intervals and detects:
 * - When a data burst starts (first bytes received after airplane mode cycle)
 * - When a data burst ends (no new bytes for a timeout period)
 * - Total bytes received per burst and per session
 */
class TrafficTracker {

    private val TAG = "TrafficTracker"

    /** Baseline RX bytes when monitoring started */
    private var baselineRxBytes: Long = 0

    /** Last recorded RX bytes for delta calculation */
    private var lastRxBytes: Long = 0

    /** Total bytes received in current session */
    var sessionBytes: Long = 0
        private set

    /** Bytes received in current burst */
    var currentBurstBytes: Long = 0
        private set

    /** Whether data is currently flowing */
    var isDataFlowing: Boolean = false
        private set

    /** Timestamp when current burst started */
    var burstStartTime: Long = 0
        private set

    /** Timestamp of last received data */
    var lastDataTime: Long = 0
        private set

    /** Number of bursts detected in this session */
    var burstCount: Int = 0
        private set

    /** Total bytes received across all cycles in this session */
    var totalCycleBytes: Long = 0
        private set

    /**
     * Reset the tracker for a new monitoring cycle.
     * Called after airplane mode is turned off.
     */
    fun startMonitoring() {
        val currentRx = getMobileRxBytes()
        baselineRxBytes = currentRx
        lastRxBytes = currentRx
        currentBurstBytes = 0
        isDataFlowing = false
        burstStartTime = 0
        lastDataTime = 0
        Log.d(TAG, "Monitoring started. Baseline: $baselineRxBytes bytes")
    }

    /**
     * Reset the tracker for a completely new session.
     * Called when user starts the service.
     */
    fun resetSession() {
        sessionBytes = 0
        currentBurstBytes = 0
        totalCycleBytes = 0
        burstCount = 0
        isDataFlowing = false
        baselineRxBytes = 0
        lastRxBytes = 0
        burstStartTime = 0
        lastDataTime = 0
        Log.d(TAG, "Session reset")
    }

    /**
     * Take a snapshot and check for data changes.
     * Should be called every ~100ms for high precision.
     *
     * @param dataTimeoutMs How long without data before considering burst ended
     * @return TrafficSnapshot with current state
     */
    fun snapshot(dataTimeoutMs: Long = 3000): TrafficSnapshot {
        val currentRx = getMobileRxBytes()
        val now = System.currentTimeMillis()
        val delta = currentRx - lastRxBytes

        if (delta > 0) {
            // New data received!
            if (!isDataFlowing) {
                // Burst just started
                isDataFlowing = true
                burstStartTime = now
                burstCount++
                currentBurstBytes = 0
                Log.d(TAG, "📡 Data burst #$burstCount STARTED")
            }

            currentBurstBytes += delta
            sessionBytes += delta
            totalCycleBytes += delta
            lastDataTime = now
            lastRxBytes = currentRx

            Log.d(TAG, "📊 Data flowing: +$delta bytes (burst: $currentBurstBytes, session: $sessionBytes)")
        } else if (isDataFlowing && lastDataTime > 0) {
            // No new data - check if burst has ended
            val timeSinceLastData = now - lastDataTime
            if (timeSinceLastData >= dataTimeoutMs) {
                // Burst ended
                isDataFlowing = false
                val burstDuration = lastDataTime - burstStartTime
                Log.d(TAG, "🛑 Data burst ENDED. Duration: ${burstDuration}ms, Bytes: $currentBurstBytes")

                return TrafficSnapshot(
                    totalSessionBytes = sessionBytes,
                    currentBurstBytes = currentBurstBytes,
                    isDataFlowing = false,
                    burstJustEnded = true,
                    burstDurationMs = burstDuration,
                    deltaBytes = 0,
                    burstCount = burstCount
                )
            }
        }

        lastRxBytes = currentRx

        return TrafficSnapshot(
            totalSessionBytes = sessionBytes,
            currentBurstBytes = currentBurstBytes,
            isDataFlowing = isDataFlowing,
            burstJustEnded = false,
            burstDurationMs = if (isDataFlowing && burstStartTime > 0) now - burstStartTime else 0,
            deltaBytes = delta,
            burstCount = burstCount
        )
    }

    /**
     * Get current mobile RX bytes from TrafficStats.
     * Returns 0 if stats are not available.
     */
    private fun getMobileRxBytes(): Long {
        val bytes = TrafficStats.getMobileRxBytes()
        return if (bytes == TrafficStats.UNSUPPORTED.toLong()) 0L else bytes
    }

    /**
     * Get current mobile TX bytes (for info display).
     */
    fun getMobileTxBytes(): Long {
        val bytes = TrafficStats.getMobileTxBytes()
        return if (bytes == TrafficStats.UNSUPPORTED.toLong()) 0L else bytes
    }
}

/**
 * Represents a single snapshot of traffic state.
 */
data class TrafficSnapshot(
    val totalSessionBytes: Long,
    val currentBurstBytes: Long,
    val isDataFlowing: Boolean,
    val burstJustEnded: Boolean,
    val burstDurationMs: Long,
    val deltaBytes: Long,
    val burstCount: Int
)
