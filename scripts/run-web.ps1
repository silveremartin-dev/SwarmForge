<#
.SYNOPSIS
    SwarmForge Web Client Launcher (PowerShell)
#>
param (
    [switch]$Static,
    [int]$Port = 0
)

$rootDir = Split-Path -Parent $PSScriptRoot
Set-Location $rootDir

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SwarmForge - Web Client Launcher" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$targetDir = if ($Static) { "swarmforge-web-client" } else { "swarmforge-web" }
if ($Port -eq 0) {
    $Port = if ($Static) { 8080 } else { 5173 }
}

$hasNode = Get-Command node -ErrorAction SilentlyContinue
$hasNpm = Get-Command npm -ErrorAction SilentlyContinue

if ($hasNode -and $hasNpm -and -not $Static) {
    Write-Host "[1/2] Checking dependencies for swarmforge-web..." -ForegroundColor Green
    Set-Location "swarmforge-web"
    if (-not (Test-Path "node_modules")) {
        Write-Host "[INFO] Installing NPM dependencies..." -ForegroundColor Yellow
        npm install
    }
    Write-Host "[2/2] Starting SwarmForge Web Client (Vite Dev Server)..." -ForegroundColor Green
    Write-Host "URL: http://localhost:$Port/" -ForegroundColor Cyan
    npm run dev -- --host --port $Port --open
    exit
}

$hasPython = Get-Command python -ErrorAction SilentlyContinue
if ($hasPython) {
    Write-Host "[INFO] Starting local Web server on port $Port using Python..." -ForegroundColor Green
    Write-Host "URL: http://localhost:$Port/" -ForegroundColor Cyan
    Start-Process "http://localhost:$Port"
    python -m http.server $Port --directory $targetDir
    exit
}

$hasPy = Get-Command py -ErrorAction SilentlyContinue
if ($hasPy) {
    Write-Host "[INFO] Starting local Web server on port $Port using Python Launcher..." -ForegroundColor Green
    Write-Host "URL: http://localhost:$Port/" -ForegroundColor Cyan
    Start-Process "http://localhost:$Port"
    py -m http.server $Port --directory $targetDir
    exit
}

Write-Host "[WARNING] Opening static web client directly..." -ForegroundColor Yellow
Start-Process "swarmforge-web-client\index.html"
