# ==============================================================================
# SwarmForge - Live Logs Stream from GCP VM (Windows PowerShell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================
param (
    [string]$Service = "server"
)

$PROJECT = "swarmforge-509813"
$ZONE = "europe-west1-b"
$INSTANCE = "swarmforge-vm"

Write-Host "📜 Suivi des logs en temps réel pour '$Service' sur GCP..." -ForegroundColor Yellow
Write-Host "(Appuyez sur Ctrl+C pour quitter le flux de logs)" -ForegroundColor Cyan

$CMD = "cd ~/swarmforge && sudo docker compose logs -f --tail=100 $Service"

gcloud compute ssh $INSTANCE --project=$PROJECT --zone=$ZONE --command="$CMD"
