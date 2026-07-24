@echo off
REM SwarmForge Full Build Script (Windows)
REM Compiles and packages all modules across the repository.

echo ========================================
echo   SwarmForge - Full Build All (Windows)
echo ========================================
echo.

cd /d "%~dp0.."

set SKIP_TESTS=-DskipTests
set MVN_OPTS=

:parse_args
if "%~1"=="" goto continue_build
if "%~1"=="--debug" (
    echo [INFO] Debug mode enabled for build
    set "MVN_OPTS=%MVN_OPTS% -Dmaven.compiler.debug=true -Dmaven.compiler.debuglevel=lines,vars,source"
)
if "%~1"=="--with-tests" (
    echo [INFO] Running tests during build
    set "SKIP_TESTS="
)
shift
goto parse_args

:continue_build
echo Building ALL project modules with Maven...
call mvn package %SKIP_TESTS% %MVN_OPTS% %*
if errorlevel 1 (
    echo.
    echo ERROR: Build Failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Build Successful for ALL modules!
echo ========================================
