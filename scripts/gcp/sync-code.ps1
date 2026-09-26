# ==============================================================================
# SwarmForge - Sync Local Code to GCP VM (Windows PowerShell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================

$Project = "swarmforge-509813"
$Zone = "europe-west1-b"
$Instance = "swarmforge-vm"
$Archive = "code_sync.tar.gz"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   Synchronisation du code local vers la VM GCP...        " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Archive (Java backend only for cloud builds — web is pre-built in Docker)
Write-Host "[1/3] Empaquetage des sources Java backend..." -ForegroundColor Yellow

if (Test-Path $Archive) { Remove-Item $Archive -Force }
tar -czf $Archive `
    --exclude="*/target" --exclude="target" `
    --exclude="*/node_modules" --exclude="node_modules" `
    --exclude="*/.git" --exclude=".git" `
    --exclude="*/javadoc" --exclude="javadoc" `
    --exclude="*/captures" `
    --exclude="*.zip" --exclude="*.exe" --exclude="*.jar" `
    swarmforge-core swarmforge-server swarmforge-compute swarmforge-benchmarks swarmforge-plugins `
    pom.xml docker-compose.yml deploy-gcp.sh scripts docs

$SizeKb = [math]::Round((Get-Item $Archive).Length / 1KB, 1)
Write-Host "Taille archive : $SizeKb KB" -ForegroundColor Cyan

# 2. SCP
Write-Host "[2/3] Transfert vers la VM..." -ForegroundColor Yellow
$RemoteDest = "${Instance}:/tmp/${Archive}"
gcloud compute scp --quiet $Archive $RemoteDest --project=$Project --zone=$Zone

# 3. Unpack
Write-Host "[3/3] Decompression sur la VM..." -ForegroundColor Yellow
$UnpackCmd = "sudo rm -rf ~/swarmforge && mkdir -p ~/swarmforge && tar -xzf /tmp/$Archive -C ~/swarmforge && rm -f /tmp/$Archive"
gcloud compute ssh --quiet $Instance --project=$Project --zone=$Zone --command="$UnpackCmd"

# Clean local
Remove-Item -Path $Archive -Force -ErrorAction SilentlyContinue

Write-Host "==========================================================" -ForegroundColor Green
Write-Host "   Code synchronise avec succes sur la VM GCP !           " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
