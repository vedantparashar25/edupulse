#!/usr/bin/env bash
set -e

echo "======================================================="
echo "         EduPulse - Smart Campus CLI Launcher"
echo "======================================================="
echo ""

mkdir -p bin

echo "[1/2] Compiling Java codebase..."
javac -d bin -encoding UTF-8 $(find src test -name "*.java")

echo "[2/2] Compilation successful! Launching EduPulse CLI..."
echo ""

if [ -z "$1" ]; then
    java -cp bin com.vityarthi.edupulse.Main
else
    java -cp bin com.vityarthi.edupulse.Main "$@"
fi
