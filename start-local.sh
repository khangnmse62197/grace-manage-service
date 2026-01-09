#!/bin/bash
# Quick setup script for local SQL Server with Docker

echo "Starting SQL Server container..."
docker-compose up -d

echo ""
echo "Waiting for SQL Server to start (30 seconds)..."
sleep 30

echo ""
echo "Checking SQL Server health..."
docker-compose ps

echo ""
echo "SQL Server is ready!"
echo ""
echo "Next steps:"
echo "1. Run Spring Boot: mvn spring-boot:run -Dspring-boot.run.arguments=\"--spring.profiles.active=local\""
echo "2. View logs: docker-compose logs -f sqlserver"
echo "3. Stop: docker-compose down"
echo ""
echo "Database: grace_db"
echo "Username: sa"
echo "Password: YourStrong@Password123"

