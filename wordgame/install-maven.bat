@echo off
echo ========================================
echo       Installing Apache Maven
echo ========================================
echo.

REM Set Maven version and download URL
set MAVEN_VERSION=3.9.5
set MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip
set INSTALL_DIR=%USERPROFILE%\tools
set MAVEN_HOME=%INSTALL_DIR%\apache-maven-%MAVEN_VERSION%

echo Creating installation directory...
if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"

echo Downloading Maven %MAVEN_VERSION%...
echo Please wait, this may take a few minutes...

REM Download Maven using PowerShell
powershell -Command "& {Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%INSTALL_DIR%\maven.zip'}"

if %errorlevel% neq 0 (
    echo ❌ Download failed! Please check your internet connection.
    pause
    exit /b 1
)

echo ✅ Download completed!
echo.

echo Extracting Maven...
powershell -Command "& {Expand-Archive -Path '%INSTALL_DIR%\maven.zip' -DestinationPath '%INSTALL_DIR%' -Force}"

if %errorlevel% neq 0 (
    echo ❌ Extraction failed!
    pause
    exit /b 1
)

echo ✅ Maven extracted successfully!
echo.

REM Clean up zip file
del "%INSTALL_DIR%\maven.zip"

echo Setting up environment...
echo Maven installed at: %MAVEN_HOME%
echo.

REM Add to PATH for current session
set PATH=%MAVEN_HOME%\bin;%PATH%

echo Testing Maven installation...
"%MAVEN_HOME%\bin\mvn" -version

if %errorlevel% neq 0 (
    echo ❌ Maven test failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo    ✅ Maven installed successfully!
echo ========================================
echo.
echo Maven location: %MAVEN_HOME%
echo.
echo To make Maven available permanently, add this to your PATH:
echo %MAVEN_HOME%\bin
echo.
echo Or run this command to add to PATH permanently:
echo setx PATH "%%PATH%%;%MAVEN_HOME%\bin"
echo.
echo Press any key to continue...
pause > nul