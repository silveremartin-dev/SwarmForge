Get-ChildItem -Recurse -Include *.java,*.jsx,*.js -Exclude node_modules,target,.git | Where-Object { 
    $_.FullName -notmatch '\\(node_modules|target|\.git)\\' 
} | ForEach-Object { 
    $lines = (Get-Content $_.FullName | Measure-Object -Line).Lines
    if ($lines -gt 600) { 
        [PSCustomObject]@{ 
            Lines = $lines
            Path = $_.FullName.Substring((Get-Location).Path.Length + 1)
        } 
    } 
} | Sort-Object -Descending Lines | Format-Table -AutoSize
