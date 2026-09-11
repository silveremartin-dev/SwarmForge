@echo off
REM SwarmForge - Lanceur Universel Serveur Java + Scénario Preset + Client Web
setlocal enabledelayedexpansion

echo =======================================================================
echo          SwarmForge - Serveur Java et Scenarios Academiques           
echo =======================================================================
echo.
echo Choisissez un scenario de recherche parmi les presets ci-dessous :
echo.
echo   [ 1] ACAD_01_LEVY_BROWNIAN             (Levy Flights vs Brownian Walk)
echo   [ 2] ACAD_02_POLYETHISM_BDI            (Polyethisme et Specialisation BDI)
echo   [ 3] ACAD_03_NEST_MORPHOGENESIS        (Morphogenese du Nid et Microclimat)
echo   [ 4] ACAD_04_INTERSPECIFIC_COMPETITION (Competition Lasius vs Linepithema) [DEFAUT]
echo   [ 5] ACAD_05_TROPHALLAXIS              (Flux Nutritifs et Trophallaxie)
echo   [ 6] ACAD_06_EPIDEMIOLOGY_QUARANTINE   (Epidemiologie et Auto-Quarantaine)
echo   [ 7] ACAD_07_ATTINE_FUNGI              (Symbiose Champignon - Fourmis Atta)
echo   [ 8] ACAD_08_STIGMERGIC_PHEROMONES     (Stigmergie et Resolution de Labyrinthe)
echo   [ 9] ACAD_09_DULOSIS_RAID              (Raids d'Esclavagisme Polyergus vs Formica)
echo   [10] ACAD_10_SAVANNA_COEVOLUTION       (Adaptation Tropicale Savane Serengeti)
echo   [11] ACAD_11_ALPINE_THERMOREGULATION   (Cryo-Tolerance et Thermoregulation Alpine)
echo   [12] ACAD_12_BOREAL_SOLAR_DOMES        (Domes d'Epines et Chaleur Solaire Taiga)
echo   [13] ACAD_13_STEPPE_HARVESTING         (Granivorie et Resistance Secheresse Steppe)
echo   [14] ACAD_14_WETLAND_FLOOD_RAFTING     (Radeau d'Inondation Solenopsis)
echo   [15] ACAD_15_WASP_WILD_BEEHIVE         (Guepier Suspendu vs Ruche Sauvage)
echo   [16] ACAD_16_APICULTURAL_APIARY        (Rucher Moderne Dadant et Butinage)
echo.
set /p SCENARIO_CHOICE="Entrez le numero du scenario (1-16) [Defaut: 4] : "
if "%SCENARIO_CHOICE%"=="" set SCENARIO_CHOICE=4

echo.
echo [1/2] Lancement du serveur SwarmForge Java (Scenario #%SCENARIO_CHOICE%)...
cd /d "%~dp0.."

REM Demarrage du serveur Java
start "SwarmForge-Server-Java" cmd /c "mvn exec:java -pl swarmforge-server -Dexec.args=\"--scenario %SCENARIO_CHOICE%\" & pause"

echo [2/2] Ouverture du Client Web 3D (Chrome)...
timeout /t 3 /nobreak >nul
start "" "http://localhost:5173"

echo.
echo =======================================================================
echo  Serveur Java actif (:50051 gRPC, :8081 WebSocket)
echo  Client Web connecte sur http://localhost:5173
echo =======================================================================
