@echo off
echo ========================================================
echo Starting CodeInsight Spring Boot Backend
echo ========================================================
cd /d "%~dp0backend"
call mvn spring-boot:run
pause
