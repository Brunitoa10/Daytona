@echo off
rem Borra los archivos de la version anterior (Suncoast 500) que quedaron tras renombrar a Daytona.
cd /d "%~dp0"
if exist src\main\java\com\suncoast rmdir /s /q src\main\java\com\suncoast
if exist src\test\java\com\suncoast rmdir /s /q src\test\java\com\suncoast
del /q src\main\resources\native\suncoast_input_*.dll 2>nul
del /q dist\lib\native\suncoast_input_*.dll 2>nul
del /q dist\suncoast500.jar 2>nul
del /q native\suncoast_input.c 2>nul
echo Listo. Ya podes borrar este archivo.
pause
