# OoreDoost - PowerShell Permissions Granter
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "          ✈️ OoreDoost - All Permissions Granter" -ForegroundColor Cyan
Write-Host "======================================================================`n" -ForegroundColor Cyan

# 1. Device check
Write-Host "[1/4] Checking ADB Device connection..." -ForegroundColor Yellow
& adb devices

# 2. PM Permissions
Write-Host "`n[2/4] Granting System & Secure Permissions (PM)..." -ForegroundColor Yellow
$pmPermissions = @(
    "android.permission.WRITE_SECURE_SETTINGS",
    "android.permission.DUMP",
    "android.permission.PACKAGE_USAGE_STATS",
    "android.permission.POST_NOTIFICATIONS",
    "android.permission.READ_PHONE_STATE",
    "android.permission.ACCESS_NETWORK_STATE",
    "android.permission.CHANGE_NETWORK_STATE",
    "android.permission.ACCESS_COARSE_LOCATION",
    "android.permission.ACCESS_FINE_LOCATION"
)

foreach ($perm in $pmPermissions) {
    & adb shell pm grant com.ooredoost.app $perm 2>$null
}
Write-Host "[OK] PM Permissions applied successfully." -ForegroundColor Green

# 3. AppOps Permissions
Write-Host "`n[3/4] Granting AppOps & Background Permissions..." -ForegroundColor Yellow
$appOps = @(
    "RUN_IN_BACKGROUND",
    "RUN_ANY_IN_BACKGROUND",
    "AUTO_START",
    "SYSTEM_ALERT_WINDOW",
    "GET_USAGE_STATS",
    "WRITE_SETTINGS",
    "POST_NOTIFICATION",
    "BOOT_COMPLETED"
)

foreach ($op in $appOps) {
    & adb shell cmd appops set com.ooredoost.app $op allow 2>$null
}
Write-Host "[OK] AppOps permissions applied successfully." -ForegroundColor Green

# 4. Battery Optimization
Write-Host "`n[4/4] Whitelisting from Battery Optimization / Doze mode..." -ForegroundColor Yellow
& adb shell dumpsys deviceidle whitelist +com.ooredoost.app 2>$null
Write-Host "[OK] Battery whitelist applied successfully." -ForegroundColor Green

Write-Host "`n======================================================================" -ForegroundColor Green
Write-Host "    🎉 SUCCESS: All permissions granted! OoreDoost is ready." -ForegroundColor Green
Write-Host "======================================================================`n" -ForegroundColor Green
