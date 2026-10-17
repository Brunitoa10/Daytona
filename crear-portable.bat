@echo off
rem ==================================================================
rem  Crea una version PORTABLE con Java incluido: no necesita instalar
rem  nada ni internet. Copia la carpeta portable\Daytona a un
rem  pendrive o a cualquier PC (Windows 7 en adelante) y abri Jugar.bat
rem
rem  IMPORTANTE para Windows 7: ejecuta este script con un Java 8
rem  (JAVA_HOME apuntando a un JDK/JRE 8), porque se copia ESE Java.
rem  Si las PC destino son de 32 bits, usa un Java 8 de 32 bits.
rem ==================================================================
setlocal
cd /d "%~dp0"
if not exist dist\daytona.jar call compilar.bat
if "%JAVA_HOME%"=="" (
  for /f "delims=" %%j in ('where java 2^>nul') do if not defined JBIN set "JBIN=%%~dpj"
) else (
  set "JBIN=%JAVA_HOME%\bin\"
)
if not defined JBIN (echo No se encontro Java. Defini JAVA_HOME. & pause & exit /b 1)
for %%d in ("%JBIN%..") do set "JROOT=%%~fd"
if exist "%JROOT%\jre\bin\java.exe" set "JROOT=%JROOT%\jre"
echo Usando Java en: %JROOT%
if exist portable rmdir /s /q portable
mkdir portable\Daytona
xcopy /e /i /y /q dist portable\Daytona >nul
xcopy /e /i /y /q "%JROOT%" portable\Daytona\jre >nul
copy /y Jugar.bat portable\Daytona\ >nul
copy /y README.md portable\Daytona\ >nul
echo.
echo Version portable lista en: portable\Daytona  ^(abrir Jugar.bat^)
pause
