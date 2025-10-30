@echo off
echo ========================================
echo       🎮 Word Game - GUI Version
echo ========================================
echo.

REM Change to project directory
cd /d "%~dp0"

echo Building project...
call mvn clean package -q

if %errorlevel% neq 0 (
    echo ❌ Build failed! Please check Maven and Java installation.
    pause
    exit /b 1
)

echo ✅ Build successful!
echo.
echo Starting Word Game GUI...
echo.

REM Run the GUI application
java -cp "client\target\client-1.0.0-jar-with-dependencies.jar" com.dat.wordgame.client.ClientMain

echo.
echo Game closed. Press any key to exit...
pause > nul