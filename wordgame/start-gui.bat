@echo off
echo Starting Word Game GUI...

REM Set JavaFX module path (adjust path as needed)
set JAVAFX_PATH=--module-path "lib\javafx" --add-modules javafx.controls,javafx.fxml

REM Run the application
java %JAVAFX_PATH% -cp "target\client-1.0.0-jar-with-dependencies.jar" com.dat.wordgame.client.ClientMain

pause