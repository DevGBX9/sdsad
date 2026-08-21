package com.ooredoost.app.util

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log

/**
 * Advanced Helper for toggling Airplane Mode, Cellular Radio Power, and Mobile Data.
 * Works seamlessly across all Android versions (including Android 14 on Infinix devices).
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
     * Fully activates airplane mode and cuts cellular radio/mobile data completely.
     */
    fun enableAirplaneMode(context: Context): Boolean {
        Log.d(TAG, "✈️ Enabling full Airplane Mode & Cutting Data...")
        var success = false

        // 1. Set Airplane Mode ON in Settings.Global
        try {
            val cr = context.contentResolver
            Settings.Global.putString(cr, Settings.Global.AIRPLANE_MODE_RADIOS, "cell,bluetooth,wifi,nfc,wimax")
            Settings.Global.putInt(cr, Settings.Global.AIRPLANE_MODE_ON, 1)
            // Explicitly disable mobile data in settings database
            Settings.Global.putInt(cr, "mobile_data", 0)
            Settings.Global.putInt(cr, "mobile_data0", 0)
            Settings.Global.putInt(cr, "mobile_data1", 0)
            success = true
        } catch (e: Exception) {
            Log.e(TAG, "Settings.Global write error", e)
        }

        // 2. Shut down Cellular Radio Power (Cuts connection to Ooredoo cell tower)
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            if (tm != null) {
                // Method A: setRadioPower(false)
                try {
                    val method = tm.javaClass.getMethod("setRadioPower", Boolean::class.javaPrimitiveType)
                    method.isAccessible = true
                    method.invoke(tm, false)
                    Log.d(TAG, "✅ TelephonyManager.setRadioPower(false) success")
                } catch (e: Throwable) {
                    Log.d(TAG, "setRadioPower fallback: ${e.message}")
                }

                // Method B: setDataEnabled(false)
                try {
                    val method = tm.javaClass.getMethod("setDataEnabled", Boolean::class.javaPrimitiveType)
                    method.isAccessible = true
                    method.invoke(tm, false)
                    Log.d(TAG, "✅ TelephonyManager.setDataEnabled(false) success")
                } catch (e: Throwable) {
                    Log.d(TAG, "setDataEnabled fallback: ${e.message}")
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "TelephonyManager error", e)
        }

        // 3. IConnectivityManager System Service Toggle
        try {
            val smClass = Class.forName("android.os.ServiceManager")
            val getServiceMethod = smClass.getMethod("getService", String::class.java)
            val binder = getServiceMethod.invoke(null, "connectivity") as? IBinder
            if (binder != null) {
                val stubClass = Class.forName("android.net.IConnectivityManager\$Stub")
                val asInterfaceMethod = stubClass.getMethod("asInterface", IBinder::class.java)
                val iConnectivityManager = asInterfaceMethod.invoke(null, binder)
                val setAirplaneMethod = iConnectivityManager?.javaClass?.getMethod("setAirplaneMode", Boolean::class.javaPrimitiveType)
                setAirplaneMethod?.invoke(iConnectivityManager, true)
            }
        } catch (e: Throwable) {
            // Ignore reflection
        }

        // 4. Send safe broadcast
        try {
            val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
                putExtra("state", true)
            }
            context.sendBroadcast(intent)
        } catch (e: Throwable) {
            // Ignored
        }

        return success
    }

    /**
     * Fully deactivates airplane mode, restores cellular radio, and reconnects Mobile Data.
     */
    fun disableAirplaneMode(context: Context): Boolean {
        Log.d(TAG, "📡 Disabling Airplane Mode & Reconnecting Data...")
        var success = false

        // 1. Set Airplane Mode OFF in Settings.Global
        try {
            val cr = context.contentResolver
            Settings.Global.putInt(cr, Settings.Global.AIRPLANE_MODE_ON, 0)
            // Explicitly enable mobile data in settings database
            Settings.Global.putInt(cr, "mobile_data", 1)
            Settings.Global.putInt(cr, "mobile_data0", 1)
            Settings.Global.putInt(cr, "mobile_data1", 1)
            success = true
        } catch (e: Exception) {
            Log.e(TAG, "Settings.Global write error", e)
        }

        // 2. Power ON Cellular Radio & Enable Data
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            if (tm != null) {
                // Power ON radio
                try {
                    val method = tm.javaClass.getMethod("setRadioPower", Boolean::class.javaPrimitiveType)
                    method.isAccessible = true
                    method.invoke(tm, true)
                    Log.d(TAG, "✅ TelephonyManager.setRadioPower(true) success")
                } catch (e: Throwable) {
                    Log.d(TAG, "setRadioPower fallback: ${e.message}")
                }

                // Enable Data
                try {
                    val method = tm.javaClass.getMethod("setDataEnabled", Boolean::class.javaPrimitiveType)
                    method.isAccessible = true
                    method.invoke(tm, true)
                    Log.d(TAG, "✅ TelephonyManager.setDataEnabled(true) success")
                } catch (e: Throwable) {
                    Log.d(TAG, "setDataEnabled fallback: ${e.message}")
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "TelephonyManager error", e)
        }

        // 3. IConnectivityManager System Service Toggle
        try {
            val smClass = Class.forName("android.os.ServiceManager")
            val getServiceMethod = smClass.getMethod("getService", String::class.java)
            val binder = getServiceMethod.invoke(null, "connectivity") as? IBinder
            if (binder != null) {
                val stubClass = Class.forName("android.net.IConnectivityManager\$Stub")
                val asInterfaceMethod = stubClass.getMethod("asInterface", IBinder::class.java)
                val iConnectivityManager = asInterfaceMethod.invoke(null, binder)
                val setAirplaneMethod = iConnectivityManager?.javaClass?.getMethod("setAirplaneMode", Boolean::class.javaPrimitiveType)
                setAirplaneMethod?.invoke(iConnectivityManager, false)
            }
        } catch (e: Throwable) {
            // Ignore reflection
        }

        // 4. Send safe broadcast
        try {
            val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
                putExtra("state", false)
            }
            context.sendBroadcast(intent)
        } catch (e: Throwable) {
            // Ignored
        }

        return success
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
