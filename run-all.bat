@echo off
echo ========================================
echo   Starting Full Application
echo ========================================

echo.
echo [1/3] Starting Backend in new window...
start "Backend" cmd /k "cd /d %~dp0backend && C:\apache-maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run"

echo [2/3] Waiting 20 seconds for backend to start...
timeout /t 20 /nobreak >nul

echo [3/3] Starting Frontend...
cd /d "%~dp0frontend"
call npm install
call npm start
