@echo off
REM Runs the built JAR (build it first with build.bat)
cd /d "%~dp0.."
java -jar target\hostel-management.jar
pause
