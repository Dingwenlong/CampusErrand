@echo off
echo ==========================================
echo    Applying Test Data (Empty State)
echo ==========================================
echo.

set SQL_FILE=%~dp0test_data_empty.sql
set MYSQL_CMD=mysql -uroot -proot_password campus_errand

echo Executing SQL script...
%MYSQL_CMD% < "%SQL_FILE%"

if %errorlevel% equ 0 (
    echo.
    echo SUCCESS: Test data applied successfully!
    echo.
) else (
    echo.
    echo ERROR: Failed to apply test data!
    echo Please check MySQL connection and password.
    echo.
)

pause
