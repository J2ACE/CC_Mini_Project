@echo off
echo ========================================================
echo Launching CodeInsight Analyzer (Backend + Frontend)
echo ========================================================
start "CodeInsight Backend (Port 8080)" cmd /k "cd /d %~dp0backend && mvn spring-boot:run"
timeout /t 5 /nobreak >nul
start "CodeInsight Frontend (Port 5173)" cmd /k "cd /d %~dp0frontend && npm run dev"
echo.
echo ========================================================
echo Services launched in separate windows!
echo Backend API:  http://localhost:8080
echo Frontend UI:  http://localhost:5173
echo ========================================================
