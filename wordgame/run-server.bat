@echo off
echo ========================================
echo        🎮 Word Game - Server
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
echo Starting Word Game Server on port 7777...
echo.

REM Run the server
java -cp "server\target\server-1.0.0-jar-with-dependencies.jar" com.dat.wordgame.server.ServerMain

echo.
echo Server stopped. Press any key to exit...
pause > nul