# SwarmForge - Raw Assets Directory

This directory serves as the landing folder for incoming raw sound effects, 3D mesh files (`.obj`, `.fbx`, `.gltf`), and texture archives (`.png`, `.jpg`).

### 📦 Dispatched Target Directories:
- **Editor Audio Bank**: `swarmforge-editor/src/main/resources/sounds/`
- **Editor 3D Models**: `swarmforge-editor/src/main/resources/models/`
- **Editor Textures**: `swarmforge-editor/src/main/resources/textures/`
- **Web Audio Bank**: `swarmforge-web/public/sounds/`
- **Web 3D Assets**: `swarmforge-web/public/3d/`

To process and dispatch new assets, run:
```powershell
powershell -ExecutionPolicy Bypass -File scripts/dispatch_assets.ps1
powershell -ExecutionPolicy Bypass -File scripts/cleanup_assets.ps1
```
