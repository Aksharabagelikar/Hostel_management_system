@echo off
REM Builds the runnable JAR: target\hostel-management.jar
cd /d "%~dp0.."
call mvn clean package -DskipTests
echo.
echo Done. Run it with:  java -jar target\hostel-management.jar
pause
