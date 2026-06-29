@echo off
echo ========================================
echo   LISTE DES CLIENTS DISPONIBLES
echo ========================================
echo.
echo Lancement de l'utilitaire...
echo.

cd /d %~dp0
mvn exec:java -Dexec.mainClass="com.banknet.util.ClientInfoUtil" -Dexec.args="" -q

pause



