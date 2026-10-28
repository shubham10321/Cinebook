@echo off
cd /d "%~dp0"
if not exist bin mkdir bin
echo Compiling CineBook...
javac -encoding UTF-8 -cp "lib\mysql-connector-j-9.3.0.jar" -d bin src\cinema\*.java src\cinema\model\*.java src\cinema\dao\*.java src\cinema\ui\*.java src\cinema\util\*.java
if %ERRORLEVEL% NEQ 0 (
  echo Compile failed.
  exit /b 1
)
echo Compile successful. Class files are in the bin folder.
