$ErrorActionPreference = 'Stop'
$version = '8.11.1'
$checksum = 'f397b287023acdba1e9f6fc5ea72d22dd63669d59ed4a289a29b1a76eee151c6'
$base = if ($env:GRADLE_USER_HOME) { $env:GRADLE_USER_HOME } else { Join-Path $env:USERPROFILE '.gradle' }
$cache = Join-Path $base 'coffeedev-bootstrap'
$gradle = Join-Path $cache "gradle-$version/bin/gradle.bat"
if (!(Test-Path $gradle)) {
    New-Item -ItemType Directory -Force -Path $cache | Out-Null
    $archive = Join-Path $cache "gradle-$version-bin.zip"
    Invoke-WebRequest "https://downloads.gradle.org/distributions/gradle-$version-bin.zip" -OutFile $archive
    if ((Get-FileHash $archive -Algorithm SHA256).Hash.ToLowerInvariant() -ne $checksum) { throw 'Gradle checksum mismatch' }
    Expand-Archive -Path $archive -DestinationPath $cache -Force
}
& $gradle @args
exit $LASTEXITCODE
