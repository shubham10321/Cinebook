@echo off
cd /d "%~dp0"
set MYSQL_EXE=C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe
if not exist "%MYSQL_EXE%" (
  echo mysql.exe was not found at the default path.
  echo Open MySQL Workbench or Command Line Client and run database\cinema_booking.sql
  exit /b 1
)
echo This will create the cinebook database and sample data.
set /p MYSQL_USER=MySQL username [root]: 
if "%MYSQL_USER%"=="" set MYSQL_USER=root
"%MYSQL_EXE%" -u %MYSQL_USER% -p < database\cinema_booking.sql
if %ERRORLEVEL% NEQ 0 (
  echo Database setup failed. Check your username and password.
  exit /b 1
)
echo Database setup completed.
