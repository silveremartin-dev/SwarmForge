# ==============================================================================
# SwarmForge - Rebuild Docker Stack on GCP VM (Windows PowerShell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================
param (
    [string]$Service = ""
)

$Project = "swarmforge-509813"
$Zone = "europe-west1-b"
$Instance = "swarmforge-vm"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   Rebuild du cluster SwarmForge sur le Cloud...          " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

if ($Service -ne "") {
    Write-Host "Rebuild cible pour le service : $Service" -ForegroundColor Yellow
    $RemoteCommand = 'cd ~/swarmforge; sudo docker compose build ' + $Service + '; sudo docker compose up -d --no-deps ' + $Service
} else {
    Write-Host "Rebuild complet (Server + 2 Workers + Web + BD)..." -ForegroundColor Yellow
    $RemoteCommand = 'cd ~/swarmforge; sudo docker compose build; sudo docker compose up -d --scale compute=2'
}

gcloud compute ssh --quiet $Instance --project=$Project --zone=$Zone --command="$RemoteCommand"

Write-Host "==========================================================" -ForegroundColor Green
Write-Host "   Rebuild et redemarrage termines !                      " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
