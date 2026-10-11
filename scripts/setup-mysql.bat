@echo off
REM Creates the hostel_management database using the SQL file (asks for the MySQL root password)
cd /d "%~dp0.."
mysql -u root -p < database\hostel_management.sql
echo.
echo Database created.
pause
