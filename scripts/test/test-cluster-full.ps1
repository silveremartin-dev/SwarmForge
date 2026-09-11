<#
.SYNOPSIS
    SwarmForge - Test Cluster Complet : Serveur + Compute Node + Studio + Web
#>
param (
    [string]$Scenario = "4"
)

$rootDir = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
if (-not (Test-Path (Join-Path $rootDir "pom.xml"))) {
    $rootDir = Split-Path -Parent $PSScriptRoot
}
Set-Location $rootDir

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "  SwarmForge - Test Cluster Complet (Serveur + Node + Clients)   " -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan

Write-Host "[1/5] Démarrage du Serveur Java SwarmForge (Scénario #$Scenario)..." -ForegroundColor Green
Start-Process cmd -ArgumentList "/k title SwarmForge Server - Scenario #$Scenario && cd /d `"$rootDir`" && mvn exec:java -pl swarmforge-server -Dexec.args=`"--scenario $Scenario`""

$pythonCmd = if (Get-Command py -ErrorAction SilentlyContinue) { "py" } elseif (Get-Command python -ErrorAction SilentlyContinue) { "python" } else { "$env:LOCALAPPDATA\Programs\Python\Python314\python.exe" }
$staticScript = Join-Path $rootDir "scripts\test\serve_web_static.py"

Write-Host "[2/5] Démarrage du serveur Web HTTP (:5173)..." -ForegroundColor Green
Start-Process cmd -ArgumentList "/k title SwarmForge Web HTTP && cd /d `"$rootDir`" && `"$pythonCmd`" `"$staticScript`""

Write-Host "[3/5] Attente d'initialisation du serveur (4 secondes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 4

Write-Host "[4/5] Connexion d'un Compute Node headless..." -ForegroundColor Green
Start-Process cmd -ArgumentList "/k title SwarmForge Compute Node && cd /d `"$rootDir`" && mvn exec:java -pl swarmforge-compute"

Write-Host "[5/5] Lancement des clients Web et Studio JavaFX..." -ForegroundColor Green
Start-Process "http://localhost:5173"
Start-Process cmd -ArgumentList "/k title SwarmForge Studio Client && cd /d `"$rootDir`" && mvn exec:java -pl swarmforge-editor"

Write-Host ""
Write-Host "✓ Cluster complet démarré avec succès !" -ForegroundColor Green
Write-Host "  - Serveur Master    : :50051 (gRPC), :8081 (WebSocket)"
Write-Host "  - Compute Node      : Headless worker connecté"
Write-Host "  - Client Web        : http://localhost:5173"
Write-Host "  - Client Studio     : JavaFX / jMonkeyEngine"
