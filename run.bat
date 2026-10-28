@echo off
cd /d "%~dp0"
if not exist bin\cinema\Main.class (
  echo Please run compile.bat first.
  exit /b 1
)
if not exist db.properties (
  echo Copy db.properties.example to db.properties and enter your MySQL password.
  exit /b 1
)
java -cp "bin;lib\mysql-connector-j-9.3.0.jar" cinema.Main
