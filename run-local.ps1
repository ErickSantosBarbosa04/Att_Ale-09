$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$runtime = Join-Path $projectRoot 'runtime'
$javaHome = $null
$mavenCommand = $null

$bundledJava = Get-ChildItem -LiteralPath (Join-Path $runtime 'jdk') -Filter java.exe -Recurse -ErrorAction SilentlyContinue |
    Where-Object FullName -Like '*\bin\java.exe' | Select-Object -First 1
if ($bundledJava) {
    $javaHome = Split-Path (Split-Path $bundledJava.FullName -Parent) -Parent
}
elseif ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    $javaHome = $env:JAVA_HOME
}
else {
    $javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
    if (-not $javaCommand) { throw 'Java 21 não encontrado. Instale um JDK 21 e configure JAVA_HOME.' }
    $javaVersion = (& $javaCommand.Source -version 2>&1 | Out-String)
    if ($javaVersion -notmatch 'version "21\.') { throw 'Este projeto requer Java 21. Configure JAVA_HOME para um JDK 21.' }
}

$bundledMaven = Join-Path $runtime 'apache-maven-3.9.16\bin\mvn.cmd'
if (Test-Path -LiteralPath $bundledMaven) {
    $mavenCommand = $bundledMaven
}
else {
    $maven = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if (-not $maven) { throw 'Maven não encontrado. Instale Maven e adicione mvn.cmd ao PATH.' }
    $mavenCommand = $maven.Source
}

$bundledMongo = Join-Path $runtime 'mongodb\bin\mongod.exe'
if (Test-Path -LiteralPath $bundledMongo) {
    $mongoCommand = $bundledMongo
}
else {
    $mongo = Get-Command mongod.exe -ErrorAction SilentlyContinue
    if (-not $mongo) { throw 'MongoDB não encontrado. Instale MongoDB Community ou prepare runtime\mongodb\bin\mongod.exe.' }
    $mongoCommand = $mongo.Source
}

function Test-LocalPort([int]$Port) {
    $client = [System.Net.Sockets.TcpClient]::new()
    try {
        $connect = $client.ConnectAsync('127.0.0.1', $Port)
        return $connect.Wait(800) -and $client.Connected
    }
    catch {
        return $false
    }
    finally {
        $client.Dispose()
    }
}

$workRoot = Split-Path (Split-Path $projectRoot -Parent) -Parent
$dataPath = Join-Path $workRoot 'work\mongodata'
$logPath = Join-Path $workRoot 'work\logs\mongod.log'
New-Item -ItemType Directory -Path $dataPath, (Split-Path $logPath -Parent) -Force | Out-Null

if (-not (Test-LocalPort 27017)) {
    $arguments = "--dbpath `"$dataPath`" --bind_ip 127.0.0.1 --port 27017 --logpath `"$logPath`" --logappend"
    Start-Process -FilePath $mongoCommand -ArgumentList $arguments -WindowStyle Hidden | Out-Null
    $mongoReady = $false
    for ($attempt = 0; $attempt -lt 30; $attempt++) {
        if (Test-LocalPort 27017) { $mongoReady = $true; break }
        Start-Sleep -Seconds 1
    }
    if (-not $mongoReady) { throw "MongoDB não respondeu em 127.0.0.1:27017. Consulte $logPath" }
}

$env:MONGODB_URI = 'mongodb://127.0.0.1:27017/juris_home_auth'
$env:COOKIE_SECURE = 'false'
if ($javaHome) {
    $env:JAVA_HOME = $javaHome
    $env:Path = (Join-Path $javaHome 'bin') + ';' + $env:Path
}
$env:Path = (Split-Path $mavenCommand -Parent) + ';' + $env:Path

Write-Host 'Juris Home será iniciado em http://127.0.0.1:8080'
Write-Host 'MongoDB local: 127.0.0.1:27017 (não acessível por outros computadores)'
& $mavenCommand -B -ntp spring-boot:run
