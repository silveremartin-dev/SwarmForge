<#
.SYNOPSIS
    SwarmForge - Lanceur Universel Serveur Java + Scénarios Académiques + Client Web (PowerShell)
#>
param (
    [string]$Scenario = "4"
)

$rootDir = Split-Path -Parent $PSScriptRoot
Set-Location $rootDir

Write-Host "=======================================================================" -ForegroundColor Cyan
Write-Host "          SwarmForge - Serveur Java et Scénarios Académiques           " -ForegroundColor Cyan
Write-Host "=======================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Choisissez un scénario de recherche parmi les presets ci-dessous :"
Write-Host ""
Write-Host "  [ 1] ACAD_01_LEVY_BROWNIAN             (Lévy Flights vs Brownian Walk)" -ForegroundColor White
Write-Host "  [ 2] ACAD_02_POLYETHISM_BDI            (Polyéthisme et Spécialisation BDI)" -ForegroundColor White
Write-Host "  [ 3] ACAD_03_NEST_MORPHOGENESIS        (Morphogenèse du Nid et Microclimat)" -ForegroundColor White
Write-Host "  [ 4] ACAD_04_INTERSPECIFIC_COMPETITION (Compétition Lasius vs Linepithema) [DÉFAUT]" -ForegroundColor Yellow
Write-Host "  [ 5] ACAD_05_TROPHALLAXIS              (Flux Nutritifs et Trophallaxie)" -ForegroundColor White
Write-Host "  [ 6] ACAD_06_EPIDEMIOLOGY_QUARANTINE   (Épidémiologie et Auto-Quarantaine)" -ForegroundColor White
Write-Host "  [ 7] ACAD_07_ATTINE_FUNGI              (Symbiose Champignon - Fourmis Atta)" -ForegroundColor White
Write-Host "  [ 8] ACAD_08_STIGMERGIC_PHEROMONES     (Stigmergie et Résolution de Labyrinthe)" -ForegroundColor White
Write-Host "  [ 9] ACAD_09_DULOSIS_RAID              (Raids d'Esclavagisme Polyergus vs Formica)" -ForegroundColor White
Write-Host "  [10] ACAD_10_SAVANNA_COEVOLUTION       (Adaptation Tropicale Savane Serengeti)" -ForegroundColor White
Write-Host "  [11] ACAD_11_ALPINE_THERMOREGULATION   (Cryo-Tolérance et Thermorégulation Alpine)" -ForegroundColor White
Write-Host "  [12] ACAD_12_BOREAL_SOLAR_DOMES        (Dômes d'Épines et Chaleur Solaire Taïga)" -ForegroundColor White
Write-Host "  [13] ACAD_13_STEPPE_HARVESTING         (Granivorie et Résistance Sécheresse Steppe)" -ForegroundColor White
Write-Host "  [14] ACAD_14_WETLAND_FLOOD_RAFTING     (Radeau d'Inondation Solenopsis)" -ForegroundColor White
Write-Host "  [15] ACAD_15_WASP_WILD_BEEHIVE         (Guêpier Suspendu vs Ruche Sauvage)" -ForegroundColor White
Write-Host "  [16] ACAD_16_APICULTURAL_APIARY        (Rucher Moderne Dadant et Butinage)" -ForegroundColor White
Write-Host ""

$inputScenario = Read-Host "Entrez le numéro du scénario (1-16) [Défaut: 4]"
if (-not [string]::IsNullOrWhiteSpace($inputScenario)) {
    $Scenario = $inputScenario
}

Write-Host ""
Write-Host "[1/2] Démarrage du serveur Java SwarmForge (Scénario #$Scenario)..." -ForegroundColor Green
Start-Process cmd -ArgumentList "/c mvn exec:java -pl swarmforge-server -Dexec.args=`"--scenario $Scenario`""

Write-Host "[2/2] Ouverture du Client Web 3D (Chrome)..." -ForegroundColor Green
Start-Sleep -Seconds 3
Start-Process "http://localhost:5173"

Write-Host ""
Write-Host "=======================================================================" -ForegroundColor Cyan
Write-Host " Serveur Java actif (:50051 gRPC, :8081 WebSocket)" -ForegroundColor Green
Write-Host " Client Web connecté sur http://localhost:5173" -ForegroundColor Green
Write-Host "=======================================================================" -ForegroundColor Cyan
