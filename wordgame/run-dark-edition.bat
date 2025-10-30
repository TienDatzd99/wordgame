@echo off
echo ========================================
echo      🌑 WORD GAME - DARK EDITION 🌑
echo ========================================
echo.
echo 🔥 Launching GUARANTEED Dark Theme...
echo.

cd /d "d:\wordgame\wordgame"

REM Kill any existing Java processes
taskkill /f /im java.exe 2>nul

REM Wait a moment
timeout /t 2 /nobreak >nul

REM Run the Dark Edition directly from compiled classes
java -cp "client/target/classes;common/target/classes" --module-path "C:\Program Files\Java\javafx-sdk-21.0.2\lib" --add-modules javafx.controls,javafx.fxml,javafx.base,javafx.graphics com.dat.wordgame.client.ui.DarkLoginView

echo.
echo Dark Edition closed. Press any key to exit...
pause >nul