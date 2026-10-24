param(
    [ValidateSet('run', 'build')]
    [string]$Action = 'run',
    [string]$DataFile = ''
)

$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$compileRoot = Join-Path $projectRoot '.build'

function Remove-BuildDirectory {
    if (Test-Path -LiteralPath $compileRoot) {
        Remove-Item -LiteralPath $compileRoot -Recurse -Force -ErrorAction SilentlyContinue
    }
}

if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    throw 'JDK 17 or newer is required. Install a JDK and add its bin directory to PATH.'
}
if ($Action -eq 'run' -and -not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw 'The java command was not found. Add the JDK bin directory to PATH.'
}

$sourceFiles = @(Get-ChildItem -LiteralPath (Join-Path $projectRoot 'src') -Recurse -Filter '*.java' |
    Select-Object -ExpandProperty FullName)
if ($sourceFiles.Count -eq 0) {
    throw 'No Java source files were found in src.'
}

# Compile from scratch every time so classes removed from src cannot survive a
# rebuild. The directory is temporary and is removed when this script exits.
Remove-BuildDirectory
$null = New-Item -ItemType Directory -Force -Path $compileRoot

$exitCode = 0
try {
    & javac --release 17 -encoding UTF-8 -d $compileRoot @sourceFiles
    if ($LASTEXITCODE -ne 0) {
        $exitCode = $LASTEXITCODE
    }
    elseif ($Action -eq 'build') {
        Write-Output 'BUILD OK'
    }
    else {
        if ([string]::IsNullOrWhiteSpace($DataFile)) {
            $DataFile = Join-Path $projectRoot 'data\books.tsv'
        }
        elseif (-not [System.IO.Path]::IsPathRooted($DataFile)) {
            $DataFile = Join-Path $projectRoot $DataFile
        }

        $utf8 = New-Object System.Text.UTF8Encoding($false)
        [Console]::InputEncoding = $utf8
        [Console]::OutputEncoding = $utf8
        $OutputEncoding = $utf8

        Push-Location -LiteralPath $projectRoot
        try {
            & java '-Dfile.encoding=UTF-8' '-Dstdout.encoding=UTF-8' '-Dstderr.encoding=UTF-8' -cp $compileRoot library.Main $DataFile
            $exitCode = $LASTEXITCODE
        }
        finally {
            Pop-Location
        }
    }
}
finally {
    Remove-BuildDirectory
}

exit $exitCode
