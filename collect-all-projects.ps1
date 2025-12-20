$RootDir = Get-Location
$OutputDir = Join-Path $RootDir "_collected_code"

# Расширения файлов
$IncludeExtensions = @(
    "*.js","*.jsx","*.ts","*.tsx",
    "*.java","*.kt",
    "*.html","*.css","*.scss",
    "*.json","*.xml",
    "*.yml","*.yaml",
    "*.properties","*.sql",
    "*.gradle","*.md","*.txt"
)

# Исключаемые директории
$ExcludeDirs = @(
    "node_modules","dist","build","target",
    ".git",".gradle",".mvn","out","coverage"
)

Write-Host "========================================" -ForegroundColor Green
Write-Host "   GLOBAL PROJECT CODE COLLECTOR"
Write-Host "========================================" -ForegroundColor Green

# Создание папки для результатов
if (Test-Path $OutputDir) {
    Remove-Item $OutputDir -Recurse -Force
}
New-Item -ItemType Directory -Path $OutputDir | Out-Null

# Берём только директории проектов
$Projects = Get-ChildItem -Directory

foreach ($Project in $Projects) {

    $ProjectName = $Project.Name
    $ProjectPath = $Project.FullName
    $OutputFile = Join-Path $OutputDir "$ProjectName.txt"
    $FileCount = 0

    Write-Host "`n▶ Processing project: $ProjectName" -ForegroundColor Cyan

    "=== PROJECT: $ProjectName ===" | Out-File $OutputFile -Encoding UTF8
    "Path: $ProjectPath"           | Out-File $OutputFile -Append
    "Collected: $(Get-Date)"       | Out-File $OutputFile -Append
    ""                             | Out-File $OutputFile -Append

    Get-ChildItem $ProjectPath -Recurse -Include $IncludeExtensions |
    Where-Object {
        $skip = $false
        foreach ($dir in $ExcludeDirs) {
            if ($_.FullName -match "\\$dir\\") { $skip = $true }
        }
        -not $skip
    } |
    ForEach-Object {
        $FileCount++
        Write-Host "   [$FileCount] $($_.FullName)"

        "" | Out-File $OutputFile -Append
        "=== FILE: $($_.FullName) ===" | Out-File $OutputFile -Append
        "" | Out-File $OutputFile -Append

        Get-Content $_.FullName -Encoding UTF8 -ErrorAction SilentlyContinue |
            Out-File $OutputFile -Append

        "" | Out-File $OutputFile -Append
        "=== END OF FILE ===" | Out-File $OutputFile -Append
    }

    $SizeMB = [math]::Round((Get-Item $OutputFile).Length / 1MB, 2)

    Write-Host "   Files collected: $FileCount" -ForegroundColor Yellow
    Write-Host "   Output: $OutputFile ($SizeMB MB)" -ForegroundColor Green
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "ALL PROJECTS PROCESSED SUCCESSFULLY" -ForegroundColor Green
Write-Host "Results directory: $OutputDir" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Green

Write-Host "`nPress Enter to exit..." -ForegroundColor Gray
Read-Host
