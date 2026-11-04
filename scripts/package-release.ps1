<#
.SYNOPSIS
    SwarmForge - Standalone Release Packager (Windows / Cross-Platform PowerShell)
    Builds and packages SwarmForge distribution bundles (.zip) with checksums.
#>
[CmdletBinding()]
param(
    [Parameter(Position=0)]
    [string]$Platform = "windows",

    [Parameter()]
    [string]$Version = "1.0.0-beta.3",

    [Parameter()]
    [switch]$SkipBuild,

    [Parameter()]
    [switch]$Publish
)

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = (Resolve-Path "$ScriptDir\..").Path
Set-Location $RepoRoot

Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host "   🚀 SwarmForge v$Version - Release Packager (Platform: $Platform)" -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan

if (-not $SkipBuild) {
    Write-Host "`n[1/4] Building Maven modules..." -ForegroundColor Yellow
    mvn clean install -pl swarmforge-editor,swarmforge-server,swarmforge-client,swarmforge-compute,swarmforge-plugins -am -DskipTests -q
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Maven compilation failed."
        exit $LASTEXITCODE
    }

    Write-Host "`n[2/4] Resolving runtime dependencies..." -ForegroundColor Yellow
    mvn dependency:copy-dependencies -pl swarmforge-editor,swarmforge-server,swarmforge-client,swarmforge-compute -am -DincludeScope=runtime -DoutputDirectory=target/libs -q
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to copy dependencies."
        exit $LASTEXITCODE
    }
} else {
    Write-Host "`n[SKIP] Skipping Maven build (-SkipBuild specified)." -ForegroundColor Gray
}

$DistDir = "$RepoRoot\dist"
$ReleaseDir = "$DistDir\release"
$StagingDir = "$DistDir\staging\windows-bundle"

if (Test-Path $StagingDir) { Remove-Item -Recurse -Force $StagingDir }
New-Item -ItemType Directory -Force -Path "$StagingDir\lib" | Out-Null
New-Item -ItemType Directory -Force -Path $ReleaseDir | Out-Null

Write-Host "`n[3/4] Assembling release distribution bundle..." -ForegroundColor Yellow

# Copy module jars
$modules = @("server", "editor", "client", "compute", "core", "plugins")
foreach ($mod in $modules) {
    $targetDir = "$RepoRoot\swarmforge-$mod\target"
    if (Test-Path $targetDir) {
        $jar = Get-ChildItem -Path $targetDir -Filter "swarmforge-$mod-*.jar" | 
               Where-Object { $_.Name -notmatch "original|sources|javadoc" } | 
               Select-Object -First 1
        if ($jar) {
            Copy-Item $jar.FullName "$StagingDir\swarmforge-$mod.jar" -Force
            Write-Host "  -> Bundled swarmforge-$mod.jar" -ForegroundColor Green
        }
    }
}

# Copy third-party runtime dependencies
Get-ChildItem -Path $RepoRoot -Directory -Recurse -Filter "libs" | 
    Where-Object { $_.FullName -match "target\\libs$" } | 
    ForEach-Object {
        Get-ChildItem -Path $_.FullName -Filter "*.jar" | ForEach-Object {
            Copy-Item $_.FullName "$StagingDir\lib\$($_.Name)" -Force
        }
    }

# Copy sample nests, resources, licenses
if (Test-Path "$RepoRoot\samplenests") { Copy-Item -Recurse "$RepoRoot\samplenests" "$StagingDir\" -Force }
if (Test-Path "$RepoRoot\Dockerfile") { Copy-Item "$RepoRoot\Dockerfile" "$StagingDir\" -Force }
if (Test-Path "$RepoRoot\docker-compose.yml") { Copy-Item "$RepoRoot\docker-compose.yml" "$StagingDir\" -Force }
if (Test-Path "$RepoRoot\envoy.yaml") { Copy-Item "$RepoRoot\envoy.yaml" "$StagingDir\" -Force }
if (Test-Path "$RepoRoot\prometheus.yml") { Copy-Item "$RepoRoot\prometheus.yml" "$StagingDir\" -Force }
if (Test-Path "$RepoRoot\LICENSE") { Copy-Item "$RepoRoot\LICENSE" "$StagingDir\" -Force }
if (Test-Path "$RepoRoot\README.md") { Copy-Item "$RepoRoot\README.md" "$StagingDir\" -Force }

# Copy launcher scripts from scripts/run
New-Item -ItemType Directory -Force -Path "$StagingDir\scripts\run" | Out-Null
Copy-Item "$RepoRoot\scripts\run\*.bat" "$StagingDir\scripts\run\" -Force 2>$null
Copy-Item "$RepoRoot\scripts\run\*.sh" "$StagingDir\scripts\run\" -Force 2>$null

# Root launchers for convenience
Set-Content -Path "$StagingDir\run-editor.bat" -Value "@echo off`r`ntitle SwarmForge Studio Editor`r`ncd /d `"%~dp0`"`r`njava -Xmx4g --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.vector -cp `"swarmforge-editor.jar;swarmforge-core.jar;swarmforge-server.jar;lib/*`" org.swarmforge.client.Launcher %*`r`npause`r`n"
Set-Content -Path "$StagingDir\run-server.bat" -Value "@echo off`r`ntitle SwarmForge Dedicated Server`r`ncd /d `"%~dp0`"`r`njava -Xmx4g --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.vector -cp `"swarmforge-server.jar;swarmforge-core.jar;lib/*`" org.swarmforge.server.SwarmForgeServer %*`r`npause`r`n"
Set-Content -Path "$StagingDir\run-client.bat" -Value "@echo off`r`ntitle SwarmForge 3D Client Viewer`r`ncd /d `"%~dp0`"`r`njava -Xmx2g --enable-native-access=ALL-UNNAMED -cp `"swarmforge-client.jar;swarmforge-core.jar;lib/*`" org.swarmforge.client.ClientApp %*`r`npause`r`n"
Set-Content -Path "$StagingDir\run-computenode.bat" -Value "@echo off`r`ntitle SwarmForge Compute Node`r`ncd /d `"%~dp0`"`r`njava -Xmx4g --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.vector -cp `"swarmforge-compute.jar;swarmforge-core.jar;lib/*`" org.swarmforge.compute.ComputeNodeApp %*`r`npause`r`n"

Write-Host "`n[4/4] Creating zip release archive..." -ForegroundColor Yellow
$ZipName = "SwarmForge-v$Version-Windows-x64.zip"
$ZipPath = "$ReleaseDir\$ZipName"
if (Test-Path $ZipPath) { Remove-Item -Force $ZipPath }

Compress-Archive -Path "$StagingDir\*" -DestinationPath $ZipPath -CompressionLevel Optimal

# Checksum
$Checksum = (Get-FileHash -Path $ZipPath -Algorithm SHA256).Hash.ToLower()
"$Checksum  $ZipName" | Out-File -FilePath "$ReleaseDir\SHA256SUMS.txt" -Encoding utf8 -Append

Write-Host "`n==============================================================================" -ForegroundColor Green
Write-Host "   ✅ Release Package Generated Successfully!" -ForegroundColor Green
Write-Host "   • Archive (zip) : $ZipPath" -ForegroundColor Green
Write-Host "   • SHA-256       : $Checksum" -ForegroundColor Green
Write-Host "==============================================================================" -ForegroundColor Green
