@echo off
echo ==========================================
echo    CampusErrand - Docker Init Script
echo ==========================================
echo.

set PROJECT_ROOT=%~dp0
set PROJECT_ROOT=%PROJECT_ROOT:~0,-1%

echo [1/4] Stopping existing containers...
docker-compose -f "%PROJECT_ROOT%\docker-compose.yml" down

echo.
echo [2/4] Removing old volumes (fresh start)...
docker-compose -f "%PROJECT_ROOT%\docker-compose.yml" down -v

echo.
echo [3/4] Starting MySQL and Redis containers...
docker-compose -f "%PROJECT_ROOT%\docker-compose.yml" up -d

echo.
echo [4/4] Waiting for services to be healthy...
echo.
echo Please wait 30 seconds for MySQL initialization...
timeout /t 30 /nobreak >nul

echo.
echo ==========================================
echo    Docker Services Started!
echo ==========================================
echo.
docker-compose -f "%PROJECT_ROOT%\docker-compose.yml" ps
echo.
echo - MySQL: localhost:3306 (root/root_password)
echo - Redis: localhost:6379
echo.
echo Note: MySQL will auto-create 'campus_errand' database
echo.
pause
