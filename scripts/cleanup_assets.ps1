# Cleanup assets directory after dispatching
if (Test-Path "assets/extracted") {
    Remove-Item -Recurse -Force "assets/extracted"
}

$files = Get-ChildItem -Path "assets" -File
foreach ($f in $files) {
    if ($f.Name -ne "README.md") {
        Remove-Item -Force $f.FullName
        Write-Host "Removed raw asset: $($f.Name)"
    }
}

Write-Host "Assets directory cleanup completed successfully."
