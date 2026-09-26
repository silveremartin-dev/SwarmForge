# ==============================================================================
# SwarmForge - Stop GCP VM (Windows PowerShell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================

$PROJECT = "swarmforge-509813"
$ZONE = "europe-west1-b"
$INSTANCE = "swarmforge-vm"

Write-Host "🛑 Arrêt de l'instance Google Cloud Compute '$INSTANCE'..." -ForegroundColor Yellow

gcloud compute instances stop $INSTANCE --project=$PROJECT --zone=$ZONE

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Instance '$INSTANCE' arrêtée avec succès !" -ForegroundColor Green
    Write-Host "💡 La facturation du processeur (vCPU) et de la mémoire (RAM) est maintenant suspendue." -ForegroundColor Cyan
} else {
    Write-Host "❌ Erreur lors de l'arrêt de l'instance." -ForegroundColor Red
}
