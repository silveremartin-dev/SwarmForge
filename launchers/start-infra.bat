@echo off
echo Starting SwarmForge Infrastructure (Postgres, Redis)...
cd ..
docker-compose up -d
echo.
echo Infrastructure started. You can now run the server.
pause
