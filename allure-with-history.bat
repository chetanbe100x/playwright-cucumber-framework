@echo off
echo Running Allure report with history tracking...

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

REM Create directories if they don't exist
if not exist "build\allure-results" mkdir "build\allure-results"
if not exist "allure-report" mkdir "allure-report"

REM Check if we have a previous report with history
if exist "allure-report\history" (
    echo Copying previous history to current results...
    if not exist "build\allure-results\history" mkdir "build\allure-results\history"
    xcopy /E /I /Y "allure-report\history" "build\allure-results\history" >nul
)

REM Generate the report
echo Generating Allure report with history...
allure generate build\allure-results -o allure-report --clean

REM Open the report
echo Opening Allure report...
start allure-report\index.html

echo.
echo Note: Run this batch file after each test execution to maintain history.
echo.
