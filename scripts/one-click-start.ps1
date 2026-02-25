[CmdletBinding()]
param(
    [switch]$SkipDocker,
    [switch]$SkipUniBuild,
    [switch]$SkipStart
)

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

function Test-PortInUse([int]$Port) {
    return [bool](Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue)
}

function Select-Port([int]$Preferred, [int]$Fallback) {
    if (Test-PortInUse $Preferred) {
        Write-Warn "端口 $Preferred 已被占用，改用端口 $Fallback。"
        return $Fallback
    }
    return $Preferred
}

function Wait-ServiceHealthy([string]$Service, [int]$TimeoutSeconds = 180) {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        $containerId = (& docker compose @composeArgs ps -q $Service).Trim()
        if ($containerId) {
            $status = (& docker inspect --format "{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}" $containerId).Trim()
            if ($status -eq 'healthy' -or $status -eq 'running') {
                Write-Ok "$Service 服务状态：$status"
                return
            }
        }
        Start-Sleep -Seconds 2
    }

    throw "$Service 服务健康检查超时。"
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Resolve-Path (Join-Path $scriptDir "..")
$sqlFile = Join-Path $projectRoot "sql\init.sql"
$composeFile = Join-Path $projectRoot "docker-compose.yml"
$runDir = Join-Path $projectRoot ".run"
$stateFile = Join-Path $runDir "one-click-state.json"

New-Item -ItemType Directory -Path $runDir -Force | Out-Null

Ensure-Command "docker" "请安装 Docker Desktop 并启动。"

Write-Step "检查 Docker 服务 ..."
& docker info | Out-Null
Write-Ok "Docker 可用。"

$mysqlPort = Select-Port -Preferred 3306 -Fallback 33306
$redisPort = Select-Port -Preferred 6379 -Fallback 36379

$env:MYSQL_PORT = "$mysqlPort"
$env:REDIS_PORT = "$redisPort"

$composeArgs = @("-f", $composeFile, "-p", "campus-errand")

Write-Step "启动 Docker 依赖（MySQL/Redis）..."
& docker compose @composeArgs up -d mysql redis | Out-Null

Wait-ServiceHealthy -Service "mysql"
Wait-ServiceHealthy -Service "redis"

Write-Step "执行数据库初始化脚本 ..."
Get-Content $sqlFile | & docker compose @composeArgs exec -T mysql mysql -uroot -proot_password
Write-Ok "数据库初始化完成。"

Write-Host ""
Write-Host "=== 调用独立脚本 ===" -ForegroundColor Cyan

Write-Step "执行客户端编译脚本 (compile-client.ps1) ..."
& (Join-Path $scriptDir "compile-client.ps1")

if ($LASTEXITCODE -ne 0) {
    throw "客户端编译失败。"
}

Write-Host ""
Write-Host "=== 调用独立脚本 ===" -ForegroundColor Cyan

Write-Step "执行 API 服务启动脚本 (start-api.ps1) ..."
$apiProcess = $null
if (-not $SkipStart) {
    if (Test-PortInUse 8080) {
        Write-Warn "检测到 8080 已在监听，跳过 API 启动。"
    }
    else {
        Start-Process -FilePath (Join-Path $scriptDir "start-api.ps1") -WindowStyle Minimized
        $apiProcess = Get-Process -Name "powershell" -ErrorAction SilentlyContinue | Select-Object -Last 1
    }
}
else {
    Write-Warn "已启用 -SkipStart，跳过 API 启动。"
}

Write-Step "执行后台服务启动脚本 (start-admin.ps1) ..."
$adminProcess = $null
if (-not $SkipStart) {
    if (Test-PortInUse 3000) {
        Write-Warn "检测到 3000 已在监听，跳过后台管理启动。"
    }
    else {
        Start-Process -FilePath (Join-Path $scriptDir "start-admin.ps1") -WindowStyle Minimized
        $adminProcess = Get-Process -Name "powershell" -ErrorAction SilentlyContinue | Select-Object -Last 1
    }
}
else {
    Write-Warn "已启用 -SkipStart，跳过后台管理启动。"
}

$state = [ordered]@{
    startedAt = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    mysqlPort = $mysqlPort
    redisPort = $redisPort
    apiPid = if ($apiProcess) { $apiProcess.Id } else { $null }
    adminPid = if ($adminProcess) { $adminProcess.Id } else { $null }
}
$state | ConvertTo-Json | Set-Content -Path $stateFile -Encoding UTF8

Write-Host ""
Write-Host "================ 启动完成 ================" -ForegroundColor Green
Write-Host "API 接口地址: http://localhost:8080/api"
Write-Host "API 文档地址: http://localhost:8080/api/doc.html"
Write-Host "后台管理地址: http://localhost:3000"
Write-Host "MySQL 端口: $mysqlPort"
Write-Host "Redis 端口: $redisPort"
Write-Host "状态文件: $stateFile"
Write-Host "==========================================" -ForegroundColor Green
