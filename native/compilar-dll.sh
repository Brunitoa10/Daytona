#!/bin/sh
# Recompila las DLL nativas de entrada (requiere MinGW-w64). Ya vienen compiladas en src/main/resources/native.
set -e
cd "$(dirname "$0")"
OUT=../src/main/resources/native
mkdir -p "$OUT"
x86_64-w64-mingw32-gcc -O2 -shared -Iinclude daytona_input.c -o "$OUT/daytona_input_x64.dll" -lwinmm -static-libgcc -s
i686-w64-mingw32-gcc -O2 -shared -Iinclude daytona_input.c -o "$OUT/daytona_input_x86.dll" -lwinmm -static-libgcc -s -Wl,--kill-at
echo "DLLs generadas en $OUT"
