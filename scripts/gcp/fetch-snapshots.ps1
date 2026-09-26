# ==============================================================================
# SwarmForge - Export & Download Snapshots from GCP to Local (Windows PowerShell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================
param (
    [string]$OutputFile = "snapshots_export.sql"
)

$PROJECT = "swarmforge-509813"
$ZONE = "europe-west1-b"
$INSTANCE = "swarmforge-vm"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   Exportation des snapshots depuis PostgreSQL GCP...     " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Export pg_dump on the remote VM
Write-Host "[1/2] Dump des tables checkpoints, worlds, colonies sur la VM..." -ForegroundColor Yellow
$DUMP_CMD = 'sudo docker exec swarmforge-postgres pg_dump -U swarmforge -d swarmforge -t checkpoints -t worlds -t colonies > /tmp/snapshots_export.sql'
gcloud compute ssh $INSTANCE --project=$PROJECT --zone=$ZONE --command="$DUMP_CMD"

# 2. SCP transfer to local machine
Write-Host "[2/2] Telechargement du fichier vers $OutputFile..." -ForegroundColor Yellow
gcloud compute scp "${INSTANCE}:/tmp/snapshots_export.sql" "$OutputFile" --project=$PROJECT --zone=$ZONE

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "==========================================================" -ForegroundColor Green
    Write-Host "   Snapshots rapatries avec succes dans $OutputFile !" -ForegroundColor Green
    Write-Host "==========================================================" -ForegroundColor Green
    Write-Host "Ce fichier peut etre importe dans votre base locale pour les replays deterministes." -ForegroundColor Cyan
} else {
    Write-Host "Erreur lors du rapatriement des snapshots." -ForegroundColor Red
}
