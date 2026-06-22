@echo off
echo ================================================
echo  Khoi dong Server - LOCAL (khong dung Docker)
echo  Can PostgreSQL chay tren localhost:5432
echo ================================================

cd /d "%~dp0"

echo [1] Build server...
call mvn -pl common,server-app package -DskipTests -q
if errorlevel 1 (
    echo BUILD THAT BAI!
    pause
    exit /b 1
)

echo [2] Khoi dong Server...
java -jar server-app\target\server-app-1.0-SNAPSHOT-jar-with-dependencies.jar

pause
