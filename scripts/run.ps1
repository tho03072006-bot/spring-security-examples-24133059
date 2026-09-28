param([ValidateSet(1,2,3)][int]$Example=3,[ValidateSet('demo','sqlserver')][string]$Profile='demo')
$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
$moduleNames=@('vd1-email-login','vd2-custom-login','vd3-shop-otp')
$module=$moduleNames[$Example-1]
if (Test-Path -LiteralPath 'C:\Program Files\Java\jdk-26.0.2.1') { $env:JAVA_HOME='C:\Program Files\Java\jdk-26.0.2.1' }
$javaExe=if($env:JAVA_HOME){Join-Path $env:JAVA_HOME 'bin\java.exe'}else{'java'}
$jarPath=Join-Path $projectRoot "$module\target\$module-1.0.0.jar"
if (-not (Test-Path -LiteralPath $jarPath)) { & (Join-Path $PSScriptRoot 'build.ps1'); if($LASTEXITCODE -ne 0){exit $LASTEXITCODE} }
Push-Location (Join-Path $projectRoot $module)
try { & $javaExe -jar $jarPath "--spring.profiles.active=$Profile"; exit $LASTEXITCODE } finally { Pop-Location }
