# ==============================================================================
# SwarmForge — Google Cloud Batch: Infrastructure Setup (Windows PowerShell)
#
# Creates all GCP resources required to run SwarmForge batch campaigns.
# Idempotent — safe to run multiple times.
#
# Usage:
#   .\scripts\gcp\batch\setup-gcp-batch-infra.ps1 [-ProjectId <id>] [-Region <region>]
# ==============================================================================

param(
    [string]$ProjectId      = "swarmforge-509813",
    [string]$Region         = "europe-west1",
    [string]$RegistryName   = "swarmforge-registry",
    [string]$GcsBucket      = "swarmforge-simulations",
    [string]$SaName         = "swarmforge-batch-runner"
)

$ErrorActionPreference = 'Stop'
$SaEmail = "${SaName}@${ProjectId}.iam.gserviceaccount.com"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   SWARMFORGE — GCP BATCH INFRASTRUCTURE SETUP            " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Project ID   : $ProjectId"
Write-Host "  Region       : $Region"
Write-Host "  Registry     : $RegistryName"
Write-Host "  GCS Bucket   : gs://$GcsBucket"
Write-Host "  Service Acct : $SaEmail"
Write-Host "----------------------------------------------------------"

# 1. Enable required APIs
Write-Host "[1/4] Enabling GCP Batch & Storage APIs..." -ForegroundColor Yellow
gcloud services enable `
    batch.googleapis.com `
    artifactregistry.googleapis.com `
    storage.googleapis.com `
    logging.googleapis.com `
    --project=$ProjectId
Write-Host "      APIs enabled." -ForegroundColor Green

# 2. Artifact Registry
Write-Host "[2/4] Configuring Artifact Registry repository '$RegistryName'..." -ForegroundColor Yellow
$repoExists = $null
try {
    gcloud artifacts repositories describe $RegistryName `
        --location=$Region --project=$ProjectId 2>$null | Out-Null
    $repoExists = $true
} catch { $repoExists = $false }

if (-not $repoExists) {
    gcloud artifacts repositories create $RegistryName `
        --repository-format=docker `
        --location=$Region `
        --project=$ProjectId `
        --description="SwarmForge simulation engine Docker images"
    Write-Host "      Repository created." -ForegroundColor Green
} else {
    Write-Host "      Repository already exists — OK." -ForegroundColor Green
}

# 3. GCS Bucket
Write-Host "[3/4] Configuring GCS bucket 'gs://$GcsBucket'..." -ForegroundColor Yellow
$bucketExists = $null
try {
    gsutil ls -b "gs://$GcsBucket" 2>$null | Out-Null
    $bucketExists = $true
} catch { $bucketExists = $false }

if (-not $bucketExists) {
    gsutil mb -p $ProjectId -l $Region -b on "gs://$GcsBucket"

    $lifecycle = @'
{"lifecycle":{"rule":[{"action":{"type":"Delete"},"condition":{"age":90}}]}}
'@
    $tmpFile = [System.IO.Path]::GetTempFileName()
    $lifecycle | Set-Content $tmpFile -Encoding utf8
    gsutil lifecycle set $tmpFile "gs://$GcsBucket"
    Remove-Item $tmpFile -ErrorAction SilentlyContinue

    Write-Host "      Bucket created with 90-day auto-delete lifecycle." -ForegroundColor Green
} else {
    Write-Host "      Bucket already exists — OK." -ForegroundColor Green
}

# 4. Service Account & IAM
Write-Host "[4/4] Configuring Service Account '$SaName'..." -ForegroundColor Yellow
$saExists = $null
try {
    gcloud iam service-accounts describe $SaEmail --project=$ProjectId 2>$null | Out-Null
    $saExists = $true
} catch { $saExists = $false }

if (-not $saExists) {
    gcloud iam service-accounts create $SaName `
        --project=$ProjectId `
        --display-name="SwarmForge Batch Runner" `
        --description="Service account for SwarmForge Cloud Batch simulation jobs"
    Write-Host "      Service account created." -ForegroundColor Green
} else {
    Write-Host "      Service account already exists — updating IAM." -ForegroundColor Green
}

$roles = @(
    "roles/batch.jobsEditor",
    "roles/storage.objectAdmin",
    "roles/artifactregistry.reader",
    "roles/logging.logWriter"
)
foreach ($role in $roles) {
    gcloud projects add-iam-policy-binding $ProjectId `
        --member="serviceAccount:$SaEmail" `
        --role=$role --quiet | Out-Null
}
Write-Host "      IAM Roles assigned." -ForegroundColor Green

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "✅ GCP Batch infrastructure is ready!" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:"
Write-Host "  1. Build & push Docker image:"
Write-Host "       .\scripts\gcp\batch\build-and-push.ps1 -ProjectId $ProjectId -Region $Region"
Write-Host ""
Write-Host "  2. Submit batch simulation job:"
Write-Host "       .\scripts\gcp\batch\submit-batch-job.ps1 -Scenario 1 -Ticks 5000"
Write-Host "==========================================================" -ForegroundColor Green
