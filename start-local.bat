@echo off
echo Password: YourStrong@Password123
echo Username: sa
echo Database: grace_db
echo.
echo 3. Stop: docker-compose down
echo 2. View logs: docker-compose logs -f sqlserver
echo 1. Run Spring Boot: mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
echo Next steps:
echo.
echo SQL Server is ready!
echo.

docker-compose ps
echo Checking SQL Server health...
echo.

timeout /t 30 /nobreak
echo Waiting for SQL Server to start (30 seconds)...
echo.

docker-compose up -d
echo Starting SQL Server container...

REM Quick setup script for local SQL Server with Docker

