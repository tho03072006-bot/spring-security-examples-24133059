param([switch]$SkipTests)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
if (Test-Path -LiteralPath 'C:\Program Files\Java\jdk-26.0.2.1') {$env:JAVA_HOME='C:\Program Files\Java\jdk-26.0.2.1'}
if ($env:JAVA_HOME) {$env:PATH="$env:JAVA_HOME\bin;$env:PATH"}
$maven=if(Test-Path -LiteralPath 'D:\WEB\apache-maven-3.9.16\bin\mvn.cmd'){'D:\WEB\apache-maven-3.9.16\bin\mvn.cmd'}else{Join-Path $projectRoot 'mvnw.cmd'}
$repoPath=Join-Path ([Environment]::GetFolderPath('UserProfile')) '.m2\repository'
$mavenArgs=@("-Dmaven.repo.local=$repoPath",'clean','verify','-B')
if($SkipTests){$mavenArgs+='-DskipTests'}
Push-Location $projectRoot
try { & $maven @mavenArgs; exit $LASTEXITCODE } finally { Pop-Location }
