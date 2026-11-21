# NEXA AI — Desktop Client Startup Script
$env:JAVA_HOME = "C:\Users\Kose\.gradle\jdks\eclipse_adoptium-21-amd64-windows.2"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH

Write-Host "=================================================" -ForegroundColor Magenta
Write-Host "   NEXA AI — Desktop Assistant Client Starting    " -ForegroundColor Magenta
Write-Host "=================================================" -ForegroundColor Magenta

cd "c:\Users\Kose\.gemini\antigravity\scratch\java AI\nexa-ai"
..\mvnw.ps1 javafx:run -pl nexa-desktop
