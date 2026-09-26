# ==============================================================================
# SwarmForge - One-Click Sync & Hot-Reload Rebuild on GCP (Windows PowerShell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================
param (
    [string]$Service = ""
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   ⚡ SWARMFORGE : SYNC LOCAL CODE + CLUSTER HOT REBUILD  " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Sync
& "$PSScriptRoot\sync-code.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Échec de la synchronisation." -ForegroundColor Red
    exit 1
}

# 2. Rebuild
& "$PSScriptRoot\rebuild-cloud.ps1" -Service $Service
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Échec du rebuild." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "   🎉 Déploiement et Rebuild réussis avec succès !        " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
