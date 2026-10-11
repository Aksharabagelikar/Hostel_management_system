@echo off
REM Starts the Hostel Management System (run from the project folder or from scripts folder)
cd /d "%~dp0.."
echo Starting Hostel Management System on http://localhost:8080 ...
call mvn spring-boot:run
pause
