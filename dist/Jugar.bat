@echo off
rem ==========================================================
rem  Daytona - Windows 7, 8, 10 y 11.
rem  Usa el Java incluido (carpeta jre\, version portable) o el Java 8+ instalado.
rem ==========================================================
setlocal
cd /d "%~dp0"
if exist daytona.jar (set "JAR=daytona.jar") else (set "JAR=dist\daytona.jar")
if exist "jre\bin\javaw.exe" (
  start "" "jre\bin\javaw.exe" -Xms256m -Xmx768m -jar "%JAR%"
  exit /b 0
)
where javaw >nul 2>nul || (
  echo No se encontro Java. Instala Java 8 o superior, o usa la version portable ^(crear-portable.bat^).
  pause
  exit /b 1
)
start "" javaw -Xms256m -Xmx768m -jar "%JAR%"
