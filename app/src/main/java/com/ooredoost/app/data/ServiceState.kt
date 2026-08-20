package com.ooredoost.app.data

/**
 * Represents the current operating mode of the service.
 */
enum class CycleMode {
    SMART,
    MANUAL
}

/**
 * Represents the current status of the airplane mode cycling service.
 */
enum class ServiceStatus {
    /** Service is not running */
    IDLE,
    /** Turning airplane mode ON */
    ACTIVATING_AIRPLANE,
    /** Airplane mode is ON, waiting before turning off */
    AIRPLANE_ON,
    /** Turning airplane mode OFF */
    DEACTIVATING_AIRPLANE,
    /** Waiting for mobile data to reconnect */
    WAITING_RECONNECT,
    /** Monitoring data traffic for incoming burst */
    MONITORING,
    /** Free data burst detected and flowing */
    DATA_FLOWING,
    /** Data burst ended, preparing next cycle */
    DATA_STOPPED,
    /** Waiting manual interval before next cycle */
    WAITING_INTERVAL,
    /** Permission not granted */
    PERMISSION_ERROR
}

/**
 * Holds the complete state of the OoreDoost service.
 * Shared between the service and UI via StateFlow.
 */
data class ServiceState(
    val isRunning: Boolean = false,
    val mode: CycleMode = CycleMode.SMART,
    val status: ServiceStatus = ServiceStatus.IDLE,
    val cycleCount: Int = 0,
    val totalSessionBytes: Long = 0,
    val currentBurstBytes: Long = 0,
    val isDataFlowing: Boolean = false,
    val lastCycleTimeMs: Long = 0,
    val sessionStartTime: Long = 0,
    val dataFlowStartTime: Long = 0,
    val dataFlowEndTime: Long = 0,
    val burstCount: Int = 0,
    val manualIntervalSeconds: Int = 10,
    val statusMessage: String = ""
)
