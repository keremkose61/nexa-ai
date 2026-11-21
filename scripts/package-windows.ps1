# NEXA AI — Windows Packaging with jpackage
param(
    [string]$AppVersion = "1.0.0",
    [string]$Vendor = "NEXA AI Technologies",
    [string]$OutputDir = "c:\Users\Kose\.gemini\antigravity\scratch\java AI\dist"
)

$env:JAVA_HOME = "C:\Users\Kose\.gradle\jdks\eclipse_adoptium-21-amd64-windows.2"
$jpackageExe = "$env:JAVA_HOME\bin\jpackage.exe"

Write-Host "Checking jpackage..." -ForegroundColor Cyan
if (-not (Test-Path $jpackageExe)) {
    Write-Error "jpackage not found at $jpackageExe"
    exit 1
}

New-Item -ItemType Directory -Force $OutputDir | Out-Null
$inputJarDir = "c:\Users\Kose\.gemini\antigravity\scratch\java AI\nexa-ai\nexa-desktop\target"
$mainJar = "nexa-desktop-1.0.0-SNAPSHOT.jar"

Write-Host "Creating Windows Application Package with jpackage..." -ForegroundColor Green
& $jpackageExe `
    --type app-image `
    --name "NexaAI" `
    --app-version $AppVersion `
    --vendor $Vendor `
    --input $inputJarDir `
    --main-jar $mainJar `
    --main-class "com.nexa.desktop.NexaApplication" `
    --dest $OutputDir `
    --win-shortcut `
    --win-menu

Write-Host "Build complete! Package output located in: $OutputDir\NexaAI" -ForegroundColor Green
