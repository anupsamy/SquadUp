# Test script to verify server connectivity

Write-Host "Testing backend server connectivity..." -ForegroundColor Cyan
Write-Host ""

# Test 1: Check if port 3000 is listening
Write-Host "1. Checking if port 3000 is listening..." -ForegroundColor Yellow
$listening = Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue
if ($listening) {
    Write-Host "   ✓ Port 3000 is listening" -ForegroundColor Green
    Write-Host "   Local Address: $($listening.LocalAddress)" -ForegroundColor Gray
    Write-Host "   State: $($listening.State)" -ForegroundColor Gray
} else {
    Write-Host "   ✗ Port 3000 is NOT listening!" -ForegroundColor Red
    Write-Host "   The server may not be running." -ForegroundColor Yellow
}

Write-Host ""

# Test 2: Try to connect to localhost
Write-Host "2. Testing HTTP connection to localhost:3000..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:3000/api/" -TimeoutSec 5 -ErrorAction Stop
    Write-Host "   ✓ Server is responding on localhost" -ForegroundColor Green
    Write-Host "   Status Code: $($response.StatusCode)" -ForegroundColor Gray
} catch {
    Write-Host "   ✗ Cannot connect to localhost:3000" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""

# Test 3: Try to connect to 0.0.0.0 (all interfaces)
Write-Host "3. Testing HTTP connection to 0.0.0.0:3000..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://0.0.0.0:3000/api/" -TimeoutSec 5 -ErrorAction Stop
    Write-Host "   ✓ Server is responding on 0.0.0.0" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Cannot connect to 0.0.0.0:3000" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""

# Test 4: Check Windows Firewall rules
Write-Host "4. Checking Windows Firewall rules for port 3000..." -ForegroundColor Yellow
$firewallRules = Get-NetFirewallRule | Where-Object { $_.DisplayName -like "*3000*" -or $_.DisplayName -like "*Node*" }
if ($firewallRules) {
    Write-Host "   Found firewall rules:" -ForegroundColor Gray
    $firewallRules | ForEach-Object {
        Write-Host "   - $($_.DisplayName): $($_.Enabled)" -ForegroundColor Gray
    }
} else {
    Write-Host "   ⚠ No specific firewall rules found for port 3000" -ForegroundColor Yellow
    Write-Host "   You may need to allow Node.js through Windows Firewall" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Note: Android emulator uses 10.0.2.2 to access localhost" -ForegroundColor Cyan
Write-Host "If localhost works but emulator doesn't, check Windows Firewall" -ForegroundColor Cyan

