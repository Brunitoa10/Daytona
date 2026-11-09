#!/bin/sh
# Compila Daytona sin internet ni Maven (JDK 8+). Resultado en dist/ (corre en Java 8+ / Windows 7+)
set -e
cd "$(dirname "$0")"
rm -rf out && mkdir -p out/classes dist/lib/native
find src/main/java -name "*.java" > out/sources.txt
javac -source 1.8 -target 1.8 -encoding UTF-8 -nowarn -d out/classes -cp "lib/*" @out/sources.txt
cp -r src/main/resources/. out/classes/
mkdir -p out/classes/META-INF && cp LICENSE THIRD-PARTY-NOTICES.txt out/classes/META-INF/
cp lib/*.jar dist/lib/
cp src/main/resources/native/*.dll dist/lib/native/
jar cfm dist/daytona.jar build-support/MANIFEST.MF -C out/classes .
cp Jugar.bat Jugar.sh dist/ 2>/dev/null || true
cp LICENSE THIRD-PARTY-NOTICES.txt dist/
rm -rf out
echo "Listo: dist/daytona.jar"
