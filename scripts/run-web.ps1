<#
.SYNOPSIS
    SwarmForge Web Client & Integrated Live Server Launcher (PowerShell)
#>
param (
    [int]$Port = 5173
)

$rootDir = Split-Path -Parent $PSScriptRoot
Set-Location $rootDir

Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "      SwarmForge - Web Client & Serveur Simulation    " -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host ""

$pythonCmd = $null
if (Get-Command py -ErrorAction SilentlyContinue) {
    $pythonCmd = "py"
} elseif (Get-Command python -ErrorAction SilentlyContinue) {
    $pythonCmd = "python"
} elseif (Test-Path "$env:LOCALAPPDATA\Programs\Python\Python314\python.exe") {
    $pythonCmd = "$env:LOCALAPPDATA\Programs\Python\Python314\python.exe"
}

if ($pythonCmd) {
    Write-Host "[INFO] Démarrage du serveur SwarmForge complet (Web sur :$Port + WebSocket sur :8081)..." -ForegroundColor Green
    & $pythonCmd (Join-Path $rootDir "scripts\mock_server.py")
    exit
}

$hasNode = Get-Command node -ErrorAction SilentlyContinue
$hasNpm = Get-Command npm -ErrorAction SilentlyContinue

if ($hasNode -and $hasNpm) {
    Set-Location (Join-Path $rootDir "swarmforge-web")
    if (-not (Test-Path "node_modules")) {
        Write-Host "[INFO] Installation des dépendances NPM..." -ForegroundColor Yellow
        npm install
    }
    Write-Host "[INFO] Lancement du serveur Vite Dev..." -ForegroundColor Green
    npm run dev -- --host --port $Port --open
    exit
}

Write-Host "ERREUR: Ni Python ni Node.js n'ont été trouvés sur le système." -ForegroundColor Red
