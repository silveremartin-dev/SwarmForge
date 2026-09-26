# ==============================================================================
# SwarmForge - Run Headless Scenario on Cloud Cluster (Windows PowerShell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================
param (
    [string]$Scenario = "1"
)

$PROJECT = "swarmforge-509813"
$ZONE = "europe-west1-b"
$INSTANCE = "swarmforge-vm"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   🐜 Lancement du Scénario #$Scenario sur le Cloud       " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$CMD = "cd ~/swarmforge && sudo SCENARIO=$Scenario docker compose up -d --no-deps server"

gcloud compute ssh --quiet $INSTANCE --project=$PROJECT --zone=$ZONE --command="$CMD"

Write-Host "✅ Scénario #$Scenario démarré et en cours de calcul !" -ForegroundColor Green
