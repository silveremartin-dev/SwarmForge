# SwarmForge - Javadoc Generation Script (PowerShell)
# Generates aggregate and per-module Javadoc directly in /javadoc (without apidocs sub-folder)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = Split-Path -Parent $ScriptDir
Set-Location $RootDir

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Building SwarmForge Javadoc Documentation " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Run Maven Javadoc generation (per-module and aggregate)
Write-Host "[1/3] Running Maven javadoc:javadoc..." -ForegroundColor Yellow
mvn javadoc:javadoc -Dquiet=true

Write-Host "[2/3] Running Maven javadoc:aggregate..." -ForegroundColor Yellow
mvn javadoc:aggregate -Dquiet=true

# 2. Collect per-module Javadocs
$Modules = @("swarmforge-core", "swarmforge-server", "swarmforge-editor", "swarmforge-client", "swarmforge-compute", "swarmforge-benchmarks")
$TempModulesDir = Join-Path $RootDir "target\temp_module_javadocs"

if (Test-Path $TempModulesDir) {
    Remove-Item -Path $TempModulesDir -Recurse -Force
}
New-Item -ItemType Directory -Path $TempModulesDir -Force | Out-Null

foreach ($mod in $Modules) {
    $ModApidocs = Join-Path $RootDir "$mod\javadoc\apidocs"
    if (-not (Test-Path $ModApidocs)) {
        $ModApidocs = Join-Path $RootDir "$mod\target\site\apidocs"
    }
    if (Test-Path $ModApidocs) {
        $ModDest = Join-Path $TempModulesDir $mod
        Write-Host "Collecting Javadoc for module '$mod'..." -ForegroundColor Green
        New-Item -ItemType Directory -Path $ModDest -Force | Out-Null
        Copy-Item -Path "$ModApidocs\*" -Destination $ModDest -Recurse -Force
    }
}

# 3. Collect Aggregate Javadoc
$TempAggregateDir = Join-Path $RootDir "target\temp_aggregate_javadoc"
if (Test-Path $TempAggregateDir) {
    Remove-Item -Path $TempAggregateDir -Recurse -Force
}
New-Item -ItemType Directory -Path $TempAggregateDir -Force | Out-Null

$AggApidocs = Join-Path $RootDir "javadoc\apidocs"
if (-not (Test-Path $AggApidocs)) {
    $AggApidocs = Join-Path $RootDir "target\site\apidocs"
}

if (Test-Path $AggApidocs) {
    Write-Host "Collecting Aggregate Javadoc..." -ForegroundColor Green
    Copy-Item -Path "$AggApidocs\*" -Destination $TempAggregateDir -Recurse -Force
}

# 4. Clean root /javadoc directory and structure output without 'apidocs' subfolder
$FinalJavadocDir = Join-Path $RootDir "javadoc"
Write-Host "[3/3] Structuring output in $FinalJavadocDir (removing apidocs sub-level)..." -ForegroundColor Yellow

if (Test-Path $FinalJavadocDir) {
    Remove-Item -Path $FinalJavadocDir -Recurse -Force
}
New-Item -ItemType Directory -Path $FinalJavadocDir -Force | Out-Null

# Clean up module javadoc subdirectories in source trees
foreach ($mod in $Modules) {
    $ModJd = Join-Path $RootDir "$mod\javadoc"
    if (Test-Path $ModJd) {
        Remove-Item -Path $ModJd -Recurse -Force
    }
}

# Copy aggregate Javadoc files directly to /javadoc/ (e.g. /javadoc/index.html)
if (Test-Path "$TempAggregateDir\index.html") {
    Copy-Item -Path "$TempAggregateDir\*" -Destination $FinalJavadocDir -Recurse -Force
}

# Copy module Javadocs directly to /javadoc/<module-name>/
foreach ($mod in $Modules) {
    $SrcMod = Join-Path $TempModulesDir $mod
    if (Test-Path "$SrcMod\index.html") {
        $DestMod = Join-Path $FinalJavadocDir $mod
        Write-Host "Placing module Javadoc: $DestMod\index.html" -ForegroundColor Green
        New-Item -ItemType Directory -Path $DestMod -Force | Out-Null
        Copy-Item -Path "$SrcMod\*" -Destination $DestMod -Recurse -Force
    }
}

# Clean temp directories
Remove-Item -Path $TempModulesDir -Recurse -Force
Remove-Item -Path $TempAggregateDir -Recurse -Force

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Javadoc successfully generated! " -ForegroundColor Green
Write-Host " Aggregate: $FinalJavadocDir\index.html" -ForegroundColor Green
foreach ($mod in $Modules) {
    if (Test-Path "$FinalJavadocDir\$mod\index.html") {
        Write-Host " Module $mod`: $FinalJavadocDir\$mod\index.html" -ForegroundColor Green
    }
}
Write-Host "==========================================" -ForegroundColor Cyan
