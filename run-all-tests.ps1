# Script de Testes Automatizados - ApiBank
# Executa todos os cenários de teste do roteiro

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  ApiBank - Suite de Testes Completa" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$testsPassed = 0
$testsFailed = 0

function Test-Result {
    param($name, $condition)
    if ($condition) {
        Write-Host "[PASS] $name" -ForegroundColor Green
        $script:testsPassed++
    } else {
        Write-Host "[FAIL] $name" -ForegroundColor Red
        $script:testsFailed++
    }
}

# ====================
# 1. PREPARAÇÃO
# ====================
Write-Host "1. PREPARAÇÃO" -ForegroundColor Yellow
Write-Host "---" -ForegroundColor DarkGray

try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get
    Test-Result "Health Check" ($health.status -eq "UP")
} catch {
    Write-Host "[FAIL] API não está rodando!" -ForegroundColor Red
    Write-Host "Execute: docker compose up -d" -ForegroundColor Yellow
    exit
}

Write-Host ""

# ====================
# 2. AUTENTICAÇÃO
# ====================
Write-Host "2. TESTES DE AUTENTICAÇÃO" -ForegroundColor Yellow
Write-Host "---" -ForegroundColor DarkGray

# 2.1 Registrar primeiro usuário
try {
    $register1 = @{
        name = "João Silva"
        email = "joao@email.com"
        cpf = "52998224725"
        password = "senha123"
    } | ConvertTo-Json
    
    $response1 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register" -Method Post -ContentType "application/json" -Body $register1
    $token1 = $response1.token
    $userId1 = $response1.userId
    Test-Result "Registrar usuário João" ($null -ne $token1)
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        # Usuário já existe, fazer login
        $login1 = @{
            email = "joao@email.com"
            password = "senha123"
        } | ConvertTo-Json
        
        $response1 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method Post -ContentType "application/json" -Body $login1
        $token1 = $response1.token
        $userId1 = $response1.userId
        Test-Result "Login João (usuário existente)" ($null -ne $token1)
    } else {
        Test-Result "Registrar usuário João" $false
    }
}

# 2.2 Registrar segundo usuário
try {
    $register2 = @{
        name = "Maria Santos"
        email = "maria@email.com"
        cpf = "11144477735"
        password = "senha456"
    } | ConvertTo-Json
    
    $response2 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register" -Method Post -ContentType "application/json" -Body $register2
    $token2 = $response2.token
    Test-Result "Registrar usuário Maria" ($null -ne $token2)
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        # Usuário já existe, fazer login
        $login2 = @{
            email = "maria@email.com"
            password = "senha456"
        } | ConvertTo-Json
        
        $response2 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method Post -ContentType "application/json" -Body $login2
        $token2 = $response2.token
        Test-Result "Login Maria (usuário existente)" ($null -ne $token2)
    } else {
        Test-Result "Registrar usuário Maria" $false
    }
}

# 2.3 Email duplicado deve falhar
try {
    $registerDup = @{
        name = "Pedro"
        email = "joao@email.com"
        cpf = "12345678909"
        password = "senha789"
    } | ConvertTo-Json
    
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register" -Method Post -ContentType "application/json" -Body $registerDup
    Test-Result "Rejeitar email duplicado" $false
} catch {
    Test-Result "Rejeitar email duplicado" ($_.Exception.Response.StatusCode -eq 400)
}

# 2.4 CPF inválido deve falhar
try {
    $registerInvalid = @{
        name = "Carlos"
        email = "carlos@email.com"
        cpf = "12345678901"
        password = "senha789"
    } | ConvertTo-Json
    
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register" -Method Post -ContentType "application/json" -Body $registerInvalid
    Test-Result "Rejeitar CPF inválido" $false
} catch {
    Test-Result "Rejeitar CPF inválido" ($_.Exception.Response.StatusCode -eq 400)
}

# 2.5 Login com senha errada
try {
    $loginWrong = @{
        email = "joao@email.com"
        password = "senhaErrada"
    } | ConvertTo-Json
    
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method Post -ContentType "application/json" -Body $loginWrong
    Test-Result "Rejeitar senha incorreta" $false
} catch {
    Test-Result "Rejeitar senha incorreta" ($_.Exception.Response.StatusCode -eq 401)
}

Write-Host ""

# ====================
# 3. CONTAS
# ====================
Write-Host "3. TESTES DE CONTAS" -ForegroundColor Yellow
Write-Host "---" -ForegroundColor DarkGray

