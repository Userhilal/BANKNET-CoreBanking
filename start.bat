@echo off
echo ========================================
echo   BANKNET - Demarrage avec Docker
echo ========================================
echo.

echo [1/3] Demarrage du conteneur MySQL Docker...
docker-compose up -d

if %errorlevel% neq 0 (
    echo ERREUR: Impossible de demarrer Docker. Verifiez que Docker Desktop est lance.
    pause
    exit /b 1
)

echo.
echo [2/3] Attente du demarrage de MySQL (10 secondes)...
timeout /t 10 /nobreak >nul

echo.
echo [3/3] Lancement de l'application BANKNET...
echo.
mvn clean compile javafx:run

pause



