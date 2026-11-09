@echo off
rem ==========================================================
rem  Compila Daytona SIN internet ni Maven.
rem  Sirve cualquier JDK 8 o superior (el resultado corre en Java 8+ y Windows 7+).
rem  Resultado: carpeta dist\ lista para jugar o copiar.
rem ==========================================================
setlocal
cd /d "%~dp0"
where javac >nul 2>nul || (echo Falta el JDK ^(8 o superior^) en el PATH & pause & exit /b 1)
if exist out rmdir /s /q out
mkdir out\classes
dir /s /b src\main\java\*.java > out\sources.txt
javac -source 1.8 -target 1.8 -encoding UTF-8 -nowarn -d out\classes -cp "lib\*" @out\sources.txt || (pause & exit /b 1)
xcopy /e /i /y /q src\main\resources out\classes >nul
if not exist out\classes\META-INF mkdir out\classes\META-INF
copy /y LICENSE out\classes\META-INF\ >nul
copy /y THIRD-PARTY-NOTICES.txt out\classes\META-INF\ >nul
if not exist dist\lib\native mkdir dist\lib\native
copy /y lib\*.jar dist\lib\ >nul
copy /y src\main\resources\native\*.dll dist\lib\native\ >nul
jar cfm dist\daytona.jar build-support\MANIFEST.MF -C out\classes . || (pause & exit /b 1)
copy /y Jugar.bat dist\ >nul
copy /y LICENSE dist\ >nul
copy /y THIRD-PARTY-NOTICES.txt dist\ >nul
rmdir /s /q out
echo.
echo Listo: dist\daytona.jar  ^(ejecuta dist\Jugar.bat^)
pause
