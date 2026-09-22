<#
.SYNOPSIS
    SwarmForge - Lanceur Combiné Serveur Java & Client Web (PowerShell)
    Démarre le serveur Java SwarmForge Server (gRPC :50051 / WebSocket :8081)
    et lance le client Web dans le navigateur.
#>
param (
    [int]$HttpPort = 5173,
    [switch]$NoGui
)

$rootDir = Split-Path -Parent $PSScriptRoot
Set-Location $rootDir

Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host "       SwarmForge - Démarrage Combiné Serveur Réel & Client Web               " -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host ""

# 1. Vérification de Maven & Java pour le serveur réel
$hasMvn = Get-Command mvn -ErrorAction SilentlyContinue

if ($hasMvn) {
    Write-Host "[1/2] Lancement du Serveur Java SwarmForge (:50051 gRPC / :8081 WebSocket)..." -ForegroundColor Green
    Start-Process cmd.exe -ArgumentList "/k cd /d `"$rootDir`" && mvn exec:java -pl swarmforge-server -Dexec.args=`"--local`""
    Start-Sleep -Seconds 2
} else {
    Write-Host "[AVERTISSEMENT] Maven n'a pas été détecté. Le serveur WebSocket autonome Python sera utilisé." -ForegroundColor Yellow
}

# 2. Lancement du Client Web
Write-Host "[2/2] Démarrage du Client Web SwarmForge sur le port $HttpPort..." -ForegroundColor Green
& (Join-Path $PSScriptRoot "run-web.ps1") -Port $HttpPort
