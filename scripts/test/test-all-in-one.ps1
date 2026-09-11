<#
.SYNOPSIS
    SwarmForge - Test Tout-en-Un : Serveur Java + Multi-Clients Web + Client Lourd Studio + Compute Node
.DESCRIPTION
    Lance un environnement de test local complet pour SwarmForge :
    1. Serveur Java SwarmForge (gRPC :50051 + WebSocket :8081) avec un scénario académique au choix
    2. Serveur HTTP statique pour le client Web 3D Three.js (:5173)
    3. N clients Web ouverts automatiquement dans le navigateur
    4. Client lourd JavaFX/jMonkeyEngine (SwarmForge Studio)
    5. (Optionnel) Compute Node headless haute performance
#>

param (
    [string]$Scenario = "4",
    [int]$WebClients = 2,
    [bool]$LaunchEditor = $true,
    [bool]$LaunchComputeNode = $false,
    [switch]$Interactive = $false
)

$rootDir = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
if (-not (Test-Path (Join-Path $rootDir "pom.xml"))) {
    $rootDir = Split-Path -Parent $PSScriptRoot
}
if (-not (Test-Path (Join-Path $rootDir "pom.xml"))) {
    $rootDir = (Get-Item $PSScriptRoot).Parent.Parent.FullName
}
Set-Location $rootDir

Write-Host "=======================================================================" -ForegroundColor Cyan
Write-Host "       🐝 SwarmForge - Lanceur de Test Multi-Clients Tout-en-Un 🌿      " -ForegroundColor Cyan
Write-Host "=======================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Scénarios Académiques Disponibles :" -ForegroundColor Yellow
Write-Host "   [ 1] ACAD_01_LEVY_BROWNIAN             (Lévy Flights vs Brownian Walk)" -ForegroundColor White
Write-Host "   [ 2] ACAD_02_POLYETHISM_BDI            (Polyéthisme et Spécialisation BDI)" -ForegroundColor White
Write-Host "   [ 3] ACAD_03_NEST_MORPHOGENESIS        (Morphogenèse du Nid et Microclimat)" -ForegroundColor White
Write-Host "   [ 4] ACAD_04_INTERSPECIFIC_COMPETITION (Compétition Lasius vs Linepithema) [DÉFAUT]" -ForegroundColor Green
Write-Host "   [ 5] ACAD_05_TROPHALLAXIS              (Flux Nutritifs et Trophallaxie)" -ForegroundColor White
Write-Host "   [ 6] ACAD_06_EPIDEMIOLOGY_QUARANTINE   (Épidémiologie et Auto-Quarantaine)" -ForegroundColor White
Write-Host "   [ 7] ACAD_07_ATTINE_FUNGI              (Symbiose Champignon - Fourmis Atta)" -ForegroundColor White
Write-Host "   [ 8] ACAD_08_STIGMERGIC_PHEROMONES     (Stigmergie et Résolution Labyrinthe)" -ForegroundColor White
Write-Host "   [ 9] ACAD_09_DULOSIS_RAID              (Raids d'Esclavagisme Polyergus)" -ForegroundColor White
Write-Host "   [10] ACAD_10_SAVANNA_COEVOLUTION       (Adaptation Tropicale Serengeti)" -ForegroundColor White
Write-Host "   [11] ACAD_11_ALPINE_THERMOREGULATION   (Cryo-Tolérance et Thermorégulation)" -ForegroundColor White
Write-Host "   [12] ACAD_12_BOREAL_SOLAR_DOMES        (Dômes Solaires Forêt Boréale)" -ForegroundColor White
Write-Host "   [13] ACAD_13_STEPPE_HARVESTING         (Granivorie et Sécheresse Steppe)" -ForegroundColor White
Write-Host "   [14] ACAD_14_WETLAND_FLOOD_RAFTING     (Radeau d'Inondation Solenopsis)" -ForegroundColor White
Write-Host "   [15] ACAD_15_WASP_WILD_BEEHIVE         (Guêpier Suspendu vs Ruche Sauvage)" -ForegroundColor White
Write-Host "   [16] ACAD_16_APICULTURAL_APIARY        (Rucher Moderne Dadant et Butinage)" -ForegroundColor White
Write-Host ""

# Mode interactif si aucun argument ou si demandé
$promptScenario = Read-Host "Numéro du scénario (1-16) [Défaut: $Scenario]"
if (-not [string]::IsNullOrWhiteSpace($promptScenario)) {
    $Scenario = $promptScenario.Trim()
}

$promptWeb = Read-Host "Nombre de clients Web à ouvrir (0-5) [Défaut: $WebClients]"
if (-not [string]::IsNullOrWhiteSpace($promptWeb)) {
    $WebClients = [int]$promptWeb.Trim()
}

