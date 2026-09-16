@echo off
setlocal
echo =======================================================
echo          EduPulse - Smart Campus CLI Launcher
echo =======================================================
echo.

if not exist "bin" mkdir bin

echo [1/2] Compiling Java codebase...
javac -d bin -encoding UTF-8 src\com\vityarthi\edupulse\common\*.java src\com\vityarthi\edupulse\model\*.java src\com\vityarthi\edupulse\util\*.java src\com\vityarthi\edupulse\pattern\factory\*.java src\com\vityarthi\edupulse\pattern\strategy\*.java src\com\vityarthi\edupulse\pattern\observer\*.java src\com\vityarthi\edupulse\repository\*.java src\com\vityarthi\edupulse\service\*.java src\com\vityarthi\edupulse\cli\*.java src\com\vityarthi\edupulse\Main.java test\com\vityarthi\edupulse\test\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed! Please check JDK installation.
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Compilation successful! Launching EduPulse CLI...
echo.

if "%~1"=="" (
    java -cp bin com.vityarthi.edupulse.Main
) else (
    java -cp bin com.vityarthi.edupulse.Main %*
)

endlocal
