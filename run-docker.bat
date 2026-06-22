@echo off
echo ================================================
echo  Khoi dong PostgreSQL + Server bang Docker Compose
echo ================================================

cd /d "%~dp0"

echo [1] Build Maven truoc (can thiet cho Docker build)...
call mvn -pl common,server-app package -DskipTests -q
if errorlevel 1 (
    echo BUILD THAT BAI!
    pause
    exit /b 1
)

echo [2] Khoi dong Docker Compose...
docker compose up --build -d

echo.
echo [3] Doi server san sang (15 giay)...
timeout /t 15 /nobreak > nul

echo [4] Kiem tra trang thai...
docker compose ps

echo.
echo ================================================
echo  Server dang chay tai: localhost:9000
echo  PostgreSQL tai: localhost:5432
echo  Chay: run-client.bat de mo client
echo ================================================
pause
