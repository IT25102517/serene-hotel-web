param([Parameter(ValueFromRemainingArguments=$true)][string[]]$MavenArgs)
$ErrorActionPreference = 'Stop'
$backendRoot = Split-Path $PSScriptRoot -Parent
if (-not $env:JAVA_HOME) {
    $javaSettings = & cmd.exe /d /c 'java -XshowSettings:properties -version 2>&1' | Out-String
    if ($javaSettings -match 'java.home\s*=\s*([^\r\n]+)') { $env:JAVA_HOME = $Matches[1].Trim() }
}
if (-not $env:JAVA_HOME -or -not (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin/javac.exe'))) {
    throw 'Install JDK 17 or 21 and set JAVA_HOME to the JDK folder.'
}
$installed = Get-Command mvn.cmd -ErrorAction SilentlyContinue
if ($installed) { $maven = $installed.Source }
else {
    $version = '3.9.15'
    $cache = Join-Path $backendRoot '.mvn/cache'
    $maven = Join-Path $cache "apache-maven-$version/bin/mvn.cmd"
    if (-not (Test-Path -LiteralPath $maven)) {
        New-Item -ItemType Directory -Force -Path $cache | Out-Null
        $url = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$version/apache-maven-$version-bin.zip"
        $zip = Join-Path $cache 'maven.zip'
        Write-Host "Downloading Maven $version for this project..."
        Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing
        $expected = ((Invoke-WebRequest -Uri "$url.sha512" -UseBasicParsing).Content.Trim() -split '\s+')[0]
        $hasher = [System.Security.Cryptography.SHA512]::Create()
        $stream = [System.IO.File]::OpenRead($zip)
        try { $actual = [BitConverter]::ToString($hasher.ComputeHash($stream)).Replace('-', '') }
        finally { $stream.Dispose(); $hasher.Dispose() }
        if ($actual -ne $expected) { throw 'Maven download checksum mismatch.' }
        [System.Reflection.Assembly]::LoadWithPartialName('System.IO.Compression.FileSystem') | Out-Null
        [System.IO.Compression.ZipFile]::ExtractToDirectory($zip, $cache)
    }
}
Push-Location $backendRoot
try { & $maven @MavenArgs; exit $LASTEXITCODE } finally { Pop-Location }
