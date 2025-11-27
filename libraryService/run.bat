@echo off
title Library Service - Docker Runner

docker --version >nul 2>&1
IF ERRORLEVEL 1 (
    echo Please install Docker Desktop first.
    pause
    exit /b
)

echo [INFO] Building jar...
IF EXIST mvnw.cmd (
    call mvnw.cmd clean package -DskipTests
) ELSE (
    mvn clean package -DskipTests
)

IF ERRORLEVEL 1 (
    echo [ERROR] Maven build failed. Please check the error above.
    pause
    exit /b 1
)

docker compose up --build

pause
