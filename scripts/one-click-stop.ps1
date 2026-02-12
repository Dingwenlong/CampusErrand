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

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Resolve-Path (Join-Path $scriptDir "..")
$composeFile = Join-Path $projectRoot "docker-compose.yml"
$runDir = Join-Path $projectRoot ".run"
$stateFile = Join-Path $runDir "one-click-state.json"
$composeArgs = @("-f", $composeFile, "-p", "campus-errand")

if (Test-Path $stateFile) {
    Write-Step "读取运行状态文件 ..."
    $state = Get-Content $stateFile -Raw | ConvertFrom-Json

    foreach ($name in @("apiPid", "adminPid")) {
        $pidValue = $state.$name
        if ($pidValue) {
            try {
                $proc = Get-Process -Id $pidValue -ErrorAction SilentlyContinue
                if ($proc) {
                    Stop-Process -Id $pidValue -Force
                    Write-Ok "已停止进程 $name (PID=$pidValue)"
                }
            }
            catch {
                Write-Warn "停止进程 $name (PID=$pidValue) 失败：$($_.Exception.Message)"
            }
        }
    }
}
else {
    Write-Warn "未找到状态文件：$stateFile"
}

Write-Step "停止 Docker 依赖服务 ..."
& docker compose @composeArgs down

Write-Ok "已完成停止与清理。"
