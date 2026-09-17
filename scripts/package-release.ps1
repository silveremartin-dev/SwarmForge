# SwarmForge Release & Standalone Packaging Script
# Automated packaging with jpackage (Embedded JRE, No Java prerequisite for end-users)
# Copyright (c) 2022-2026 Silvère Martin-Michiellot / SwarmForge

param (
    [string]$Version = "2.0.0",
    [string]$AppName = "SwarmForge",
    [string]$OutputDir = "",
    [string]$InstallDir = "",
    [switch]$SkipBuild = $false,
    [switch]$CreateMsi = $false
)

$ErrorActionPreference = "Stop"
$RepoRoot = Split-Path -Path $PSScriptRoot -Parent
Set-Location $RepoRoot

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "   SwarmForge v$Version - Autonomous Release Package Generator" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan

# 1. Check JDK & jpackage
$jpackage = "jpackage"
if (-not (Get-Command "jpackage" -ErrorAction SilentlyContinue)) {
    if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\jpackage.exe")) {
        $jpackage = "$env:JAVA_HOME\bin\jpackage.exe"
    } elseif (Test-Path "C:\Program Files\Java\jdk-25\bin\jpackage.exe") {
        $jpackage = "C:\Program Files\Java\jdk-25\bin\jpackage.exe"
    } elseif (Test-Path "C:\Program Files\Java\jdk-21\bin\jpackage.exe") {
        $jpackage = "C:\Program Files\Java\jdk-21\bin\jpackage.exe"
    } else {
        Write-Error "jpackage tool not found. Please ensure JDK 21+ is installed and configured in PATH or JAVA_HOME."
        exit 1
    }
}
Write-Host " [OK] Using jpackage: $jpackage" -ForegroundColor Green

# 2. Build Maven modules
if (-not $SkipBuild) {
    Write-Host "`n[1/6] Building Maven modules (Core, Server, Editor)..." -ForegroundColor Yellow
    mvn package -pl swarmforge-editor -am -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Maven build failed."
        exit 1
    }
    
    Write-Host "`n[2/6] Copying runtime dependencies..." -ForegroundColor Yellow
    mvn dependency:copy-dependencies -pl swarmforge-editor -DincludeScope=runtime -DoutputDirectory=target/libs
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to copy dependencies."
        exit 1
    }
} else {
    Write-Host "`n[SKIP] Skipping Maven compilation (--SkipBuild specified)." -ForegroundColor DarkGray
}

# 3. Prepare staging directories
$DistDir = Join-Path $RepoRoot "dist"
$ReleaseDir = Join-Path $DistDir "release"
$StagingDir = Join-Path $DistDir "staging"
$StagingJars = Join-Path $StagingDir "jars"

if (Test-Path $StagingDir) { Remove-Item -Recurse -Force $StagingDir }
if (Test-Path $ReleaseDir) { Remove-Item -Recurse -Force $ReleaseDir }
New-Item -ItemType Directory -Force -Path $StagingJars | Out-Null
New-Item -ItemType Directory -Force -Path $ReleaseDir | Out-Null

Write-Host "`n[3/6] Staging runtime JARs..." -ForegroundColor Yellow
Copy-Item "swarmforge-editor/target/libs/*.jar" $StagingJars -Force

$editorJar = Get-ChildItem -Path "swarmforge-editor/target" -Filter "swarmforge-editor-*.jar" | Where-Object { $_.Name -notmatch "original" } | Select-Object -First 1
if (-not $editorJar) {
    Write-Error "Could not find built swarmforge-editor JAR in swarmforge-editor/target."
    exit 1
}
Copy-Item $editorJar.FullName $StagingJars -Force
$MainJarName = $editorJar.Name

$coreJar = Get-ChildItem -Path "swarmforge-core/target" -Filter "swarmforge-core-*.jar" -ErrorAction SilentlyContinue | Where-Object { $_.Name -notmatch "original" } | Select-Object -First 1
if ($coreJar) { Copy-Item $coreJar.FullName $StagingJars -Force }

