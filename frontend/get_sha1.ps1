# Get SHA-1 fingerprint for debug keystore
Write-Host "Getting SHA-1 fingerprint for debug keystore..." -ForegroundColor Green

$debugKeystore = "$env:USERPROFILE\.android\debug.keystore"
$keytool = "$env:JAVA_HOME\bin\keytool"

if (-not (Test-Path $debugKeystore)) {
    Write-Host "Debug keystore not found at: $debugKeystore" -ForegroundColor Yellow
    Write-Host "Please run the app at least once in debug mode to generate the keystore." -ForegroundColor Yellow
    exit 1
}

if (-not (Test-Path $keytool)) {
    Write-Host "keytool not found. Trying to find Java..." -ForegroundColor Yellow
    $javaHome = (Get-Command java -ErrorAction SilentlyContinue).Source
    if ($javaHome) {
        $keytool = Join-Path (Split-Path (Split-Path $javaHome)) "bin\keytool.exe"
    }
    
    if (-not (Test-Path $keytool)) {
        Write-Host "ERROR: Could not find keytool. Please add Java to your PATH." -ForegroundColor Red
        Write-Host "Or run this command manually:" -ForegroundColor Yellow
        Write-Host "keytool -list -v -keystore `"$debugKeystore`" -alias androiddebugkey -storepass android -keypass android" -ForegroundColor Cyan
        exit 1
    }
}

Write-Host "`nSHA-1 Fingerprint:" -ForegroundColor Green
& $keytool -list -v -keystore $debugKeystore -alias androiddebugkey -storepass android -keypass android | Select-String -Pattern "SHA1"

Write-Host "`nCopy the SHA-1 value (without colons) and add it to Google Cloud Console." -ForegroundColor Yellow
Write-Host "Package name: com.cpen321.squadup" -ForegroundColor Cyan
