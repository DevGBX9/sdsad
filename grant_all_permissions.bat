@echo off
title OoreDoost - All Permissions Granter
color 0A

echo ======================================================================
echo           OoreDoost - Grant All ADB Permissions
echo ======================================================================
echo.

:: 1. Check ADB connection
echo [1/4] Checking ADB Device connection...
adb devices
echo.

:: 2. Grant Core PM Permissions
echo [2/4] Granting System and Secure Permissions (PM)...
adb shell pm grant com.ooredoost.app android.permission.WRITE_SECURE_SETTINGS 2>nul
adb shell pm grant com.ooredoost.app android.permission.DUMP 2>nul
adb shell pm grant com.ooredoost.app android.permission.PACKAGE_USAGE_STATS 2>nul
adb shell pm grant com.ooredoost.app android.permission.POST_NOTIFICATIONS 2>nul
adb shell pm grant com.ooredoost.app android.permission.READ_PHONE_STATE 2>nul
adb shell pm grant com.ooredoost.app android.permission.ACCESS_NETWORK_STATE 2>nul
adb shell pm grant com.ooredoost.app android.permission.CHANGE_NETWORK_STATE 2>nul
adb shell pm grant com.ooredoost.app android.permission.ACCESS_COARSE_LOCATION 2>nul
adb shell pm grant com.ooredoost.app android.permission.ACCESS_FINE_LOCATION 2>nul
echo [OK] PM Permissions applied successfully.
echo.

:: 3. Grant AppOps & Background Permissions
echo [3/4] Granting Background Execution & AppOps Permissions...
adb shell cmd appops set com.ooredoost.app RUN_IN_BACKGROUND allow 2>nul
adb shell cmd appops set com.ooredoost.app RUN_ANY_IN_BACKGROUND allow 2>nul
adb shell cmd appops set com.ooredoost.app AUTO_START allow 2>nul
adb shell cmd appops set com.ooredoost.app SYSTEM_ALERT_WINDOW allow 2>nul
adb shell cmd appops set com.ooredoost.app GET_USAGE_STATS allow 2>nul
adb shell cmd appops set com.ooredoost.app WRITE_SETTINGS allow 2>nul
adb shell cmd appops set com.ooredoost.app POST_NOTIFICATION allow 2>nul
adb shell cmd appops set com.ooredoost.app BOOT_COMPLETED allow 2>nul
echo [OK] AppOps permissions applied successfully.
echo.

:: 4. Battery Optimization Exemption
echo [4/4] Whitelisting from Battery Optimization / Doze mode...
adb shell dumpsys deviceidle whitelist +com.ooredoost.app 2>nul
echo [OK] Battery whitelist applied.
echo.

echo ======================================================================
echo     SUCCESS: All permissions granted! OoreDoost is ready to go.
echo ======================================================================
echo.
pause
