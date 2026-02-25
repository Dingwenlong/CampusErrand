[CmdletBinding()]
param(
    [int]$MySqlPort = 3306,
    [int]$RedisPort = 6379
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

Ensure-Command "mvn" "请安装 Maven 3.6+ 并配置 PATH。"

$mysqlPort = Select-Port -Preferred $MySqlPort -Fallback 33306
$redisPort = Select-Port -Preferred $RedisPort -Fallback 36379

Write-Step "检查 Maven 依赖 ..."
Ensure-MavenDependencies -RootDir $projectRoot

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

    Write-Host ""
    Write-Host "================ API 服务已启动 ================" -ForegroundColor Green
    Write-Host "API 接口地址: http://localhost:8080/api"
    Write-Host "API 文档地址: http://localhost:8080/api/doc.html"
    Write-Host "MySQL 端口: $mysqlPort"
    Write-Host "Redis 端口: $redisPort"
    Write-Host "================================================" -ForegroundColor Green
}
