@echo off
REM =======================================================================
REM   SwarmForge - Test Tout-en-Un : Serveur Java + Multi-Clients (BAT)
REM =======================================================================

cd /d "%~dp0..\.."

echo =======================================================================
echo        SwarmForge - Lanceur de Test Multi-Clients Tout-en-Un
echo =======================================================================
echo.
echo   Scenarios Academiques Disponibles :
echo    [ 1] ACAD_01_LEVY_BROWNIAN             (Levy Flights vs Brownian Walk)
echo    [ 2] ACAD_02_POLYETHISM_BDI            (Polyethisme et Specialisation BDI)
echo    [ 3] ACAD_03_NEST_MORPHOGENESIS        (Morphogenese du Nid et Microclimat)
echo    [ 4] ACAD_04_INTERSPECIFIC_COMPETITION (Competition Lasius vs Linepithema) [DEFAUT]
echo    [ 5] ACAD_05_TROPHALLAXIS              (Flux Nutritifs et Trophallaxie)
echo    [ 6] ACAD_06_EPIDEMIOLOGY_QUARANTINE   (Epidemiologie et Auto-Quarantaine)
echo    [ 7] ACAD_07_ATTINE_FUNGI              (Symbiose Champignon - Fourmis Atta)
echo    [ 8] ACAD_08_STIGMERGIC_PHEROMONES     (Stigmergie et Resolution Labyrinthe)
echo    [ 9] ACAD_09_DULOSIS_RAID              (Raids d'Esclavagisme Polyergus)
echo    [10] ACAD_10_SAVANNA_COEVOLUTION       (Adaptation Tropicale Serengeti)
echo    [11] ACAD_11_ALPINE_THERMOREGULATION   (Cryo-Tolerance et Thermoregulation)
echo    [12] ACAD_12_BOREAL_SOLAR_DOMES        (Domes Solaires Foret Boreale)
echo    [13] ACAD_13_STEPPE_HARVESTING         (Granivorie et Secheresse Steppe)
echo    [14] ACAD_14_WETLAND_FLOOD_RAFTING     (Radeau d'Inondation Solenopsis)
echo    [15] ACAD_15_WASP_WILD_BEEHIVE         (Guepier Suspendu vs Ruche Sauvage)
echo    [16] ACAD_16_APICULTURAL_APIARY        (Rucher Moderne Dadant et Butinage)
echo.

set SCENARIO_ID=4
set /p USER_SCENARIO="Entrez le numero du scenario (1-16) [Defaut: 4]: "
if not "%USER_SCENARIO%"=="" set SCENARIO_ID=%USER_SCENARIO%

set WEB_COUNT=2
set /p USER_WEB="Nombre de clients Web a ouvrir (0-5) [Defaut: 2]: "
if not "%USER_WEB%"=="" set WEB_COUNT=%USER_WEB%

set LAUNCH_EDITOR=O
set /p USER_ED="Lancer le client lourd Studio JavaFX (O/N) [Defaut: O]: "
if not "%USER_ED%"=="" set LAUNCH_EDITOR=%USER_ED%

echo.
echo [1/4] Lancement du Serveur Java SwarmForge (Scenario #%SCENARIO_ID%)...
start "SwarmForge Server [Scenario %SCENARIO_ID%]" cmd /k "mvn exec:java -pl swarmforge-server -Dexec.args=\"--scenario %SCENARIO_ID%\""

echo [2/4] Lancement du Serveur Web HTTP Statique...
start "SwarmForge Web Static Server" cmd /k "py scripts\test\serve_web_static.py || python scripts\test\serve_web_static.py"

echo [3/4] Attente de 4 secondes pour initialisation...
timeout /t 4 /nobreak >nul

if %WEB_COUNT% GTR 0 (
    echo [4/4] Ouverture de %WEB_COUNT% client(s) Web...
    for /L %%i in (1,1,%WEB_COUNT%) do (
        start http://localhost:5173
    )
)

if /I "%LAUNCH_EDITOR%"=="O" (
    echo [Bonus] Lancement du Client Lourd Studio JavaFX...
    start "SwarmForge Studio Client" cmd /k "mvn exec:java -pl swarmforge-editor"
)

echo.
echo =======================================================================
echo          TOUS LES COMPOSANTS SONT DEMARRES AVEC SUCCES !
echo =======================================================================
echo   - Serveur gRPC      : localhost:50051
echo   - Serveur WebSocket : ws://localhost:8081
echo   - Client(s) Web     : http://localhost:5173 (%WEB_COUNT% ouverts)
echo =======================================================================
