<#
.SYNOPSIS
    SwarmForge - Test Rapide : Serveur Java + 2 Clients Web
#>
param (
    [string]$Scenario = "4"
)

$rootDir = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
if (-not (Test-Path (Join-Path $rootDir "pom.xml"))) {
    $rootDir = Split-Path -Parent $PSScriptRoot
}
Set-Location $rootDir

Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "   SwarmForge - Test Serveur Java + 2 Clients Web    " -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "[1/3] Démarrage du Serveur Java SwarmForge (Scénario #$Scenario)..." -ForegroundColor Green
Start-Process cmd -ArgumentList "/k title SwarmForge Server - Scenario #$Scenario && cd /d `"$rootDir`" && mvn exec:java -pl swarmforge-server -Dexec.args=`"--scenario $Scenario`""

$pythonCmd = if (Get-Command py -ErrorAction SilentlyContinue) { "py" } elseif (Get-Command python -ErrorAction SilentlyContinue) { "python" } else { "$env:LOCALAPPDATA\Programs\Python\Python314\python.exe" }
$staticScript = Join-Path $rootDir "scripts\test\serve_web_static.py"

Write-Host "[2/3] Démarrage du serveur Web HTTP (:5173)..." -ForegroundColor Green
Start-Process cmd -ArgumentList "/k title SwarmForge Web HTTP && cd /d `"$rootDir`" && `"$pythonCmd`" `"$staticScript`""

Write-Host "[3/3] Initialisation et ouverture de 2 onglets Web..." -ForegroundColor Green
Start-Sleep -Seconds 4
Start-Process "http://localhost:5173"
Start-Sleep -Milliseconds 500
Start-Process "http://localhost:5173"

Write-Host ""
Write-Host "✓ Test démarré avec succès !" -ForegroundColor Green
Write-Host "  - Serveur WebSocket : ws://localhost:8081"
Write-Host "  - Client Web 1 & 2 : http://localhost:5173"
