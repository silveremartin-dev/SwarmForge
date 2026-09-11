<#
.SYNOPSIS
    SwarmForge - Test Hybride : Serveur Java + Client Web + Client Studio JavaFX
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
Write-Host "  SwarmForge - Test Hybride Serveur + Web + Studio Desktop       " -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan

Write-Host "[1/4] Démarrage du Serveur Java SwarmForge (Scénario #$Scenario)..." -ForegroundColor Green
Start-Process cmd -ArgumentList "/k title SwarmForge Server - Scenario #$Scenario && cd /d `"$rootDir`" && mvn exec:java -pl swarmforge-server -Dexec.args=`"--scenario $Scenario`""

$pythonCmd = if (Get-Command py -ErrorAction SilentlyContinue) { "py" } elseif (Get-Command python -ErrorAction SilentlyContinue) { "python" } else { "$env:LOCALAPPDATA\Programs\Python\Python314\python.exe" }
$staticScript = Join-Path $rootDir "scripts\test\serve_web_static.py"

Write-Host "[2/4] Démarrage du serveur Web HTTP (:5173)..." -ForegroundColor Green
Start-Process cmd -ArgumentList "/k title SwarmForge Web HTTP && cd /d `"$rootDir`" && `"$pythonCmd`" `"$staticScript`""

Write-Host "[3/4] Attente d'initialisation (4 secondes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 4

Write-Host "[4/4] Ouverture du Client Web et lancement du Client Studio JavaFX..." -ForegroundColor Green
Start-Process "http://localhost:5173"
Start-Process cmd -ArgumentList "/k title SwarmForge Studio Client && cd /d `"$rootDir`" && mvn exec:java -pl swarmforge-editor"

Write-Host ""
Write-Host "✓ Test Hybride démarré !" -ForegroundColor Green
Write-Host "  - Serveur gRPC      : localhost:50051"
Write-Host "  - Serveur WebSocket : ws://localhost:8081"
Write-Host "  - Client Web 3D     : http://localhost:5173"
Write-Host "  - Client Studio     : Fenêtre JavaFX / jMonkeyEngine"
