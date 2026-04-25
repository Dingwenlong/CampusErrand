@echo off
echo ==========================================
echo    CampusErrand - Startup Script
echo ==========================================
echo.

set PROJECT_ROOT=%~dp0
set PROJECT_ROOT=%PROJECT_ROOT:~0,-1%

echo [1/3] Checking and releasing ports...
echo - Checking port 8081 (API)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
    if not "%%a"=="" (
        echo - Killing process %%a on port 8081
        taskkill /F /PID %%a >nul 2>&1
    )
)
echo - Checking port 3000 (Admin)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :3000 ^| findstr LISTENING') do (
    if not "%%a"=="" (
        echo - Killing process %%a on port 3000
        taskkill /F /PID %%a >nul 2>&1
    )
)
echo Ports released.
echo.

echo [2/3] Starting API Service (Spring Boot)...
start "API-Service" cmd /k "cd /d %PROJECT_ROOT%\campus-errand-api && mvn spring-boot:run"

echo.
echo [3/3] Starting Admin Frontend (Vue)...
start "Admin-Frontend" cmd /k "cd /d %PROJECT_ROOT%\campus-errand-admin && pnpm dev"

echo.
echo ==========================================
echo    All Services Started!
echo ==========================================
echo.
echo - API: http://localhost:8081/api
echo - Admin: http://localhost:3000
echo.
echo Note: To build WeChat Mini Program, run build-miniprogram.bat
echo.
pause
