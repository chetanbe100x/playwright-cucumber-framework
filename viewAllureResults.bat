@echo off
echo Running Allure report server...

REM Check if Allure is installed
where allure >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Allure command-line tool is not installed or not in PATH.
    echo Please install Allure command-line tool:
    echo 1. Download from: https://github.com/allure-framework/allure2/releases
    echo 2. Extract to a directory on your computer
    echo 3. Add the bin directory to your PATH
    echo.
    pause
    exit /b 1
)

REM Create allure-results directory if it doesn't exist
if not exist "build\allure-results" mkdir "build\allure-results"

REM Run Allure server
echo Starting Allure report server...
allure serve build\allure-results

echo Allure report server closed.
