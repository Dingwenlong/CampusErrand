[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'

function Write-Step([string]$Message) {
    Write-Host "[STEP] $Message" -ForegroundColor Cyan
}

function Write-Warn([string]$Message) {
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Write-Ok([string]$Message) {
    Write-Host "[OK] $Message" -ForegroundColor Green
}

function Ensure-Command([string]$Command, [string]$Tip) {
    if (-not (Get-Command $Command -ErrorAction SilentlyContinue)) {
        throw "未找到命令 '$Command'。$Tip"
    }
}

function Ensure-NpmDependencies([string]$ProjectDir, [string]$ProjectName) {
    $nodeModules = Join-Path $ProjectDir "node_modules"
    if (Test-Path $nodeModules) {
        Write-Ok "$ProjectName 依赖已安装（检测到 node_modules）。"
        return
    }

    Write-Step "$ProjectName 缺少依赖，执行 npm install ..."
    Push-Location $ProjectDir
    try {
        & npm install
    }
    finally {
        Pop-Location
    }
}

function Test-PortInUse([int]$Port) {
    return [bool](Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue)
}

function Start-LongRunningProcess([string]$Name, [string]$Command, [string]$WorkingDirectory) {
    $shell = (Get-Command pwsh -ErrorAction SilentlyContinue)?.Source
    if (-not $shell) {
        $shell = (Get-Command powershell -ErrorAction SilentlyContinue)?.Source
    }
    if (-not $shell) {
        throw "未找到可用 PowerShell（pwsh/powershell）。"
    }

    $proc = Start-Process -FilePath $shell -WorkingDirectory $WorkingDirectory -ArgumentList @("-NoExit", "-Command", $Command) -WindowStyle Minimized -PassThru
    Write-Ok "$Name 已启动，PID=$($proc.Id)"
    return $proc
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Resolve-Path (Join-Path $scriptDir "..")
$adminDir = Join-Path $projectRoot "campus-errand-admin"

Ensure-Command "node" "请安装 Node.js 16+ 并配置 PATH。"
Ensure-Command "npm" "请安装 npm。"

Write-Step "检查后台管理依赖 ..."
Ensure-NpmDependencies -ProjectDir $adminDir -ProjectName "后台管理"

if (Test-PortInUse 3000) {
    Write-Warn "检测到 3000 已在监听，跳过后台管理启动。"
}
else {
    $adminStartCmd = @"
Set-Location '$adminDir'
npm run dev
"@

    $adminProcess = Start-LongRunningProcess -Name "后台管理" -Command $adminStartCmd -WorkingDirectory $adminDir

    Write-Host ""
    Write-Host "================ 后台管理已启动 ================" -ForegroundColor Green
    Write-Host "后台管理地址: http://localhost:3000"
    Write-Host "================================================" -ForegroundColor Green
}
