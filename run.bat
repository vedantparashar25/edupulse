@echo off
setlocal
echo =======================================================
echo          EduPulse - Smart Campus CLI Launcher
echo =======================================================
echo.

if not exist "bin" mkdir bin

echo [1/2] Compiling Java codebase...
javac -d bin -encoding UTF-8 (Get-ChildItem -Path "src", "test" -Recurse -Filter "*.java").FullName 2>nul
if %ERRORLEVEL% NEQ 0 (
    javac -d bin -encoding UTF-8 src\com\vityarthi\edupulse\common\*.java src\com\vityarthi\edupulse\model\*.java src\com\vityarthi\edupulse\util\*.java src\com\vityarthi\edupulse\pattern\factory\*.java src\com\vityarthi\edupulse\pattern\strategy\*.java src\com\vityarthi\edupulse\pattern\observer\*.java src\com\vityarthi\edupulse\repository\*.java src\com\vityarthi\edupulse\service\*.java src\com\vityarthi\edupulse\cli\*.java src\com\vityarthi\edupulse\web\*.java src\com\vityarthi\edupulse\Main.java test\com\vityarthi\edupulse\test\*.java
)

echo [2/2] Compilation successful!
echo.

if "%~1"=="--web" (
    echo Starting EduPulse Live Server on http://localhost:8080 ...
    java -cp bin com.vityarthi.edupulse.web.EduPulseWebServer
) else if "%~1"=="" (
    java -cp bin com.vityarthi.edupulse.Main
) else (
    java -cp bin com.vityarthi.edupulse.Main %*
)

endlocal
