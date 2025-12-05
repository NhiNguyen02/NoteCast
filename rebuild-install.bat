@echo off
REM Script to completely rebuild and reinstall NoteCast app
REM This ensures the latest code changes are included in the APK

echo ========================================
echo NoteCast - Clean Build and Install
echo ========================================
echo.

echo [1/5] Uninstalling existing app...
adb uninstall com.example.notecast
echo.

echo [2/5] Cleaning build artifacts...
call gradlew clean
echo.

echo [3/5] Building debug APK...
call gradlew assembleDebug
echo.

echo [4/5] Verifying APK exists...
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo APK found: app\build\outputs\apk\debug\app-debug.apk
) else (
    echo ERROR: APK not found after build!
    pause
    exit /b 1
)
echo.

echo [5/5] Installing APK...
adb install -r app\build\outputs\apk\debug\app-debug.apk
echo.

echo ========================================
echo Build and install completed!
echo ========================================
pause