$headers1 = @{ Authorization = "Bearer $token1" }
$headers2 = @{ Authorization = "Bearer $token2" }

# 3.1 Consultar contas do João
try {
    $accounts1 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts" -Method Get -Headers $headers1
    $accountId1 = $accounts1[0].id
    $accountNumber1 = $accounts1[0].accountNumber
    Test-Result "Consultar contas do João" ($accounts1.Count -ge 1)
} catch {
    Test-Result "Consultar contas do João" $false
}

# 3.2 Consultar contas da Maria
try {
    $accounts2 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts" -Method Get -Headers $headers2
    $accountId2 = $accounts2[0].id
    $accountNumber2 = $accounts2[0].accountNumber
    Test-Result "Consultar contas da Maria" ($accounts2.Count -ge 1)
} catch {
    Test-Result "Consultar contas da Maria" $false
}

# 3.3 João não pode acessar conta da Maria
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId2" -Method Get -Headers $headers1
    Test-Result "Bloquear acesso à conta de outro usuário" $false
} catch {
    Test-Result "Bloquear acesso à conta de outro usuário" ($_.Exception.Response.StatusCode -eq 404)
}

Write-Host ""

# ====================
# 4. TRANSAÇÕES
# ====================
Write-Host "4. TESTES DE TRANSAÇÕES" -ForegroundColor Yellow
Write-Host "---" -ForegroundColor DarkGray

# 4.1 Depósito na conta do João
try {
    $deposit1 = @{
        amount = 1000.00
        description = "Depósito teste"
    } | ConvertTo-Json
    
    $txDeposit = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId1/transactions/deposit" -Method Post -ContentType "application/json" -Headers $headers1 -Body $deposit1
    Test-Result "Depósito R$ 1.000 (João)" ($txDeposit.amount -eq 1000)
} catch {
    Test-Result "Depósito R$ 1.000 (João)" $false
}

# 4.2 Depósito na conta da Maria
try {
    $deposit2 = @{
        amount = 500.00
        description = "Depósito teste"
    } | ConvertTo-Json
    
    $txDeposit2 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId2/transactions/deposit" -Method Post -ContentType "application/json" -Headers $headers2 -Body $deposit2
    Test-Result "Depósito R$ 500 (Maria)" ($txDeposit2.amount -eq 500)
} catch {
    Test-Result "Depósito R$ 500 (Maria)" $false
}

# 4.3 Saque válido
try {
    $withdraw = @{
        amount = 200.00
        description = "Saque teste"
    } | ConvertTo-Json
    
    $txWithdraw = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId1/transactions/withdraw" -Method Post -ContentType "application/json" -Headers $headers1 -Body $withdraw
    Test-Result "Saque R$ 200 (João)" ($txWithdraw.amount -eq 200)
} catch {
    Test-Result "Saque R$ 200 (João)" $false
}

# 4.4 Saque acima do saldo deve falhar
try {
    $withdrawHigh = @{
        amount = 10000.00
        description = "Saque alto"
    } | ConvertTo-Json
    
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId1/transactions/withdraw" -Method Post -ContentType "application/json" -Headers $headers1 -Body $withdrawHigh
    Test-Result "Rejeitar saque acima do saldo" $false
} catch {
    Test-Result "Rejeitar saque acima do saldo" ($_.Exception.Response.StatusCode -eq 400)
}

# 4.5 Transferência entre contas
try {
    $transfer = @{
        targetAccountNumber = $accountNumber2
        amount = 100.00
        description = "Transferência teste"
    } | ConvertTo-Json
    
    $txTransfer = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId1/transactions/transfer" -Method Post -ContentType "application/json" -Headers $headers1 -Body $transfer
    Test-Result "Transferência R$ 100 (João → Maria)" ($txTransfer.amount -eq 100)
} catch {
    Write-Host "    Erro: $($_.Exception.Message)" -ForegroundColor Red
    Test-Result "Transferência R$ 100 (João → Maria)" $false
}

# 4.6 Transferência para conta inexistente
try {
    $transferInvalid = @{
        targetAccountNumber = "99999999-9"
        amount = 50.00
        description = "Transferência inválida"
    } | ConvertTo-Json
    
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId1/transactions/transfer" -Method Post -ContentType "application/json" -Headers $headers1 -Body $transferInvalid
    Test-Result "Rejeitar transferência para conta inexistente" $false
} catch {
    Test-Result "Rejeitar transferência para conta inexistente" ($_.Exception.Response.StatusCode -eq 400)
}