$serverJar = Get-ChildItem -Path "swarmforge-server/target" -Filter "swarmforge-server-*.jar" -ErrorAction SilentlyContinue | Where-Object { $_.Name -notmatch "original" } | Select-Object -First 1
if ($serverJar) { Copy-Item $serverJar.FullName $StagingJars -Force }

# 4. Generate Windows .ico icon from source png
Write-Host "`n[4/6] Creating application icon..." -ForegroundColor Yellow
$PngIcon = Join-Path $RepoRoot "swarmforge-editor/src/main/resources/icons/icon.png"
$IcoIcon = Join-Path $StagingDir "icon.ico"
if (Test-Path $PngIcon) {
    try {
        Add-Type -AssemblyName System.Drawing
        $bmp = [System.Drawing.Bitmap]::FromFile($PngIcon)
        $thumb = New-Object System.Drawing.Bitmap($bmp, 256, 256)
        $iconHandle = $thumb.GetHicon()
        $icon = [System.Drawing.Icon]::FromHandle($iconHandle)
        $stream = New-Object System.IO.FileStream($IcoIcon, [System.IO.FileMode]::Create)
        $icon.Save($stream)
        $stream.Close()
        $icon.Dispose()
        $thumb.Dispose()
        $bmp.Dispose()
        Write-Host " [OK] Generated icon at: $IcoIcon" -ForegroundColor Green
    } catch {
        Write-Warning "Could not convert icon to ICO: $_"
    }
}

# 5. Invoke jpackage to create Standalone Windows App-Image
Write-Host "`n[5/6] Generating Standalone Native App with jpackage (Embedded JRE)..." -ForegroundColor Yellow
$AppImageOut = Join-Path $StagingDir "app-build"
if (Test-Path $AppImageOut) { Remove-Item -Recurse -Force $AppImageOut }

# jpackage requires purely numeric version format (e.g. 1.0.0)
$NumericVersion = ($Version -split '-')[0]
if ($NumericVersion -notmatch '^\d+(\.\d+)*$') {
    $NumericVersion = "1.0.0"
}

$jpackageArgs = @(
    "--name", $AppName,
    "--app-version", $NumericVersion,
    "--input", $StagingJars,
    "--main-jar", $MainJarName,
    "--main-class", "org.swarmforge.client.Launcher",
    "--type", "app-image",
    "--dest", $AppImageOut,
    "--java-options", "-Xmx4g",
    "--java-options", "-Dfile.encoding=UTF-8",
    "--java-options", "--enable-native-access=ALL-UNNAMED",
    "--vendor", "Silvere Martin-Michiellot",
    "--copyright", "Copyright (c) 2022-2026 Silvere Martin-Michiellot",
    "--description", "SwarmForge - Eusocial Insect Simulation & Research Studio"
)

if (Test-Path $IcoIcon) {
    $jpackageArgs += @("--icon", $IcoIcon)
}

& $jpackage $jpackageArgs
if ($LASTEXITCODE -ne 0) {
    Write-Error "jpackage failed to generate app-image."
    exit 1
}

$BundleRoot = Join-Path $AppImageOut $AppName

# Include essential resources in the bundle
Write-Host " -> Bundling sample nests, presets, documentation, and launchers..." -ForegroundColor Cyan
if (Test-Path "samplenests") {
    Copy-Item "samplenests" -Destination $BundleRoot -Recurse -Force
}
if (Test-Path "docs") {
    Copy-Item "docs" -Destination (Join-Path $BundleRoot "docs") -Recurse -Force
}
Copy-Item "README.md" -Destination $BundleRoot -Force
Copy-Item "LICENSE" -Destination $BundleRoot -Force
if (Test-Path $IcoIcon) {
    Copy-Item $IcoIcon -Destination $BundleRoot -Force
}

# Create Studio Console/Debug Launcher (.bat)
$BatStudioContent = @"
@echo off
title SwarmForge Simulation Studio
echo ========================================================
echo   SwarmForge v$Version - Simulation Studio Launcher
echo ========================================================
cd /d "%~dp0"
set "PATH=%~dp0runtime\bin;%PATH%"
"%~dp0SwarmForge.exe" %*
"@
Set-Content -Path (Join-Path $BundleRoot "SwarmForge-Studio.bat") -Value $BatStudioContent -Encoding ASCII

