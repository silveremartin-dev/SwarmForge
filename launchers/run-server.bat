@echo off
echo Starting SwarmForge Server GUI...
echo NOTE: Ensure you have run 'start-infra.bat' first!
cd ..

REM Enable access to internal JDK modules for Netty/gRPC Self-Signed Certificates
set "MAVEN_OPTS=%MAVEN_OPTS% --add-exports=java.base/sun.security.x509=ALL-UNNAMED --add-opens=java.base/sun.security.ssl=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED"

mvn exec:java -pl swarmforge-server -Dexec.mainClass="org.swarmforge.server.ServerGuiApp"
pause
