# NEXA AI — Backend Startup Script
$env:JAVA_HOME = "C:\Users\Kose\.gradle\jdks\eclipse_adoptium-21-amd64-windows.2"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH

Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "   NEXA AI — Enterprise Backend Server Starting   " -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

$jarPath = "c:\Users\Kose\.gemini\antigravity\scratch\java AI\nexa-ai\nexa-backend\target\nexa-backend-1.0.0-SNAPSHOT.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "Building backend jar..." -ForegroundColor Yellow
    cd "c:\Users\Kose\.gemini\antigravity\scratch\java AI\nexa-ai"
    ..\mvnw.ps1 package -pl nexa-backend -DskipTests
}

& "$env:JAVA_HOME\bin\java.exe" -jar $jarPath
