# Script para iniciar o ApiBank

Write-Host "================================" -ForegroundColor Cyan
Write-Host "   ApiBank - Banco Digital" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Verificar Docker
Write-Host "Verificando Docker..." -ForegroundColor Yellow
$dockerInstalled = Get-Command docker -ErrorAction SilentlyContinue

if ($dockerInstalled) {
    Write-Host "✓ Docker encontrado!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Subindo o ambiente com Docker Compose..." -ForegroundColor Yellow
    docker compose up -d
    
    Write-Host ""
    Write-Host "Aguardando serviços iniciarem..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
    
    Write-Host ""
    Write-Host "================================" -ForegroundColor Green
    Write-Host "   ✓ Ambiente Iniciado!" -ForegroundColor Green
    Write-Host "================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "API disponível em: http://localhost:8080" -ForegroundColor Cyan
    Write-Host "Swagger UI: http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
    Write-Host "Health Check: http://localhost:8080/actuator/health" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Para parar: docker compose down" -ForegroundColor Yellow
    
} else {
    Write-Host "✗ Docker não encontrado!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Opção 1: Instale o Docker Desktop" -ForegroundColor Yellow
    Write-Host "  https://www.docker.com/products/docker-desktop/" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Opção 2: Execute localmente com PostgreSQL instalado" -ForegroundColor Yellow
    Write-Host "  mvn spring-boot:run" -ForegroundColor Cyan
}

