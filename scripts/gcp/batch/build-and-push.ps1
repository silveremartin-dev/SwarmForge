# ==============================================================================
# SwarmForge — Build & Push Batch Simulation Engine Image (Windows PowerShell)
# ==============================================================================

param(
    [string]$ProjectId      = "swarmforge-509813",
    [string]$Region         = "europe-west1",
    [string]$RegistryName   = "swarmforge-registry",
    [string]$ImageTag       = "latest"
)

$ErrorActionPreference = 'Stop'
$ImageUri = "${Region}-docker.pkg.dev/${ProjectId}/${RegistryName}/swarmforge-server:${ImageTag}"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   SWARMFORGE — BUILD & PUSH BATCH DOCKER IMAGE           " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Target Image : $ImageUri"
Write-Host "----------------------------------------------------------"

# 1. Auth Docker to Artifact Registry
Write-Host "[1/3] Authenticating Docker to GCP Artifact Registry..." -ForegroundColor Yellow
gcloud auth configure-docker "${Region}-docker.pkg.dev" --quiet

# 2. Build Image
Write-Host "[2/3] Building Docker image for SwarmForge Server/Batch..." -ForegroundColor Yellow
docker build -f swarmforge-server/Dockerfile -t $ImageUri .

# 3. Push Image
Write-Host "[3/3] Pushing image to Artifact Registry..." -ForegroundColor Yellow
docker push $ImageUri

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "✅ Image pushed successfully: $ImageUri" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
