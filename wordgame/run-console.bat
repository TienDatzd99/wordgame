@echo off
echo ========================================
echo      🎮 Word Game - Console Version
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
echo Starting Word Game Console...
echo.

REM Run the console application
java -cp "client\target\client-1.0.0-jar-with-dependencies.jar" com.dat.wordgame.client.ClientMain --console

echo.
echo Game closed. Press any key to exit...
pause > nul