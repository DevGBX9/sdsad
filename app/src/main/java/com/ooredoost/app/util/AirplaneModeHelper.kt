package com.ooredoost.app.util

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
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
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON,
                0
            ) != 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read airplane mode state", e)
            false
        }
    }

    /**
     * Enable airplane mode.
     * @return true if successful, false if permission denied.
     */
    fun enableAirplaneMode(context: Context): Boolean {
        return setAirplaneMode(context, true)
    }

    /**
     * Disable airplane mode.
     * @return true if successful, false if permission denied.
     */
    fun disableAirplaneMode(context: Context): Boolean {
        return setAirplaneMode(context, false)
    }

    /**
     * Sets the airplane mode state using multiple fallback mechanisms:
     * 1. Settings.Global.putInt (core method with WRITE_SECURE_SETTINGS)
     * 2. ConnectivityManager hidden API (if accessible via reflection)
     * 3. Broadcast notification (safely caught)
     */
    private fun setAirplaneMode(context: Context, enabled: Boolean): Boolean {
        val value = if (enabled) 1 else 0
        var success = false

        // 1. Primary Method: Settings.Global (requires WRITE_SECURE_SETTINGS)
        try {
            success = Settings.Global.putInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON,
                value
            )
            Log.d(TAG, "Settings.Global.putInt AIRPLANE_MODE_ON=$value returned: $success")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException on Settings.Global.putInt (WRITE_SECURE_SETTINGS not granted)", e)
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Exception setting AIRPLANE_MODE_ON", e)
        }

        // 2. Secondary Method: ConnectivityManager via reflection (supported on some ROMs like Transsion/Infinix)
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (cm != null) {
                val method = cm.javaClass.getMethod("setAirplaneMode", Boolean::class.javaPrimitiveType)
                method.isAccessible = true
                method.invoke(cm, enabled)
                Log.d(TAG, "ConnectivityManager.setAirplaneMode($enabled) invoked successfully")
            }
        } catch (e: Throwable) {
            // Ignore reflection failure, primary method is main
            Log.d(TAG, "ConnectivityManager reflection skipped: ${e.message}")
        }

        // 3. Broadcast attempt: Safely catch SecurityException since modern Android protects this broadcast
        try {
            val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
                putExtra("state", enabled)
            }
            context.sendBroadcast(intent)
        } catch (e: Throwable) {
            // ACTION_AIRPLANE_MODE_CHANGED is a protected broadcast on Android 4.4+
            // System handles the state change automatically via ContentObserver on Settings.Global
            Log.d(TAG, "Protected broadcast skipped (normal on Android 7+): ${e.message}")
        }

        return success
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
