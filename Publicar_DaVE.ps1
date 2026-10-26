# ============================================================
#  Publicar_DaVE.ps1 - Pipeline de publicacion de DaVE Player
#  Uso:  .\Publicar_DaVE.ps1 -NuevaVersion "4.9.67" -Changelog "...texto..."
#  Hace: bump de version -> commit -> push -> release -> espera CI
#        -> lee tamano real del APK -> actualiza version.json -> push
# ============================================================
param(
    [Parameter(Mandatory = $true)]
    [string]$NuevaVersion,

    [Parameter(Mandatory = $true)]
    [string]$Changelog
)

$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $repo

function Fail($msg) { Write-Host "[X] $msg" -ForegroundColor Red; exit 1 }
function Ok($msg)   { Write-Host "[OK] $msg" -ForegroundColor Green }

# 0. Validaciones previas
$gitClean = (git status --porcelain) -eq $null
if (-not $gitClean) { Fail "El arbol de trabajo tiene cambios sin commitear. Commitea o descarta primero." }
if ($NuevaVersion -notmatch '^\d+\.\d+\.\d+$') { Fail "La version debe tener formato X.Y.Z (ej: 4.9.67)" }
gh auth status 2>$null | Out-Null
if ($LASTEXITCODE -ne 0) { Fail "gh CLI no autenticado. Ejecuta: gh auth login" }
Ok "Pre-validaciones superadas"

# 1. Calcular versionCode = anterior + 1
$gradlePath = Join-Path $repo "app\build.gradle.kts"
$gradle = Get-Content $gradlePath -Raw
if ($gradle -notmatch 'versionCode = (\d+)') { Fail "No encontre versionCode en build.gradle.kts" }
$nuevoCode = [int]$Matches[1] + 1
$versionActual = if ($gradle -match 'versionName = "([^"]+)"') { $Matches[1] } else { Fail "No encontre versionName" }
Write-Host "Publicando v$NuevaVersion (build $nuevoCode), anterior: v$versionActual"

# 2. Bump en build.gradle.kts
$gradle = $gradle -replace 'versionCode = \d+', "versionCode = $nuevoCode"
$gradle = $gradle -replace 'versionName = "[^"]+"', "versionName = `"$NuevaVersion`""
[System.IO.File]::WriteAllText($gradlePath, $gradle, [System.Text.UTF8Encoding]::new($false))
Ok "build.gradle.kts actualizado a $nuevoCode / $NuevaVersion"

# 3. version.json provisional (sin tamano; se corrige al final)
$manifest = [ordered]@{
    versionCode = $nuevoCode
    versionName = $NuevaVersion
    downloadUrl = "https://github.com/davidchaparro780-wq/D4ve/releases/download/v$NuevaVersion/app-release.apk"
    changelog   = $Changelog
    fileSizeMb  = 0
}
$manifest | ConvertTo-Json -Depth 3 | Set-Content (Join-Path $repo "version.json") -Encoding UTF8
Ok "version.json provisional escrito"

# 4. Commit + push
git add app/build.gradle.kts version.json
git commit -m "chore(release): v$NuevaVersion (build $nuevoCode)"
git push origin main
if ($LASTEXITCODE -ne 0) { Fail "El push fallo (revisa scope workflow o red)" }
Ok "Push a main realizado"

# 5. Esperar build de compilacion del push (detecta errores de sintaxis antes del release)
Write-Host "Esperando build de compilacion (detecta errores antes del release)..."
Start-Sleep -Seconds 15
$runId = (gh run list --limit 1 --json databaseId --jq ".[0].databaseId")
gh run watch $runId --exit-status --interval 30
if ($LASTEXITCODE -ne 0) { Fail "El build de compilacion FALLO - corrige los errores y vuelve a ejecutar (la version ya esta bumped, solo re-push)" }
Ok "Compilacion verificada"

# 6. Crear release (dispara build de APKs firmados)
$tag = "v$NuevaVersion"
$titulo = $Changelog.Split("`n")[0].Trim()
gh release create $tag --title $titulo --notes $Changelog
if ($LASTEXITCODE -ne 0) { Fail "No se pudo crear el release $tag" }
Ok "Release $tag creado"

# 7. Esperar build de release (sube app-debug.apk y app-release.apk firmados)
Write-Host "Esperando build de release firmado (~7 min)..."
Start-Sleep -Seconds 15
$relRunId = (gh run list --workflow build-release-apk.yml --limit 1 --json databaseId --jq ".[0].databaseId")
gh run watch $relRunId --exit-status --interval 30
if ($LASTEXITCODE -ne 0) { Fail "El build de release fallo" }
Ok "APKs firmados subidos al release"

# 8. Tamano real del app-release.apk
$assetSize = (gh api "repos/davidchaparro780-wq/D4ve/releases/tags/$tag" --jq ".assets[] | select(.name==`"app-release.apk`") | .size")
if (-not $assetSize) { Fail "No encontre el asset app-release.apk en el release" }
$tamanoMb = [math]::Round([double]$assetSize / 1048576.0, 1)
Ok "Tamano real del APK: $tamanoMb MB"

# 9. Corregir version.json con el tamano real y pushear
$manifest.fileSizeMb = $tamanoMb
$manifest | ConvertTo-Json -Depth 3 | Set-Content (Join-Path $repo "version.json") -Encoding UTF8
git add version.json
git commit -m "chore(release): manifest v$NuevaVersion con tamano real ($tamanoMb MB)"
git push origin main
if ($LASTEXITCODE -ne 0) { Fail "Push final del manifest fallo" }

Write-Host ""
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host " RELEASE v$NuevaVersion PUBLICADO COMPLETO" -ForegroundColor Cyan
Write-Host " APK:      $tamanoMb MB (release firmado)"
Write-Host " Updater:  activo - los usuarios recibiran el aviso" -ForegroundColor Cyan
Write-Host " URL:      https://github.com/davidchaparro780-wq/D4ve/releases/tag/$tag"
Write-Host "=========================================================" -ForegroundColor Cyan
