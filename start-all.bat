@echo off
echo ==========================================
echo    CampusErrand - Startup Script
echo ==========================================
echo.

set PROJECT_ROOT=%~dp0
set PROJECT_ROOT=%PROJECT_ROOT:~0,-1%

echo [1/2] Starting API Service (Spring Boot)...
start "API-Service" cmd /k "cd /d %PROJECT_ROOT%\campus-errand-api && mvn spring-boot:run"

echo [2/2] Starting Admin Frontend (Vue)...
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
