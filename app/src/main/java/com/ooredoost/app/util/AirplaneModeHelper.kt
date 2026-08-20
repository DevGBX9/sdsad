package com.ooredoost.app.util

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

/**
 * Helper class for toggling airplane mode programmatically.
 * Requires WRITE_SECURE_SETTINGS permission (granted via ADB).
 */
object AirplaneModeHelper {

    private const val TAG = "AirplaneModeHelper"

    /**
     * Check if airplane mode is currently enabled.
     */
    fun isAirplaneModeOn(context: Context): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON,
            0
        ) != 0
    }

    /**
     * Enable airplane mode.
     * @return true if successful, false if permission denied.
     */
    fun enableAirplaneMode(context: Context): Boolean {
        return try {
            Settings.Global.putInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON,
                1
            )
            // Broadcast the change so the system responds
            val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
                putExtra("state", true)
            }
            context.sendBroadcast(intent)
            Log.d(TAG, "Airplane mode ENABLED")
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied: WRITE_SECURE_SETTINGS not granted", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable airplane mode", e)
            false
        }
    }

    /**
     * Disable airplane mode.
     * @return true if successful, false if permission denied.
     */
    fun disableAirplaneMode(context: Context): Boolean {
        return try {
            Settings.Global.putInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON,
                0
            )
            // Broadcast the change so the system responds
            val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
                putExtra("state", false)
            }
            context.sendBroadcast(intent)
            Log.d(TAG, "Airplane mode DISABLED")
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied: WRITE_SECURE_SETTINGS not granted", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable airplane mode", e)
            false
        }
    }

    /**
     * Toggle airplane mode (on → off, off → on).
     * @return true if successful.
     */
    fun toggleAirplaneMode(context: Context): Boolean {
        return if (isAirplaneModeOn(context)) {
            disableAirplaneMode(context)
        } else {
            enableAirplaneMode(context)
        }
    }

    /**
     * Check if the WRITE_SECURE_SETTINGS permission is granted.
     */
    fun hasPermission(context: Context): Boolean {
        return try {
            // Try a read operation first
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON,
                0
            )
            // Try a write test - write the current value back
            val currentValue = Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON,
                0
            )
            Settings.Global.putInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON,
                currentValue
            )
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
