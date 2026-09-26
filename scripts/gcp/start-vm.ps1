# ==============================================================================
# SwarmForge - Start GCP VM and get IP (Windows PowerShell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================

$PROJECT = "swarmforge-509813"
$ZONE = "europe-west1-b"
$INSTANCE = "swarmforge-vm"

Write-Host "🚀 Démarrage de l'instance Google Cloud '$INSTANCE'..." -ForegroundColor Yellow

gcloud compute instances start $INSTANCE --project=$PROJECT --zone=$ZONE

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Instance démarrée !" -ForegroundColor Green
    
    $NAT_IP = (gcloud compute instances describe $INSTANCE --project=$PROJECT --zone=$ZONE --format="get(networkInterfaces[0].accessConfigs[0].natIP)").Trim()
    
    Write-Host ""
    Write-Host "======================================================" -ForegroundColor Cyan
    Write-Host "   🌐 IP Publique SwarmForge : $NAT_IP" -ForegroundColor Green
    Write-Host "======================================================" -ForegroundColor Cyan
    Write-Host "  • gRPC Studio Stream : $NAT_IP`:50051"
    Write-Host "  • Web 3D Viewer      : http://$NAT_IP`:3000"
    Write-Host "  • REST API / Metrics : http://$NAT_IP`:8080"
    Write-Host "======================================================" -ForegroundColor Cyan
} else {
    Write-Host "❌ Erreur lors du démarrage de l'instance." -ForegroundColor Red
}