Write-Host ""

# ====================
# 5. EXTRATO
# ====================
Write-Host "5. TESTES DE EXTRATO" -ForegroundColor Yellow
Write-Host "---" -ForegroundColor DarkGray

# 5.1 Consultar extrato
try {
    $extrato = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId1/transactions?page=0&size=20" -Method Get -Headers $headers1
    Test-Result "Consultar extrato" ($extrato.content.Count -gt 0)
} catch {
    Test-Result "Consultar extrato" $false
}

# 5.2 Filtrar por tipo
try {
    $filtrado = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId1/transactions/filter?type=DEPOSITO&page=0&size=10" -Method Get -Headers $headers1
    Test-Result "Filtrar extrato por tipo" ($null -ne $filtrado)
} catch {
    Test-Result "Filtrar extrato por tipo" $false
}

# 5.3 Obter comprovante
try {
    if ($extrato.content.Count -gt 0) {
        $txId = $extrato.content[0].id
        $comprovante = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId1/transactions/$txId/receipt" -Method Get -Headers $headers1
        Test-Result "Obter comprovante" ($comprovante.id -eq $txId)
    } else {
        Test-Result "Obter comprovante" $false
    }
} catch {
    Test-Result "Obter comprovante" $false
}

Write-Host ""

# ====================
# 6. SEGURANÇA
# ====================
Write-Host "6. TESTES DE SEGURANÇA" -ForegroundColor Yellow
Write-Host "---" -ForegroundColor DarkGray

# 6.1 Acesso sem token
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts" -Method Get
    Test-Result "Bloquear acesso sem token" $false
} catch {
    $statusCode = [int]$_.Exception.Response.StatusCode.value__
    # Spring Security retorna 403 (Forbidden) quando não há token
    Test-Result "Bloquear acesso sem token" ($statusCode -eq 403 -or $statusCode -eq 401)
}

# 6.2 Token inválido
try {
    $headersInvalid = @{ Authorization = "Bearer token-invalido" }
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts" -Method Get -Headers $headersInvalid
    Test-Result "Bloquear token inválido" $false
} catch {
    $statusCode = [int]$_.Exception.Response.StatusCode.value__
    # Spring Security pode retornar 403 (Forbidden) ou 401 (Unauthorized)
    Test-Result "Bloquear token inválido" ($statusCode -eq 403 -or $statusCode -eq 401)
}

Write-Host ""

# ====================
# 7. VALIDAÇÕES
# ====================
Write-Host "7. TESTES DE VALIDAÇÃO" -ForegroundColor Yellow
Write-Host "---" -ForegroundColor DarkGray

# 7.1 Valor negativo
try {
    $negative = @{
        amount = -100.00
        description = "Teste negativo"
    } | ConvertTo-Json
    
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId1/transactions/deposit" -Method Post -ContentType "application/json" -Headers $headers1 -Body $negative
    Test-Result "Rejeitar valor negativo" $false
} catch {
    Test-Result "Rejeitar valor negativo" ($_.Exception.Response.StatusCode -eq 400)
}

# 7.2 Email inválido no registro
try {
    $invalidEmail = @{
        name = "Teste"
        email = "email-invalido"
        cpf = "52998224725"
        password = "senha123"
    } | ConvertTo-Json
    
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register" -Method Post -ContentType "application/json" -Body $invalidEmail
    Test-Result "Rejeitar email inválido" $false
} catch {
    Test-Result "Rejeitar email inválido" ($_.Exception.Response.StatusCode -eq 400)
}

Write-Host ""

# ====================
# RESUMO
# ====================
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "           RESUMO DOS TESTES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Testes Passados: $testsPassed" -ForegroundColor Green
Write-Host "Testes Falhados: $testsFailed" -ForegroundColor Red
Write-Host "Total: $($testsPassed + $testsFailed)" -ForegroundColor White
Write-Host ""

$successRate = [math]::Round(($testsPassed / ($testsPassed + $testsFailed)) * 100, 2)
Write-Host "Taxa de Sucesso: $successRate%" -ForegroundColor $(if ($successRate -ge 90) { "Green" } elseif ($successRate -ge 70) { "Yellow" } else { "Red" })

Write-Host ""
Write-Host "Documentação completa: ROTEIRO_DE_TESTES.md" -ForegroundColor Cyan
Write-Host ""

