@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "PS1=%SCRIPT_DIR%start-api.ps1"
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
  echo [ERROR] start-api failed.
  if "%KEEP_WINDOW%"=="1" pause
  exit /b 1
)

echo.
echo [OK] start-api completed.
if "%KEEP_WINDOW%"=="1" (
  echo Press any key to close this window...
  pause >nul
)
endlocal
exit /b 0
