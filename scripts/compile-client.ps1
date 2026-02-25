[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'

function Write-Step([string]$Message) {
    Write-Host "[STEP] $Message" -ForegroundColor Cyan
}

function Write-Ok([string]$Message) {
    Write-Host "[OK] $Message" -ForegroundColor Green
}

function Write-Warn([string]$Message) {
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Ensure-Command([string]$Command, [string]$Tip) {
    if (-not (Get-Command $Command -ErrorAction SilentlyContinue)) {
        throw "未找到命令 '$Command'。$Tip"
    }
}

function Ensure-NodeVersion {
    $script:NodeVersionRaw = (& node -v).Trim()
    $nodeMajorRaw = (& node -p "process.versions.node.split('.')[0]").Trim()
    $script:NodeMajor = 0
    if (-not [int]::TryParse($nodeMajorRaw, [ref]$script:NodeMajor)) {
        throw "无法解析 Node.js 版本：$script:NodeVersionRaw"
    }
    if ($script:NodeMajor -lt 16) {
        throw "当前 Node.js 版本 $script:NodeVersionRaw 过低，请使用 Node.js 16+。"
    }
    if ($script:NodeMajor -ge 21) {
        Write-Ok "检测到 Node.js $script:NodeVersionRaw，已完成 Node 24 兼容性适配。"
    }
}

function Check-PathLength {
    if ($projectRoot.Length -gt 80) {
        Write-Warn "项目路径过长 ($($projectRoot.Length) 字符)，可能导致 npm 依赖安装失败。"
        Write-Warn "建议将项目移动到较短的路径（如 C:\CampusErrand）。"
    }
}

function Ensure-NpmDependencies([string]$ProjectDir, [string]$ProjectName) {
    $nodeModules = Join-Path $ProjectDir "node_modules"
    $lockFile = Join-Path $ProjectDir "package-lock.json"
    $projectKey = (Resolve-Path $ProjectDir).Path
    $lockStamp = if (Test-Path $lockFile) { (Get-Item $lockFile).LastWriteTimeUtc.Ticks } else { $null }
    $state = $script:ClientBuildState[$projectKey]
    $needInstall = -not (Test-Path $nodeModules)
    
    # 额外检查核心依赖是否完整 (防止长路径导致安装不全)
    $isCorrupted = $false
    if ($ProjectName -eq "Uniapp" -and (Test-Path $nodeModules)) {
        if (-not (Test-Path (Join-Path $nodeModules "core-js-pure\actual\array\from.js"))) {
            $isCorrupted = $true
            Write-Warn "$ProjectName 依赖项似乎不完整，可能是路径过长导致安装失败。准备重新安装 ..."
        }
    }

    $needRebuild = $needInstall -or $isCorrupted -or ($null -eq $state) -or ($state.nodeMajor -ne $script:NodeMajor) -or ($state.lockStamp -ne $lockStamp)

    if (-not $needRebuild) {
        Write-Ok "$ProjectName 依赖已就绪。"
        return
    }

    Write-Step "$ProjectName 依赖重建中 ..."
    Push-Location $ProjectDir
    try {
        if (Test-Path $lockFile) {
            & npm ci
        }
        else {
            & npm install
        }
        if ($LASTEXITCODE -ne 0) {
            throw "$ProjectName 依赖重建失败（退出码 $LASTEXITCODE）。"
        }
    }
    finally {
        Pop-Location
    }

    $script:ClientBuildState[$projectKey] = @{
        nodeMajor = $script:NodeMajor
        lockStamp = $lockStamp
        updatedAt = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    }
    $script:ClientBuildState | ConvertTo-Json -Depth 5 | Set-Content -Path $script:ClientBuildStateFile -Encoding UTF8
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Resolve-Path (Join-Path $scriptDir "..")
$adminDir = Join-Path $projectRoot "campus-errand-admin"
$uniappDir = Join-Path $projectRoot "campus-errand-uniapp"
$runDir = Join-Path $projectRoot ".run"
$script:ClientBuildStateFile = Join-Path $runDir "client-build-state.json"
New-Item -ItemType Directory -Path $runDir -Force | Out-Null
$script:ClientBuildState = @{}
if (Test-Path $script:ClientBuildStateFile) {
    try {
        $script:ClientBuildState = Get-Content $script:ClientBuildStateFile -Raw | ConvertFrom-Json -AsHashtable
        if ($null -eq $script:ClientBuildState) { $script:ClientBuildState = @{} }
    }
    catch {
        $script:ClientBuildState = @{}
    }
}

Ensure-Command "node" "请安装 Node.js 并配置 PATH。"
Ensure-Command "npm" "请安装 npm。"
Ensure-NodeVersion
Check-PathLength

Write-Step "检查客户端依赖 ..."
Ensure-NpmDependencies -ProjectDir $adminDir -ProjectName "后台管理"
Ensure-NpmDependencies -ProjectDir $uniappDir -ProjectName "Uniapp"

Write-Step "编译 Uniapp 小程序 ..."
Push-Location $uniappDir
try {
    $buildOk = $false
    foreach ($attempt in 1..2) {
        & npm run build:mp-weixin
        if ($LASTEXITCODE -eq 0) {
            $buildOk = $true
            break
        }
        if ($LASTEXITCODE -eq -1073740791 -and $attempt -lt 2) {
            Write-Warn "Uniapp 构建进程异常退出（退出码 $LASTEXITCODE），准备重试 ..."
            Start-Sleep -Seconds 2
            continue
        }
        break
    }
    if (-not $buildOk) {
        throw "Uniapp 小程序构建失败（退出码 $LASTEXITCODE）。"
    }
}
finally {
    Pop-Location
}

$uniappOutputDir = Join-Path $uniappDir "dist\build\mp-weixin"
if (-not (Test-Path $uniappOutputDir)) {
    throw "Uniapp 小程序构建失败：未找到构建产物目录 $uniappOutputDir"
}

$global:LASTEXITCODE = 0
Write-Ok "Uniapp 小程序构建完成：campus-errand-uniapp/dist/build/mp-weixin"

Write-Host ""
Write-Host "================ 客户端编译完成 ================" -ForegroundColor Green
Write-Host "构建产物: campus-errand-uniapp/dist/build/mp-weixin"
Write-Host "================================================" -ForegroundColor Green
