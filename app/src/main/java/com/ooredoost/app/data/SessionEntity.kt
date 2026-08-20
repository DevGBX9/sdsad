package com.ooredoost.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a single usage session.
 * A session starts when the user taps Start and ends when they tap Stop.
 */
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Timestamp when the session started (millis) */
    val startTime: Long,

    /** Timestamp when the session ended (millis) */
    val endTime: Long,

    /** Total data received during this session (bytes) */
    val dataBytes: Long,

    /** Number of airplane mode cycles in this session */
    val cycleCount: Int,

    /** Number of successful data bursts received */
    val burstCount: Int,

    /** Mode used: "smart" or "manual" */
    val mode: String,

    /** Manual interval in seconds (only for manual mode) */
    val manualInterval: Int = 0
)
