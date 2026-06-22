@echo off
echo ================================================
echo  Khoi dong Client - Quan ly thiet bi van phong
echo ================================================

cd /d "%~dp0"

echo [1] Build client...
call mvn -pl common,client-app package -DskipTests -q
if errorlevel 1 (
    echo BUILD THAT BAI! Kiem tra log tren.
    pause
    exit /b 1
)

echo [2] Khoi dong Client...
java -jar client-app\target\client-app-1.0-SNAPSHOT-jar-with-dependencies.jar

pause