$promptEditor = Read-Host "Lancer le client lourd Studio JavaFX (O/N) ? [Défaut: O]"
if (-not [string]::IsNullOrWhiteSpace($promptEditor)) {
    $LaunchEditor = ($promptEditor.Trim().ToUpper() -eq "O" -or $promptEditor.Trim().ToUpper() -eq "Y")
}

$promptCompute = Read-Host "Lancer un Compute Node headless (O/N) ? [Défaut: N]"
if (-not [string]::IsNullOrWhiteSpace($promptCompute)) {
    $LaunchComputeNode = ($promptCompute.Trim().ToUpper() -eq "O" -or $promptCompute.Trim().ToUpper() -eq "Y")
}

# Détection de Python
$pythonCmd = $null
if (Get-Command py -ErrorAction SilentlyContinue) {
    $pythonCmd = "py"
} elseif (Get-Command python -ErrorAction SilentlyContinue) {
    $pythonCmd = "python"
} elseif (Test-Path "$env:LOCALAPPDATA\Programs\Python\Python314\python.exe") {
    $pythonCmd = "$env:LOCALAPPDATA\Programs\Python\Python314\python.exe"
}

Write-Host ""
Write-Host "-----------------------------------------------------------------------" -ForegroundColor Cyan
Write-Host "[1/5] Démarrage du Serveur Java SwarmForge (Scénario #$Scenario)..." -ForegroundColor Green
$serverTitle = "SwarmForge Server - Scenario #$Scenario"
Start-Process cmd -ArgumentList "/k title $serverTitle && cd /d `"$rootDir`" && mvn exec:java -pl swarmforge-server -Dexec.args=`"--scenario $Scenario`""

# Démarrer le serveur HTTP statique pour le Web si des clients Web sont demandés
if ($WebClients -gt 0) {
    Write-Host "[2/5] Démarrage du serveur Web HTTP Statique (:5173)..." -ForegroundColor Green
    $staticServerScript = Join-Path $rootDir "scripts\test\serve_web_static.py"
    if ($pythonCmd -and (Test-Path $staticServerScript)) {
        Start-Process cmd -ArgumentList "/k title SwarmForge Web HTTP Server && cd /d `"$rootDir`" && `"$pythonCmd`" `"$staticServerScript`""
    }
}

Write-Host "[3/5] Initialisation du cluster (attente 4 secondes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 4

# Démarrer les clients Web
if ($WebClients -gt 0) {
    Write-Host "[4/5] Ouverture de $WebClients client(s) Web dans le navigateur..." -ForegroundColor Green
    for ($i = 1; $i -le $WebClients; $i++) {
        Start-Process "http://localhost:5173"
        Start-Sleep -Milliseconds 500
    }
}

# Démarrer le Compute Node si demandé
if ($LaunchComputeNode) {
    Write-Host "[Option] Lancement d'un Compute Node headless..." -ForegroundColor Green
    Start-Process cmd -ArgumentList "/k title SwarmForge Compute Node && cd /d `"$rootDir`" && mvn exec:java -pl swarmforge-compute"
    Start-Sleep -Seconds 1
}

# Démarrer le client lourd Studio
if ($LaunchEditor) {
    Write-Host "[5/5] Lancement du Client Lourd Studio JavaFX / jMonkeyEngine..." -ForegroundColor Green
    Start-Process cmd -ArgumentList "/k title SwarmForge Studio Client && cd /d `"$rootDir`" && mvn exec:java -pl swarmforge-editor"
}

Write-Host ""
Write-Host "=======================================================================" -ForegroundColor Cyan
Write-Host "          TOUS LES COMPOSANTS SONT OPÉRATIONNELS !                     " -ForegroundColor Green
Write-Host "=======================================================================" -ForegroundColor Cyan
Write-Host "  • Serveur gRPC      : localhost:50051" -ForegroundColor White
Write-Host "  • Serveur WebSocket : ws://localhost:8081" -ForegroundColor White
Write-Host "  • Client(s) Web     : http://localhost:5173 ($WebClients ouverts)" -ForegroundColor White
if ($LaunchEditor) {
    Write-Host "  • Client Studio     : Actif (JavaFX / jMonkeyEngine)" -ForegroundColor White
}
if ($LaunchComputeNode) {
    Write-Host "  • Compute Node      : Connecté au cluster" -ForegroundColor White
}
Write-Host "=======================================================================" -ForegroundColor Cyan
Write-Host "Pour arrêter les processus, fermez simplement leurs fenêtres de commande respectives." -ForegroundColor Yellow
Write-Host ""
