# NEXA AI — One-Click Native Windows Launcher (Backend + Desktop UI)
# No Docker required — connects to local MySQL 8.x

$PSScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "       NEXA AI — Enterprise Desktop Assistant    " -ForegroundColor Cyan
Write-Host "       Starting Backend and Desktop Client       " -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

# 1. Check local MySQL
Write-Host "[1/3] Checking native MySQL connection (Port 3306)..." -ForegroundColor Yellow
$mysqlCheck = Test-NetConnection -ComputerName 127.0.0.1 -Port 3306 -WarningAction SilentlyContinue
if (-not $mysqlCheck.TcpTestSucceeded) {
    Write-Host "[ERROR] MySQL is not responding on 127.0.0.1:3306!" -ForegroundColor Red
    Write-Host "Please start WampServer / MySQL 8 service first." -ForegroundColor Yellow
    exit 1
}
Write-Host "[OK] Local MySQL is running on port 3306." -ForegroundColor Green

# 2. Start Backend in separate window
Write-Host "[2/3] Launching NEXA AI Spring Boot Backend..." -ForegroundColor Yellow
$backendProcess = Start-Process powershell -ArgumentList "-ExecutionPolicy Bypass", "-NoExit", "-File `"$PSScriptRoot\run-backend.ps1`"" -PassThru

# Wait for backend health
Write-Host "Waiting for backend to become ready..." -ForegroundColor Gray
$ready = $false
for ($i = 0; $i -lt 45; $i++) {
    Start-Sleep -Seconds 1
    try {
        $res = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get -TimeoutSec 2 -ErrorAction Stop
        if ($res.status -eq "UP") {
            $ready = $true
            break
        }
    } catch {
        Write-Host "." -NoNewline -ForegroundColor Gray
    }
}
Write-Host ""

if (-not $ready) {
    Write-Host "[WARN] Backend did not respond within 45s. Check the backend window for logs." -ForegroundColor Yellow
} else {
    Write-Host "[OK] Backend is healthy and ready on http://localhost:8080" -ForegroundColor Green
}

# 3. Start JavaFX Desktop Client
Write-Host "[3/3] Starting NEXA AI JavaFX Desktop UI..." -ForegroundColor Magenta
& "$PSScriptRoot\run-desktop.ps1"
