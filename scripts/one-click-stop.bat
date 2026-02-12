@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "PS1=%SCRIPT_DIR%one-click-stop.ps1"

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
  echo [ERROR] one-click-stop failed.
  pause
  exit /b 1
)

echo.
echo [OK] one-click-stop completed.
endlocal
exit /b 0
