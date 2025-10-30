@echo off
echo ========================================
echo      🌑 WORD GAME - GUARANTEED DARK 🌑  
echo ========================================
echo.
echo 🔥 Launching ULTRA DARK THEME (Swing)...
echo 📍 This version is GUARANTEED to be dark!
echo.

cd /d "d:\wordgame\wordgame"

REM Kill any existing Java processes
taskkill /f /im java.exe 2>nul >nul

REM Build first to make sure everything is up to date
echo 🔧 Building project...
C:\Users\Admin\tools\apache-maven-3.9.5\bin\mvn clean compile -q

REM Run the Swing dark version (which always works)
echo 🚀 Starting Dark Edition...
java -cp "client/target/classes;common/target/classes" com.dat.wordgame.client.ClientMain --swing

echo.
echo Dark Edition closed. Press any key to exit...
pause >nul