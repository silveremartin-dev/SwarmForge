# ==============================================================================
# SwarmForge — Fetch Batch Simulation Results from GCS (Windows PowerShell)
#
# Usage:
#   .\scripts\gcp\batch\fetch-batch-results.ps1 -JobName <name> [-LocalDir <path>] [-ProjectId <id>]
# ==============================================================================

param(
    [Parameter(Mandatory=$true)]
    [string]$JobName,
    [string]$LocalDir  = ".\saves\batch\$JobName",
    [string]$ProjectId = "swarmforge-509813"
)

$ErrorActionPreference = 'Stop'
$GcsPath = "gs://swarmforge-simulations/$JobName/"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   SWARMFORGE — FETCH BATCH RESULTS FROM GCS              " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Job Name   : $JobName"
Write-Host "  GCS Source : $GcsPath"
Write-Host "  Local Dest : $LocalDir"
Write-Host "----------------------------------------------------------"

New-Item -ItemType Directory -Force -Path $LocalDir | Out-Null

Write-Host "Downloading results..." -ForegroundColor Yellow
gsutil -m rsync -r $GcsPath "$LocalDir\"

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "✅ Results downloaded to: $LocalDir" -ForegroundColor Green
Get-ChildItem $LocalDir -ErrorAction SilentlyContinue | Format-Table Name, Length, LastWriteTime
Write-Host "==========================================================" -ForegroundColor Green
