package com.ooredoost.app.util

import android.content.Context
import android.provider.Settings
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Controls airplane mode using shell commands for FULL system-level toggle.
 * Uses "cmd connectivity airplane-mode enable/disable" which is the SAME mechanism
 * that the Quick Settings tile uses internally - this ensures complete radio shutdown
 * and SIM reconnection, exactly like toggling manually from notifications.
 */
object AirplaneModeHelper {

    private const val TAG = "AirplaneModeHelper"

    /**
     * Check if airplane mode is currently enabled.
     */
    fun isAirplaneModeOn(context: Context): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON,
                0
            ) != 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Enable airplane mode with FULL radio shutdown.
     * Uses shell command for complete system-level toggle.
     */
    fun enableAirplaneMode(context: Context): Boolean {
        Log.d(TAG, "=== ENABLING AIRPLANE MODE (Full System Toggle) ===")

        // Method 1: cmd connectivity (same as Quick Settings tile)
        val cmdResult = executeShell("cmd connectivity airplane-mode enable")
        Log.d(TAG, "cmd connectivity enable result: $cmdResult")

        // Verify it worked
        if (isAirplaneModeOn(context)) {
            Log.d(TAG, "SUCCESS: Airplane mode is ON (verified via Settings.Global)")
            return true
        }

        // Method 2: Fallback - settings put + su cmd (for rooted devices)
        Log.d(TAG, "Trying fallback methods...")
        executeShell("settings put global airplane_mode_on 1")
        executeShell("su -c 'cmd connectivity airplane-mode enable'")
        executeShell("svc data disable")
        executeShell("svc wifi disable")

        // Method 3: Settings API
        try {
            Settings.Global.putInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON,
                1
            )
        } catch (e: Exception) {
            Log.e(TAG, "Settings.Global.putInt failed", e)
        }

        // Final verification
        val finalState = isAirplaneModeOn(context)
        Log.d(TAG, "Final airplane mode state: $finalState")
        return finalState
    }

    /**
     * Disable airplane mode with FULL radio reconnection.
     * Uses shell command for complete system-level toggle.
     */
    fun disableAirplaneMode(context: Context): Boolean {
        Log.d(TAG, "=== DISABLING AIRPLANE MODE (Full System Toggle) ===")

        // Method 1: cmd connectivity (same as Quick Settings tile)
        val cmdResult = executeShell("cmd connectivity airplane-mode disable")
        Log.d(TAG, "cmd connectivity disable result: $cmdResult")

        // Verify it worked
        if (!isAirplaneModeOn(context)) {
            Log.d(TAG, "SUCCESS: Airplane mode is OFF (verified via Settings.Global)")
            return true
        }

        // Method 2: Fallback
        Log.d(TAG, "Trying fallback methods...")
        executeShell("settings put global airplane_mode_on 0")
        executeShell("su -c 'cmd connectivity airplane-mode disable'")
        executeShell("svc data enable")
        executeShell("svc wifi enable")

        // Method 3: Settings API
        try {
            Settings.Global.putInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON,
                0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Settings.Global.putInt failed", e)
        }

        // Final verification
        val finalState = !isAirplaneModeOn(context)
        Log.d(TAG, "Final airplane mode OFF state: $finalState")
        return finalState
    }

    /**
     * Execute a shell command and return output.
     */
    private fun executeShell(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText()
            process.waitFor()
            val exitCode = process.exitValue()
            val result = "exit=$exitCode stdout=[$stdout] stderr=[$stderr]"
            Log.d(TAG, "Shell [$command] -> $result")
            result
        } catch (e: Exception) {
            val error = "Exception: ${e.message}"
            Log.d(TAG, "Shell [$command] -> $error")
            error
        }
    }

    /**
     * Check if the WRITE_SECURE_SETTINGS permission is granted.
     */
    fun hasPermission(context: Context): Boolean {
        return try {
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
        } catch (e: Exception) {
            false
        }
    }
}
