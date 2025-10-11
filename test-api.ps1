# Script para testar a API do ApiBank

Write-Host "================================" -ForegroundColor Cyan
Write-Host "   Testando ApiBank API" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Teste 1: Health Check
Write-Host "1. Testando Health Check..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get
    Write-Host "V API esta rodando!" -ForegroundColor Green
    Write-Host "Status: $($health.status)" -ForegroundColor Cyan
} catch {
    Write-Host "X API nao esta respondendo!" -ForegroundColor Red
    Write-Host "Certifique-se de que a API esta rodando (docker compose up -d)" -ForegroundColor Yellow
    exit
}

Write-Host ""

# Teste 2: Registrar Usuario
Write-Host "2. Registrando novo usuario..." -ForegroundColor Yellow
$registerBody = @{
    name = "Joao Silva"
    email = "joao@email.com"
    cpf = "52998224725"
    password = "senha123"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register" -Method Post -ContentType "application/json" -Body $registerBody
    
    Write-Host "V Usuario registrado com sucesso!" -ForegroundColor Green
    Write-Host "User ID: $($registerResponse.userId)" -ForegroundColor Cyan
    Write-Host "Token: $($registerResponse.token.Substring(0, 20))..." -ForegroundColor Cyan
    
    $token = $registerResponse.token
    
} catch {
    Write-Host "X Erro ao registrar (usuario pode ja existir)" -ForegroundColor Yellow
    Write-Host "Tentando fazer login..." -ForegroundColor Yellow
    
    # Teste 3: Login
    $loginBody = @{
        email = "joao@email.com"
        password = "senha123"
    } | ConvertTo-Json
    
    try {
        $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method Post -ContentType "application/json" -Body $loginBody
        
        Write-Host "V Login realizado com sucesso!" -ForegroundColor Green
        $token = $loginResponse.token
        
    } catch {
        Write-Host "X Erro ao fazer login!" -ForegroundColor Red
        exit
    }
}

Write-Host ""

# Teste 4: Consultar Contas
Write-Host "3. Consultando contas..." -ForegroundColor Yellow
try {
    $headers = @{
        Authorization = "Bearer $token"
    }
    
    $accounts = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts" -Method Get -Headers $headers
    
    Write-Host "V Contas encontradas: $($accounts.Count)" -ForegroundColor Green
    
    if ($accounts.Count -gt 0) {
        $accountId = $accounts[0].id
        Write-Host "Conta: $($accounts[0].accountNumber)" -ForegroundColor Cyan
        Write-Host "Saldo: R`$ $($accounts[0].balance)" -ForegroundColor Cyan
        
        Write-Host ""
        
        # Teste 5: Fazer Deposito
        Write-Host "4. Fazendo deposito de R`$ 1000..." -ForegroundColor Yellow
        $depositBody = @{
            amount = 1000.00
            description = "Deposito de teste"
        } | ConvertTo-Json
        
        try {
            $depositResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId/transactions/deposit" -Method Post -ContentType "application/json" -Headers $headers -Body $depositBody
            
            Write-Host "V Deposito realizado com sucesso!" -ForegroundColor Green
            Write-Host "Valor: R`$ $($depositResponse.amount)" -ForegroundColor Cyan
            Write-Host "ID da transacao: $($depositResponse.id)" -ForegroundColor Cyan
            
        } catch {
            Write-Host "X Erro ao fazer deposito: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
    
} catch {
    Write-Host "X Erro ao consultar contas: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "================================" -ForegroundColor Green
Write-Host "   V Testes Concluidos!" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
Write-Host ""
Write-Host "Acesse o Swagger para mais testes:" -ForegroundColor Yellow
Write-Host "http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
