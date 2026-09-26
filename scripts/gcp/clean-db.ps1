# ==============================================================================
# SwarmForge - Clean / Reset Database & Redis on GCP VM (Windows PowerShell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================

$PROJECT = "swarmforge-509813"
$ZONE = "europe-west1-b"
$INSTANCE = "swarmforge-vm"

Write-Host "⚠️  Réinitialisation des données PostgreSQL & Redis sur le Cloud..." -ForegroundColor Yellow

$CMD = "cd ~/swarmforge && sudo docker compose down -v && sudo docker compose up -d"

gcloud compute ssh $INSTANCE --project=$PROJECT --zone=$ZONE --command="$CMD"

Write-Host "✅ Base de données et cache réinitialisés à neuf !" -ForegroundColor Green
