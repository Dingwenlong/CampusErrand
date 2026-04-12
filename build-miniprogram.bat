@echo off
echo ==========================================
echo    CampusErrand - Build WeChat Mini Program
echo ==========================================
echo.

set PROJECT_ROOT=%~dp0
set PROJECT_ROOT=%PROJECT_ROOT:~0,-1%

echo Building uni-app to WeChat Mini Program...
echo.
cd /d %PROJECT_ROOT%\campus-errand-uniapp
npm run build:mp-weixin

echo.
echo ==========================================
echo    Build Complete!
echo ==========================================
echo.
echo Output: %PROJECT_ROOT%\campus-errand-uniapp\dist\build\mp-weixin
echo.
pause
