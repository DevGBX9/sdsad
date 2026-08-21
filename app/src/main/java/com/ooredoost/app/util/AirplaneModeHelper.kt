package com.ooredoost.app.util

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.IBinder
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log

/**
 * Advanced Helper for toggling Airplane Mode and Cellular Radio with full system capabilities.
 * Uses multiple layers to ensure complete radio shutdown (Cellular, SIM, Wi-Fi, Bluetooth)
 * exactly like the Notification Quick Settings Tile.
 */
object AirplaneModeHelper {

    private const val TAG = "AirplaneModeHelper"
    private var hiddenApiUnlocked = false

    /**
     * Unlock hidden API restrictions on Android 9+ using WRITE_SECURE_SETTINGS.
     */
    private fun ensureHiddenApiUnlocked(context: Context) {
        if (hiddenApiUnlocked) return
        try {
            val cr = context.contentResolver
            Settings.Global.putInt(cr, "hidden_api_policy", 1)
            Settings.Global.putInt(cr, "hidden_api_policy_p_apps", 1)
            Settings.Global.putInt(cr, "hidden_api_policy_pre_p_apps", 1)
            hiddenApiUnlocked = true
            Log.d(TAG, "Hidden API restrictions unlocked successfully")
        } catch (e: Throwable) {
            Log.w(TAG, "Could not unlock hidden API policy: ${e.message}")
        }
    }

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
     * Enable airplane mode with full radio shutdown (SIM, Cellular, Wi-Fi).
     */
    fun enableAirplaneMode(context: Context): Boolean {
        return setAirplaneModeFull(context, true)
    }

    /**
     * Disable airplane mode with full radio reconnection (re-enables SIM & Mobile Data).
     */
    fun disableAirplaneMode(context: Context): Boolean {
        return setAirplaneModeFull(context, false)
    }

    /**
     * Executes complete airplane mode toggle across all Android subsystem layers:
     * Layer 1: IConnectivityManager System Service (Exact same entry point as Notification Quick Settings)
     * Layer 2: ConnectivityManager.setAirplaneMode reflection
     * Layer 3: TelephonyManager radio power & cellular data toggle
     * Layer 4: Settings.Global AIRPLANE_MODE_ON & AIRPLANE_MODE_RADIOS
     */
    private fun setAirplaneModeFull(context: Context, enabled: Boolean): Boolean {
        val value = if (enabled) 1 else 0
        var performed = false

        ensureHiddenApiUnlocked(context)

        // ═════════════════════════════════════════════════════════════════════
        // Layer 1: IConnectivityManager System Service (Official SystemUI call)
        // ═════════════════════════════════════════════════════════════════════
        try {
            val smClass = Class.forName("android.os.ServiceManager")
            val getServiceMethod = smClass.getMethod("getService", String::class.java)
            val binder = getServiceMethod.invoke(null, "connectivity") as? IBinder

            if (binder != null) {
                val stubClass = Class.forName("android.net.IConnectivityManager\$Stub")
                val asInterfaceMethod = stubClass.getMethod("asInterface", IBinder::class.java)
                val iConnectivityManager = asInterfaceMethod.invoke(null, binder)

                if (iConnectivityManager != null) {
                    val setAirplaneMethod = iConnectivityManager.javaClass.getMethod(
                        "setAirplaneMode",
                        Boolean::class.javaPrimitiveType
                    )
                    setAirplaneMethod.invoke(iConnectivityManager, enabled)
                    Log.d(TAG, "✅ Layer 1: IConnectivityManager.setAirplaneMode($enabled) executed!")
                    performed = true
                }
            }
        } catch (e: Throwable) {
            Log.d(TAG, "Layer 1 fallback: ${e.message}")
        }

        // ═════════════════════════════════════════════════════════════════════
        // Layer 2: ConnectivityManager Manager Instance Reflection
        // ═════════════════════════════════════════════════════════════════════
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (cm != null) {
                val setAirplaneMethod = cm.javaClass.getMethod(
                    "setAirplaneMode",
                    Boolean::class.javaPrimitiveType
                )
                setAirplaneMethod.isAccessible = true
                setAirplaneMethod.invoke(cm, enabled)
                Log.d(TAG, "✅ Layer 2: ConnectivityManager.setAirplaneMode($enabled) executed!")
                performed = true
            }
        } catch (e: Throwable) {
            Log.d(TAG, "Layer 2 fallback: ${e.message}")
        }

        // ═════════════════════════════════════════════════════════════════════
        // Layer 3: TelephonyManager Radio Power (Direct SIM / Cellular Reset)
        // ═════════════════════════════════════════════════════════════════════
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            if (tm != null) {
                // Radio power: false when airplane ON, true when airplane OFF
                val setRadioPowerMethod = tm.javaClass.getMethod(
                    "setRadioPower",
                    Boolean::class.javaPrimitiveType
                )
                setRadioPowerMethod.isAccessible = true
                setRadioPowerMethod.invoke(tm, !enabled)
                Log.d(TAG, "✅ Layer 3: TelephonyManager.setRadioPower(${!enabled}) executed!")
            }
        } catch (e: Throwable) {
            Log.d(TAG, "Layer 3 fallback: ${e.message}")
        }

        // ═════════════════════════════════════════════════════════════════════
        // Layer 4: Settings.Global Database + Broadcast
        // ═════════════════════════════════════════════════════════════════════
        try {
            val cr = context.contentResolver
            // Make sure all radios are configured to be affected
            Settings.Global.putString(cr, Settings.Global.AIRPLANE_MODE_RADIOS, "cell,bluetooth,wifi,nfc,wimax")
            val putResult = Settings.Global.putInt(cr, Settings.Global.AIRPLANE_MODE_ON, value)
            if (putResult) performed = true
            Log.d(TAG, "✅ Layer 4: Settings.Global AIRPLANE_MODE_ON=$value written (result=$putResult)")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException on Settings.Global (WRITE_SECURE_SETTINGS missing)", e)
            return false
        } catch (e: Throwable) {
            Log.w(TAG, "Layer 4 exception: ${e.message}")
        }

        // Safe broadcast attempt
        try {
            val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
                putExtra("state", enabled)
            }
            context.sendBroadcast(intent)
        } catch (e: Throwable) {
            // Normal to be ignored on Android 7+
        }

        return performed
    }

    /**
     * Toggle airplane mode.
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
