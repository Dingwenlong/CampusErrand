[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'

function Write-Step([string]$Message) {
    Write-Host "[STEP] $Message" -ForegroundColor Cyan
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

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Resolve-Path (Join-Path $scriptDir "..")
$adminDir = Join-Path $projectRoot "campus-errand-admin"
$uniappDir = Join-Path $projectRoot "campus-errand-uniapp"

Ensure-Command "node" "请安装 Node.js 16+ 并配置 PATH。"
Ensure-Command "npm" "请安装 npm。"

Write-Step "检查客户端依赖 ..."
Ensure-NpmDependencies -ProjectDir $adminDir -ProjectName "后台管理"
Ensure-NpmDependencies -ProjectDir $uniappDir -ProjectName "Uniapp"

Write-Step "编译 Uniapp 小程序 ..."
Push-Location $uniappDir
try {
    & npm run build:mp-weixin
}
finally {
    Pop-Location
}
Write-Ok "Uniapp 小程序构建完成：campus-errand-uniapp/dist/build/mp-weixin"

Write-Host ""
Write-Host "================ 客户端编译完成 ================" -ForegroundColor Green
Write-Host "构建产物: campus-errand-uniapp/dist/build/mp-weixin"
Write-Host "================================================" -ForegroundColor Green
