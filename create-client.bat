@echo off
echo ========================================
echo   CREATION D'UN NOUVEAU CLIENT
echo ========================================
echo.

cd /d %~dp0
mvn exec:java -Dexec.mainClass="com.banknet.util.ClientInfoUtil" -Dexec.args="--create" -q

pause



