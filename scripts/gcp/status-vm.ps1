# ==============================================================================
# SwarmForge - Check VM & Cluster Status (Windows PowerShell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================

$PROJECT = "swarmforge-509813"
$ZONE = "europe-west1-b"
$INSTANCE = "swarmforge-vm"

Write-Host "🔍 Vérification de l'état de l'instance '$INSTANCE'..." -ForegroundColor Yellow

$STATUS = (gcloud compute instances describe $INSTANCE --project=$PROJECT --zone=$ZONE --format="get(status)" 2>$null)

if (-not $STATUS) {
    Write-Host "❌ L'instance '$INSTANCE' n'existe pas ou n'est pas accessible." -ForegroundColor Red
    exit 1
}

Write-Host "• État de la VM : $STATUS" -ForegroundColor Cyan

if ($STATUS -eq "RUNNING") {
    $NAT_IP = (gcloud compute instances describe $INSTANCE --project=$PROJECT --zone=$ZONE --format="get(networkInterfaces[0].accessConfigs[0].natIP)").Trim()
    Write-Host "• IP Publique   : $NAT_IP" -ForegroundColor Green
    Write-Host ""
    Write-Host "Services SwarmForge disponibles :" -ForegroundColor Cyan
    Write-Host "  - gRPC Server        : $NAT_IP`:50051"
    Write-Host "  - Web Dashboard 3D   : http://$NAT_IP`:3000"
    Write-Host "  - REST API / Metrics : http://$NAT_IP`:8080"
    Write-Host ""
    Write-Host "Pour inspecter les conteneurs à distance :" -ForegroundColor Yellow
    Write-Host "  gcloud compute ssh $INSTANCE --project=$PROJECT --zone=$ZONE --command=""sudo docker compose ps"""
} else {
    Write-Host "💡 La VM est arrêtée. Utilisez .\scripts\gcp\start-vm.ps1 pour la réveiller." -ForegroundColor Yellow
}
