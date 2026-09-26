# ==============================================================================
# SwarmForge — Submit a Google Cloud Batch Simulation Campaign (Windows PowerShell)
#
# Usage:
#   .\scripts\gcp\batch\submit-batch-job.ps1 `
#       [-Scenario <id|1-16>] [-Ticks <n>] [-Workers <n>] `
#       [-MachineType <type>] [-UseSpot <bool>] [-ProjectId <id>]
# ==============================================================================

param(
    [string]$Scenario    = "1",
    [int]   $Ticks       = 2000,
    [int]   $Workers     = 1,
    [string]$MachineType = "e2-standard-4",
    [bool]  $UseSpot     = $true,
    [string]$ProjectId   = "swarmforge-509813",
    [string]$Region      = "europe-west1",
    [string]$ImageTag    = "latest"
)

$ErrorActionPreference = 'Stop'

$GcsBucket   = "swarmforge-simulations"
$Image       = "${Region}-docker.pkg.dev/${ProjectId}/swarmforge-registry/swarmforge-server:${ImageTag}"
$SaEmail     = "swarmforge-batch-runner@${ProjectId}.iam.gserviceaccount.com"

# Generate unique job name (lowercase, alphanumeric + hyphens only)
$ScenarioSlug = $Scenario.ToLower() -replace '[^a-z0-9]', '-'
$ScenarioSlug = $ScenarioSlug.Substring(0, [Math]::Min(15, $ScenarioSlug.Length))
$Timestamp    = (Get-Date -Format "yyyyMMdd-HHmmss")
$JobName      = "sf-${ScenarioSlug}-${Timestamp}"

$ProvisioningModel = if ($UseSpot) { "SPOT" } else { "STANDARD" }

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   SWARMFORGE — SUBMITTING CLOUD BATCH JOB                " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Job Name     : $JobName"
Write-Host "  Scenario     : $Scenario"
Write-Host "  Ticks        : $Ticks"
Write-Host "  Task replicas: $Workers"
Write-Host "  Machine Type : $MachineType"
Write-Host "  Spot/Preempt : $UseSpot"
Write-Host "  Image        : $Image"
Write-Host "  Results GCS  : gs://$GcsBucket/$JobName/"
Write-Host "----------------------------------------------------------"

# ── Build Cloud Batch job JSON spec ──────────────────────────────────────────
$jobSpec = @"
{
  "taskGroups": [
    {
      "taskSpec": {
        "runnables": [
          {
            "container": {
              "imageUri": "$Image",
              "entrypoint": "/bin/sh",
              "commands": ["-c",
                "java -Xms2g -Xmx12g -XX:+UseG1GC -jar app.jar --headless --batch --scenario=\${SCENARIO} --ticks=\${TICKS} --export-dir=/app/saves/batch && gsutil -m rsync -r /app/saves/batch/ gs://$GcsBucket/$JobName/task-\${BATCH_TASK_INDEX}/"
              ]
            },
            "environment": {
              "variables": {
                "MODE": "batch",
                "SCENARIO": "$Scenario",
                "TICKS": "$Ticks"
              }
            }
          }
        ],
        "computeResource": {
          "cpuMilli": 4000,
          "memoryMib": 14336
        },
        "maxRetryCount": 1,
        "maxRunDuration": "86400s"
      },
      "taskCount": $Workers,
      "parallelism": $Workers
    }
  ],
  "allocationPolicy": {
    "instances": [
      {
        "policy": {
          "machineType": "$MachineType",
          "provisioningModel": "$ProvisioningModel"
        }
      }
    ],
    "serviceAccount": { "email": "$SaEmail" },
    "location": { "allowedLocations": ["regions/$Region"] }
  },
  "logsPolicy": { "destination": "CLOUD_LOGGING" }
}
"@

# ── Write spec to temp file and submit ───────────────────────────────────────
Write-Host "[1/1] Submitting job '$JobName' to Cloud Batch..." -ForegroundColor Yellow
$tmpSpec = [System.IO.Path]::GetTempFileName() + ".json"
$jobSpec | Set-Content $tmpSpec -Encoding utf8

try {
    gcloud batch jobs submit $JobName `
        --project=$ProjectId `
        --location=$Region `
        --config=$tmpSpec
} finally {
    Remove-Item $tmpSpec -ErrorAction SilentlyContinue
}

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "✅ Cloud Batch job submitted successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "  Job Name : $JobName"
Write-Host "  Results  : gs://$GcsBucket/$JobName/"
Write-Host ""
Write-Host "Monitor progress:"
Write-Host "  .\scripts\gcp\batch\monitor-batch-job.ps1 -JobName $JobName"
Write-Host ""
Write-Host "Fetch results when complete:"
Write-Host "  .\scripts\gcp\batch\fetch-batch-results.ps1 -JobName $JobName"
Write-Host ""
Write-Host "GCP Console:"
Write-Host "  https://console.cloud.google.com/batch/jobs/${JobName}?project=${ProjectId}"
Write-Host "==========================================================" -ForegroundColor Green
