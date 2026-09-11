@echo off
REM SwarmForge Web Client Launcher (Windows)
REM Automatically detects Node.js/Vite or falls back to local HTTP server / static client.

echo ========================================
echo   SwarmForge - Web Client Launcher
echo ========================================
echo.

cd /d "%~dp0.."

set TARGET_DIR=swarmforge-web
set PORT=5173

:parse_args
if "%~1"=="" goto check_environment
if "%~1"=="--static" (
    echo [INFO] Targeting static web client (swarmforge-web-client)
    set TARGET_DIR=swarmforge-web-client
    set PORT=8080
    shift
    goto parse_args
)
if "%~1"=="--port" (
    set "PORT=%~2"
    shift
    shift
    goto parse_args
)
shift
goto parse_args

:check_environment
REM Check if Node and npm are available
where node >nul 2>&1
if %errorlevel% equ 0 (
    where npm >nul 2>&1
    if %errorlevel% equ 0 (
        goto run_with_node
    )
)

echo [INFO] Node.js / npm not detected on PATH. Checking for Python HTTP server fallback...
where python >nul 2>&1
if %errorlevel% equ 0 (
    goto run_with_python
)
where py >nul 2>&1
if %errorlevel% equ 0 (
    goto run_with_py
)

echo.
echo [WARNING] Neither Node.js nor Python were found on your system PATH.
echo Attempting to directly open the static Web client in your default browser...
if exist "swarmforge-web-client\index.html" (
    start "" "swarmforge-web-client\index.html"
    echo Web client opened via file protocol. Note: for full API/gRPC web features, please install Node.js (https://nodejs.org).
    exit /b 0
)

echo ERROR: Unable to start Web Client. Please install Node.js (v18+) or Python 3.
pause
exit /b 1

:run_with_node
if "%TARGET_DIR%"=="swarmforge-web-client" (
    goto run_with_python
)

echo [1/2] Checking dependencies for swarmforge-web...
cd swarmforge-web
if not exist "node_modules" (
    echo [INFO] Installing NPM dependencies...
    call npm install
    if errorlevel 1 (
        echo ERROR: npm install failed.
        cd ..
        pause
        exit /b 1
    )
)

echo [2/2] Starting SwarmForge Web Client (Vite Dev Server)...
echo URL: http://localhost:%PORT%/
echo Press Ctrl+C to stop the web server.
echo.
call npm run dev -- --host --port %PORT% --open
cd ..
exit /b 0

:run_with_python
echo [INFO] Starting local Web server on port %PORT% using Python...
echo URL: http://localhost:%PORT%/
echo Serving directory: %TARGET_DIR%
start "" "http://localhost:%PORT%"
python -m http.server %PORT% --directory "%TARGET_DIR%"
exit /b 0

:run_with_py
echo [INFO] Starting local Web server on port %PORT% using Python Launcher (py)...
echo URL: http://localhost:%PORT%/
echo Serving directory: %TARGET_DIR%
start "" "http://localhost:%PORT%"
py -m http.server %PORT% --directory "%TARGET_DIR%"
exit /b 0
