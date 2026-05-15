@echo off
echo ==========================================
echo    CampusErrand - Docker Init Script
echo ==========================================
echo.

set PROJECT_ROOT=%~dp0
set PROJECT_ROOT=%PROJECT_ROOT:~0,-1%

echo [1/4] Stopping existing containers and removing volumes...
docker-compose -f "%PROJECT_ROOT%\docker-compose.yml" down -v 2>nul

echo.
echo [2/4] Starting MySQL and Redis containers...
docker-compose -f "%PROJECT_ROOT%\docker-compose.yml" up -d

echo.
echo [3/4] Waiting for MySQL to be ready...
set MAX_WAIT=60
set WAITED=0
:wait_loop
docker exec campus-errand-mysql mysqladmin ping -h 127.0.0.1 -uroot -proot_password --silent >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo MySQL is ready!
    goto mysql_ready
)
set /a WAITED+=2
if %WAITED% GEQ %MAX_WAIT% (
    echo Timeout waiting for MySQL. Check container logs:
    docker logs campus-errand-mysql
    goto :error
)
timeout /t 2 /nobreak >nul
echo Waiting... (%WAITED%/%MAX_WAIT%s)
goto wait_loop

:mysql_ready
echo.
echo [4/4] Verifying database initialization...
docker exec campus-errand-mysql mysql -uroot -proot_password campus_errand -e "SHOW TABLES;" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Warning: Could not verify tables. Checking init script execution...
    docker exec campus-errand-mysql mysql -uroot -proot_password -e "SELECT * FROM campus_errand.tb_user LIMIT 3;" 2>nul
)

echo.
echo ==========================================
echo    Docker Services Started!
echo ==========================================
echo.
docker-compose -f "%PROJECT_ROOT%\docker-compose.yml" ps
echo.
echo - MySQL: localhost:3306 (root/root_password)
echo - Redis: localhost:6379
echo - Database: campus_errand (auto-initialized with test data)
echo.
echo Test users (mock login with code 1-5):
echo   openid: dev_openid_1 ~ dev_openid_5
echo   Admin: admin / admin123 (auto-created on API startup)
echo.
pause
exit /b 0

:error
echo.
echo Failed to start Docker services!
pause
exit /b 1
