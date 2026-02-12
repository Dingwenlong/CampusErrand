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

function Ensure-MavenDependencies([string]$RootDir) {
    $m2Dir = Join-Path $env:USERPROFILE ".m2\repository"
    $markers = @(
        (Join-Path $m2Dir "org\springframework\boot\spring-boot-starter-web\3.1.8"),
        (Join-Path $m2Dir "com\baomidou\mybatis-plus-boot-starter\3.5.7"),
        (Join-Path $m2Dir "com\alibaba\druid-spring-boot-starter\1.2.20")
    )

    $missing = $markers | Where-Object { -not (Test-Path $_) }
    if (-not $missing) {
        Write-Ok "后端 Maven 关键依赖已安装。"
        return
    }

    Write-Step "检测到后端依赖缺失，执行 Maven 依赖预拉取 ..."
    Push-Location $RootDir
    try {
        & mvn -pl campus-errand-api -am -DskipTests dependency:go-offline
    }
    finally {
        Pop-Location
    }
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
$apiDir = Join-Path $projectRoot "campus-errand-api"
$adminDir = Join-Path $projectRoot "campus-errand-admin"
$uniappDir = Join-Path $projectRoot "campus-errand-uniapp"
$sqlFile = Join-Path $projectRoot "sql\init.sql"
$composeFile = Join-Path $projectRoot "docker-compose.yml"
$runDir = Join-Path $projectRoot ".run"
$stateFile = Join-Path $runDir "one-click-state.json"

New-Item -ItemType Directory -Path $runDir -Force | Out-Null

Ensure-Command "mvn" "请安装 Maven 3.6+ 并配置 PATH。"
Ensure-Command "node" "请安装 Node.js 16+ 并配置 PATH。"
Ensure-Command "npm" "请安装 npm。"

$mysqlPort = 3306
$redisPort = 6379

if (-not $SkipDocker) {
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
}
else {
    Write-Warn "已跳过 Docker。请确保 MySQL 和 Redis 已手动启动。"
}

Ensure-MavenDependencies -RootDir $projectRoot
Ensure-NpmDependencies -ProjectDir $adminDir -ProjectName "后台管理"
Ensure-NpmDependencies -ProjectDir $uniappDir -ProjectName "Uniapp"

if (-not $SkipUniBuild) {
    Write-Step "编译 Uniapp 小程序 ..."
    Push-Location $uniappDir
    try {
        & npm run build:mp-weixin
    }
    finally {
        Pop-Location
    }
    Write-Ok "Uniapp 小程序构建完成：campus-errand-uniapp/dist/build/mp-weixin"
}
else {
    Write-Warn "已跳过 Uniapp 构建。"
}

$apiProcess = $null
$adminProcess = $null

if ($SkipStart) {
    Write-Warn "已启用 -SkipStart，跳过 API 和后台管理启动。"
}
else {
    if (Test-PortInUse 8080) {
        Write-Warn "检测到 8080 已在监听，跳过 API 启动。"
    }
    else {
        $apiStartCmd = @"
`$env:SPRING_DATASOURCE_URL='jdbc:mysql://localhost:$mysqlPort/campus_errand?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&connectionCollation=utf8mb4_unicode_ci'
`$env:SPRING_DATASOURCE_USERNAME='root'
`$env:SPRING_DATASOURCE_PASSWORD='root_password'
`$env:SPRING_REDIS_HOST='localhost'
`$env:SPRING_REDIS_PORT='$redisPort'
Set-Location '$apiDir'
mvn spring-boot:run
"@
        $apiProcess = Start-LongRunningProcess -Name "后端 API" -Command $apiStartCmd -WorkingDirectory $projectRoot
    }

    if (Test-PortInUse 3000) {
        Write-Warn "检测到 3000 已在监听，跳过后台管理启动。"
    }
    else {
        $adminStartCmd = @"
Set-Location '$adminDir'
npm run dev
"@
        $adminProcess = Start-LongRunningProcess -Name "后台管理" -Command $adminStartCmd -WorkingDirectory $adminDir
    }
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
