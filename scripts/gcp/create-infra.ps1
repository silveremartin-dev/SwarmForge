# ==============================================================================
# SwarmForge - Create GCP Infrastructure (Windows PowerShell)
# Project: swarmforge-509813 | Zone: europe-west1-b | Machine: c2-standard-4 / c3-standard-4
# ==============================================================================
param (
    [string]$MachineType = "c2-standard-4",
    [string]$InstanceName = "swarmforge-vm"
)

$PROJECT = "swarmforge-509813"
$ZONE = "europe-west1-b"
$INSTANCE = $InstanceName
$MACHINE_TYPE = $MachineType

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   🚀 Initialisation de l'infrastructure Google Cloud     " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Règle Pare-feu (Firewall)
Write-Host "[1/2] Configuration des règles de pare-feu..." -ForegroundColor Yellow
gcloud compute firewall-rules create allow-swarmforge `
    --project=$PROJECT `
    --allow="tcp:50051,tcp:8080,tcp:3000,tcp:8081" `
    --target-tags=swarmforge-node `
    --description="Autoriser le trafic SwarmForge gRPC, Web et REST"

# 2. Création de la VM
Write-Host "[2/2] Création de la machine virtuelle ($MACHINE_TYPE)..." -ForegroundColor Yellow
gcloud compute instances create $INSTANCE `
    --project=$PROJECT `
    --zone=$ZONE `
    --machine-type=$MACHINE_TYPE `
    --tags="swarmforge-node,http-server" `
    --image-family=ubuntu-2204-lts `
    --image-project=ubuntu-os-cloud `
    --boot-disk-size=30GB

if ($LASTEXITCODE -eq 0) {
    $NAT_IP = (gcloud compute instances describe $INSTANCE --project=$PROJECT --zone=$ZONE --format="get(networkInterfaces[0].accessConfigs[0].natIP)").Trim()
    Write-Host ""
    Write-Host "==========================================================" -ForegroundColor Green
    Write-Host "   ✅ VM Créée avec succès ! IP Publique : $NAT_IP" -ForegroundColor Green
    Write-Host "==========================================================" -ForegroundColor Green
    Write-Host "Pour vous connecter en SSH :" -ForegroundColor Cyan
    Write-Host "  gcloud compute ssh $INSTANCE --project=$PROJECT --zone=$ZONE"
} else {
    Write-Host "❌ Erreur lors de la création de la VM." -ForegroundColor Red
}
