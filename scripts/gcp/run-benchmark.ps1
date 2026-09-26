# ==============================================================================
# SwarmForge - Run Comprehensive Benchmark on GCP VM (Windows PowerShell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================

$PROJECT = "swarmforge-509813"
$ZONE = "europe-west1-b"
$INSTANCE = "swarmforge-vm"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   Execution de la suite de Benchmarks sur GCP VM         " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

gcloud compute ssh $INSTANCE --project=$PROJECT --zone=$ZONE --command="bash ~/swarmforge/scripts/gcp/remote-run-benchmark.sh"