# Create Dedicated Server Launcher (.bat)
$BatServerContent = @"
@echo off
title SwarmForge Dedicated Server
echo ========================================================
echo   SwarmForge v$Version - Simulation gRPC Server
echo ========================================================
cd /d "%~dp0"
"%~dp0runtime\bin\java.exe" -Xmx4g --enable-native-access=ALL-UNNAMED -cp "app/*" org.swarmforge.server.SimulationServer %*
pause
"@
Set-Content -Path (Join-Path $BundleRoot "SwarmForge-Server.bat") -Value $BatServerContent -Encoding ASCII

# Create 1-Click Instant Installer / Shortcut Script
$InstallerPs1Content = @"
# SwarmForge 1-Click Desktop & Start Menu Shortcut Setup
`$TargetExe = Join-Path `$PSScriptRoot "SwarmForge.exe"
`$IconPath = Join-Path `$PSScriptRoot "icon.ico"
if (-not (Test-Path `$IconPath)) { `$IconPath = `$TargetExe }

`$WshShell = New-Object -ComObject WScript.Shell

# 1. Desktop Shortcut
`$DesktopPath = [System.Environment]::GetFolderPath([System.Environment+SpecialFolder]::Desktop)
`$ShortcutDesktop = `$WshShell.CreateShortcut((Join-Path `$DesktopPath "SwarmForge Studio.lnk"))
`$ShortcutDesktop.TargetPath = `$TargetExe
`$ShortcutDesktop.WorkingDirectory = `$PSScriptRoot
`$ShortcutDesktop.Description = "SwarmForge Eusocial Insect Simulation Studio"
`$ShortcutDesktop.IconLocation = "`$IconPath, 0"
`$ShortcutDesktop.Save()

# 2. Start Menu Shortcut
`$StartMenuPath = [System.Environment]::GetFolderPath([System.Environment+SpecialFolder]::Programs)
`$ShortcutStart = `$WshShell.CreateShortcut((Join-Path `$StartMenuPath "SwarmForge Studio.lnk"))
`$ShortcutStart.TargetPath = `$TargetExe
`$ShortcutStart.WorkingDirectory = `$PSScriptRoot
`$ShortcutStart.Description = "SwarmForge Eusocial Insect Simulation Studio"
`$ShortcutStart.IconLocation = "`$IconPath, 0"
`$ShortcutStart.Save()

Write-Host "=========================================================" -ForegroundColor Green
Write-Host " [SUCCESS] SwarmForge shortcuts installed on Desktop & Start Menu!" -ForegroundColor Green
Write-Host "=========================================================" -ForegroundColor Green
"@
Set-Content -Path (Join-Path $BundleRoot "Install-Shortcuts.ps1") -Value $InstallerPs1Content -Encoding UTF8

$BatInstallContent = @"
@echo off
title SwarmForge Installer
echo Installing SwarmForge Shortcuts...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0Install-Shortcuts.ps1"
echo.
echo Ready! You can now start SwarmForge from your Desktop or by running SwarmForge.exe
pause
"@
Set-Content -Path (Join-Path $BundleRoot "Install-Shortcuts.bat") -Value $BatInstallContent -Encoding ASCII

# 6. Compress and create release archives
Write-Host "`n[6/6] Generating Release Archives and Checksums..." -ForegroundColor Yellow

$ZipStandaloneName = "SwarmForge-v$Version-Windows-x64-Standalone.zip"
$ZipStandalonePath = Join-Path $ReleaseDir $ZipStandaloneName
Write-Host " -> Compressing $ZipStandaloneName..." -ForegroundColor Cyan

Compress-Archive -Path "$BundleRoot\*" -DestinationPath $ZipStandalonePath -CompressionLevel Optimal

# Create Dedicated Server Headless Package
$ServerStage = Join-Path $StagingDir "server-bundle"
New-Item -ItemType Directory -Force -Path (Join-Path $ServerStage "lib") | Out-Null
if ($serverJar) { Copy-Item $serverJar.FullName (Join-Path $ServerStage "swarmforge-server.jar") -Force }
if ($coreJar) { Copy-Item $coreJar.FullName (Join-Path $ServerStage "lib") -Force }
Copy-Item "swarmforge-editor/target/libs/*.jar" (Join-Path $ServerStage "lib") -Force
if (Test-Path "Dockerfile") { Copy-Item "Dockerfile" $ServerStage -Force }
if (Test-Path "docker-compose.yml") { Copy-Item "docker-compose.yml" $ServerStage -Force }
if (Test-Path "LICENSE") { Copy-Item "LICENSE" $ServerStage -Force }
if (Test-Path "samplenests") { Copy-Item "samplenests" $ServerStage -Recurse -Force }

$BatServerOnly = @"
@echo off
title SwarmForge Dedicated Server
cd /d "%~dp0"
java -Xmx4g -cp "swarmforge-server.jar;lib/*" org.swarmforge.server.SimulationServer %*
pause
"@
Set-Content -Path (Join-Path $ServerStage "run-server.bat") -Value $BatServerOnly -Encoding ASCII

$ShServerOnly = @'
#!/usr/bin/env bash
cd "$(dirname "$0")"
java -Xmx4g -cp "swarmforge-server.jar:lib/*" org.swarmforge.server.SimulationServer "$@"
'@
Set-Content -Path (Join-Path $ServerStage "run-server.sh") -Value $ShServerOnly -Encoding UTF8

$ZipServerName = "SwarmForge-v$Version-Server-CrossPlatform.zip"
$ZipServerPath = Join-Path $ReleaseDir $ZipServerName
Write-Host " -> Compressing $ZipServerName..." -ForegroundColor Cyan
Compress-Archive -Path "$ServerStage\*" -DestinationPath $ZipServerPath -CompressionLevel Optimal

# Generate Checksums
Write-Host " -> Calculating SHA256 checksums..." -ForegroundColor Cyan
$ChecksumFile = Join-Path $ReleaseDir "SHA256SUMS.txt"
$Checksums = @()
Get-ChildItem -Path $ReleaseDir -Filter "*.zip" | ForEach-Object {
    $hash = (Get-FileHash -Path $_.FullName -Algorithm SHA256).Hash.ToLower()
    $entry = "$hash  $($_.Name)"
    $Checksums += $entry
    Write-Host "    $entry" -ForegroundColor DarkCyan
}
$Checksums | Out-File -FilePath $ChecksumFile -Encoding utf8

# 7. Optional Custom Directory Export / Direct Install
if ($OutputDir) {
    Write-Host "`n[Export] Copying release packages to custom OutputDir: $OutputDir" -ForegroundColor Yellow
    if (-not (Test-Path $OutputDir)) {
        New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
    }
    Copy-Item (Join-Path $ReleaseDir "*") -Destination $OutputDir -Force
    Write-Host " [OK] Release archives copied to $OutputDir" -ForegroundColor Green
}

if ($InstallDir) {
    Write-Host "`n[Install] Installing standalone app directly to: $InstallDir" -ForegroundColor Yellow
    if (-not (Test-Path $InstallDir)) {
        New-Item -ItemType Directory -Force -Path $InstallDir | Out-Null
    }
    Copy-Item "$BundleRoot\*" -Destination $InstallDir -Recurse -Force
    Write-Host " [OK] Standalone SwarmForge installed at $InstallDir" -ForegroundColor Green
}

Write-Host "`n================================================================" -ForegroundColor Green
Write-Host " [COMPLETED] Release bundles successfully created in:" -ForegroundColor Green
Write-Host "  $ReleaseDir" -ForegroundColor White
if ($OutputDir) { Write-Host "  $OutputDir (Custom Export)" -ForegroundColor White }
if ($InstallDir) { Write-Host "  $InstallDir (Direct Installation)" -ForegroundColor White }
Write-Host "================================================================" -ForegroundColor Green
