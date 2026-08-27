# SwarmForge Asset Dispatcher Script
$editorRes = "swarmforge-editor/src/main/resources"
$webPublic = "swarmforge-web/public"

Write-Host "Creating target resource directories..."
New-Item -ItemType Directory -Force -Path "$editorRes/sounds" | Out-Null
New-Item -ItemType Directory -Force -Path "$editorRes/models" | Out-Null
New-Item -ItemType Directory -Force -Path "$editorRes/textures" | Out-Null
New-Item -ItemType Directory -Force -Path "$webPublic/sounds" | Out-Null
New-Item -ItemType Directory -Force -Path "$webPublic/3d" | Out-Null
New-Item -ItemType Directory -Force -Path "$webPublic/3d/textures" | Out-Null

# 1. Dispatch Audio Files
$audioMap = @{
    "assets/dragon-studio-dry-leaves-rustling-482874.mp3"     = "dry_leaves_rustling.mp3"
    "assets/freesound_community-ants-23656.mp3"               = "ant_colony_activity.mp3"
    "assets/freesound_community-sand-various-68938.mp3"        = "sand_soil_digging.mp3"
    "assets/liecio-strong-howling-wind-132281.mp3"            = "strong_howling_wind.mp3"
    "assets/loswin23-wind-gust-and-blowing-leaves-503905.mp3" = "wind_gust_leaves.mp3"
    "assets/storegraphic-soft-wind-leaves-316393.mp3"         = "soft_wind_leaves.mp3"
    "assets/the-sounds-of-an-anthill--ants-science.mp3"       = "anthill_nest_sounds.mp3"
    "assets/vectorby-strong-desert-wind-155416.mp3"           = "desert_wind_ambient.mp3"
}

foreach ($src in $audioMap.Keys) {
    $dstName = $audioMap[$src]
    if (Test-Path $src) {
        Copy-Item -Path $src -Destination "$editorRes/sounds/$dstName" -Force
        Copy-Item -Path $src -Destination "$webPublic/sounds/$dstName" -Force
        Write-Host "Dispatched Audio: $dstName"
    }
}

# Savanna splash audio
$splash = "assets/extracted/africa_savanna/African_Savanna_Source v1.0_Ready/game/art/sound/Africa_Audio/water_splash2.ogg"
if (Test-Path $splash) {
    Copy-Item -Path $splash -Destination "$editorRes/sounds/water_splash2.ogg" -Force
    Copy-Item -Path $splash -Destination "$webPublic/sounds/water_splash2.ogg" -Force
    Write-Host "Dispatched Audio: water_splash2.ogg"
}

# 2. Dispatch 3D Models
if (Test-Path "assets/cactus.obj") {
    Copy-Item -Path "assets/cactus.obj" -Destination "$editorRes/models/cactus.obj" -Force
    Copy-Item -Path "assets/cactus.obj" -Destination "$webPublic/3d/cactus.obj" -Force
    Write-Host "Dispatched Model: cactus.obj"
}

if (Test-Path "assets/extracted/bamboo/bamboo set .obj") {
    Copy-Item -Path "assets/extracted/bamboo/bamboo set .obj" -Destination "$editorRes/models/bamboo_set.obj" -Force
    Copy-Item -Path "assets/extracted/bamboo/bamboo set .obj" -Destination "$webPublic/3d/bamboo_set.obj" -Force
    Copy-Item -Path "assets/extracted/bamboo/bamboo set .mtl" -Destination "$editorRes/models/bamboo_set.mtl" -Force
    Copy-Item -Path "assets/extracted/bamboo/bamboo set .mtl" -Destination "$webPublic/3d/bamboo_set.mtl" -Force
    Write-Host "Dispatched Model: bamboo_set.obj/mtl"
}

if (Test-Path "assets/extracted/tropical_plants/Lowpoly tropical plant set.obj") {
    Copy-Item -Path "assets/extracted/tropical_plants/Lowpoly tropical plant set.obj" -Destination "$editorRes/models/tropical_plants.obj" -Force
    Copy-Item -Path "assets/extracted/tropical_plants/Lowpoly tropical plant set.obj" -Destination "$webPublic/3d/tropical_plants.obj" -Force
    Copy-Item -Path "assets/extracted/tropical_plants/Lowpoly tropical plant set.mtl" -Destination "$editorRes/models/tropical_plants.mtl" -Force
    Copy-Item -Path "assets/extracted/tropical_plants/Lowpoly tropical plant set.mtl" -Destination "$webPublic/3d/tropical_plants.mtl" -Force
    Write-Host "Dispatched Model: tropical_plants.obj/mtl"
}

$fbx = "assets/extracted/stylized_tropical/Stylized_Tropical_Pack/FBX/Stylized_Tropical_Pack_ALL.fbx"
if (Test-Path $fbx) {
    Copy-Item -Path $fbx -Destination "$editorRes/models/stylized_tropical_pack.fbx" -Force
    Copy-Item -Path $fbx -Destination "$webPublic/3d/stylized_tropical_pack.fbx" -Force
    Write-Host "Dispatched Model: stylized_tropical_pack.fbx"
}

# 3. Dispatch Textures
Get-ChildItem -Path "assets/extracted/bamboo/B textures" -Filter "*.png" -ErrorAction SilentlyContinue | ForEach-Object {
    Copy-Item -Path $_.FullName -Destination "$editorRes/textures/$($_.Name)" -Force
    Copy-Item -Path $_.FullName -Destination "$webPublic/3d/textures/$($_.Name)" -Force
}

Get-ChildItem -Path "assets/extracted/tropical_plants/Alpha texture" -Filter "*.png" -ErrorAction SilentlyContinue | ForEach-Object {
    Copy-Item -Path $_.FullName -Destination "$editorRes/textures/$($_.Name)" -Force
    Copy-Item -Path $_.FullName -Destination "$webPublic/3d/textures/$($_.Name)" -Force
}

Get-ChildItem -Path "assets/extracted/textures" -File -ErrorAction SilentlyContinue | ForEach-Object {
    Copy-Item -Path $_.FullName -Destination "$editorRes/textures/$($_.Name)" -Force
    Copy-Item -Path $_.FullName -Destination "$webPublic/3d/textures/$($_.Name)" -Force
}

Get-ChildItem -Path "assets/extracted/africa_savanna/African_Savanna_Source v1.0_Ready/game/art/terrains/AfricaPack" -File -ErrorAction SilentlyContinue | ForEach-Object {
    Copy-Item -Path $_.FullName -Destination "$editorRes/textures/$($_.Name)" -Force
    Copy-Item -Path $_.FullName -Destination "$webPublic/3d/textures/$($_.Name)" -Force
}

Write-Host "Dispatched textures successfully!"
