@echo off
REM ============================================================================
REM 一键启动脚本
REM
REM 功能说明：
REM   本脚本整合了以下三个独立脚本的功能：
REM   - 客户端编译: compile-client.bat / compile-client.ps1
REM   - API服务启动: start-api.bat / start-api.ps1
REM   - 后台服务启动: start-admin.bat / start-admin.ps1
REM
REM 如需单独运行某个功能，请直接运行对应的脚本文件：
REM   - compile-client.bat  : 仅编译客户端（Uniapp + 后台管理）
REM   - start-api.bat       : 仅启动API服务
REM   - start-admin.bat     : 仅启动后台管理服务
REM ============================================================================

setlocal

set "SCRIPT_DIR=%~dp0"
set "PS1=%SCRIPT_DIR%one-click-start.ps1"
set "KEEP_WINDOW=1"
if "%ONE_CLICK_NO_PAUSE%"=="1" set "KEEP_WINDOW=0"

if not exist "%PS1%" (
  echo [ERROR] File not found: %PS1%
  pause
  exit /b 1
)

where pwsh >nul 2>nul
if %errorlevel%==0 (
  pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%PS1%" %*
) else (
  powershell -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%PS1%" %*
)

if errorlevel 1 (
  echo.
  echo [ERROR] one-click-start failed.
  if "%KEEP_WINDOW%"=="1" pause
  exit /b 1
)

echo.
echo [INFO] API endpoint: http://localhost:8080/api
echo [INFO] API docs:     http://localhost:8080/api/doc.html
echo [INFO] Admin web:    http://localhost:3000
echo.
echo [OK] one-click-start completed.
if "%KEEP_WINDOW%"=="1" (
  echo Press any key to close this window...
  pause >nul
)
endlocal
exit /b 0
