# 🧪 Roteiro Completo de Testes - ApiBank

## 📋 Índice
1. [Preparação](#preparação)
2. [Testes de Autenticação](#1-testes-de-autenticação)
3. [Testes de Contas](#2-testes-de-contas)
4. [Testes de Transações](#3-testes-de-transações)
5. [Testes de Extrato](#4-testes-de-extrato)
6. [Testes Administrativos](#5-testes-administrativos)
7. [Testes de Segurança](#6-testes-de-segurança)
8. [Testes de Validação](#7-testes-de-validação)
9. [Checklist Final](#checklist-final)

---

## Preparação

### Iniciar a API
```powershell
docker compose up -d
Start-Sleep -Seconds 15
```

### Verificar Health Check
```powershell
curl http://localhost:8080/actuator/health
```

**Resultado Esperado:**
```json
{"status":"UP"}
```

---

## 1. Testes de Autenticação

### 1.1 ✅ Registrar Primeiro Usuário (Caso de Sucesso)

**Endpoint:** `POST /api/v1/auth/register`

**Payload:**
```json
{
  "name": "João Silva",
  "email": "joao@email.com",
  "cpf": "52998224725",
  "password": "senha123"
}
```

**PowerShell:**
```powershell
$registerBody = @{
    name = "João Silva"
    email = "joao@email.com"
    cpf = "52998224725"
    password = "senha123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register" -Method Post -ContentType "application/json" -Body $registerBody
$token1 = $response.token
Write-Host "Token do João: $($token1.Substring(0,20))..."
Write-Host "User ID: $($response.userId)"
```

**Resultado Esperado:**
- Status: 201 Created
- Retorna: token, userId, name, email, role

---

### 1.2 ✅ Registrar Segundo Usuário

**Payload:**
```json
{
  "name": "Maria Santos",
  "email": "maria@email.com",
  "cpf": "11144477735",
  "password": "senha456"
}
```

**PowerShell:**
```powershell
$registerBody2 = @{
    name = "Maria Santos"
    email = "maria@email.com"
    cpf = "11144477735"
    password = "senha456"
} | ConvertTo-Json

$response2 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register" -Method Post -ContentType "application/json" -Body $registerBody2
$token2 = $response2.token
Write-Host "Token da Maria: $($token2.Substring(0,20))..."
```

---

### 1.3 ❌ Tentar Registrar com Email Duplicado (Caso de Erro)

**Payload:**
```json
{
  "name": "Pedro Oliveira",
  "email": "joao@email.com",
  "cpf": "12345678909",
  "password": "senha789"
}
```

**Resultado Esperado:**
- Status: 400 Bad Request
- Mensagem: "E-mail já cadastrado"

---

### 1.4 ❌ Tentar Registrar com CPF Duplicado

**Payload:**
```json
{
  "name": "Pedro Oliveira",
  "email": "pedro@email.com",
  "cpf": "52998224725",
  "password": "senha789"
}
```

**Resultado Esperado:**
- Status: 400 Bad Request
- Mensagem: "CPF já cadastrado"

---

### 1.5 ❌ Tentar Registrar com CPF Inválido

**Payload:**
```json
{
  "name": "Carlos Silva",
  "email": "carlos@email.com",
  "cpf": "12345678901",
  "password": "senha789"
}
```

**Resultado Esperado:**
- Status: 400 Bad Request
- Mensagem: "CPF inválido"

---

### 1.6 ✅ Login com Credenciais Corretas

**Endpoint:** `POST /api/v1/auth/login`

**Payload:**
```json
{
  "email": "joao@email.com",
  "password": "senha123"
}
```

**PowerShell:**
```powershell
$loginBody = @{
    email = "joao@email.com"
    password = "senha123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method Post -ContentType "application/json" -Body $loginBody
$token1 = $loginResponse.token
Write-Host "Login bem-sucedido!"
```

**Resultado Esperado:**
- Status: 200 OK
- Retorna: token JWT válido

---

### 1.7 ❌ Login com Senha Incorreta

**Payload:**
```json
{
  "email": "joao@email.com",
  "password": "senhaErrada"
}
```

**Resultado Esperado:**
- Status: 401 Unauthorized
- Mensagem: "Credenciais inválidas"

---

## 2. Testes de Contas

### 2.1 ✅ Consultar Minhas Contas

**Endpoint:** `GET /api/v1/accounts`

**PowerShell:**
```powershell
$headers = @{
    Authorization = "Bearer $token1"
}

$accounts = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts" -Method Get -Headers $headers
Write-Host "Contas encontradas: $($accounts.Count)"
$accountId1 = $accounts[0].id
$accountNumber1 = $accounts[0].accountNumber
Write-Host "Conta do João: $accountNumber1 (ID: $accountId1)"
Write-Host "Saldo: R`$ $($accounts[0].balance)"
```

**Resultado Esperado:**
- Status: 200 OK
- Retorna: array com 1 conta
- Conta tem: id, accountNumber, agency, accountType, balance=0

---

### 2.2 ✅ Consultar Conta Específica

**Endpoint:** `GET /api/v1/accounts/{id}`

**PowerShell:**
```powershell
$account = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId1" -Method Get -Headers $headers
Write-Host "Número da conta: $($account.accountNumber)"
Write-Host "Agência: $($account.agency)"
Write-Host "Tipo: $($account.accountType)"
Write-Host "Saldo: R`$ $($account.balance)"
```

**Resultado Esperado:**
- Status: 200 OK
- Retorna: dados completos da conta

---

### 2.3 ❌ Tentar Acessar Conta de Outro Usuário

**PowerShell:**
```powershell
# Pegar ID da conta da Maria
$headers2 = @{ Authorization = "Bearer $token2" }
$accountsMaria = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts" -Method Get -Headers $headers2
$accountIdMaria = $accountsMaria[0].id

# João tentar acessar conta da Maria (deve falhar)
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountIdMaria" -Method Get -Headers $headers
} catch {
    Write-Host "✓ Erro esperado: Não pode acessar conta de outro usuário"
}
```

**Resultado Esperado:**
- Status: 404 Not Found
- Mensagem: "Conta não encontrada"

---

## 3. Testes de Transações

### 3.1 ✅ Realizar Depósito

**Endpoint:** `POST /api/v1/accounts/{id}/transactions/deposit`

**Payload:**
```json
{
  "amount": 1000.00,
  "description": "Depósito inicial"
}
```

**PowerShell:**
```powershell
$depositBody = @{
    amount = 1000.00
    description = "Depósito inicial"
} | ConvertTo-Json

$deposit = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId1/transactions/deposit" -Method Post -ContentType "application/json" -Headers $headers -Body $depositBody
Write-Host "Depósito realizado!"
Write-Host "ID da transação: $($deposit.id)"
Write-Host "Valor: R`$ $($deposit.amount)"
```

**Resultado Esperado:**
- Status: 200 OK
- Saldo da conta: R$ 1.000,00
- Retorna: dados da transação

---

### 3.2 ✅ Fazer Segundo Depósito

**Payload:**
```json
{
  "amount": 500.00,
  "description": "Depósito adicional"
}
```

**PowerShell:**
```powershell
$depositBody2 = @{
    amount = 500.00
    description = "Depósito adicional"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId1/transactions/deposit" -Method Post -ContentType "application/json" -Headers $headers -Body $depositBody2
Write-Host "Segundo depósito realizado! Saldo agora: R$ 1.500,00"
```

**Resultado Esperado:**
- Saldo da conta: R$ 1.500,00

---

### 3.3 ✅ Realizar Saque

**Endpoint:** `POST /api/v1/accounts/{id}/transactions/withdraw`

**Payload:**
```json
{
  "amount": 200.00,
  "description": "Saque no caixa eletrônico"
}
```

**PowerShell:**
```powershell
$withdrawBody = @{
    amount = 200.00
    description = "Saque no caixa eletrônico"
} | ConvertTo-Json

$withdraw = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId1/transactions/withdraw" -Method Post -ContentType "application/json" -Headers $headers -Body $withdrawBody
Write-Host "Saque realizado!"
Write-Host "Saldo restante: R$ 1.300,00"
```

**Resultado Esperado:**
- Status: 200 OK
- Saldo da conta: R$ 1.300,00

---

### 3.4 ❌ Tentar Sacar Mais que o Saldo

**Payload:**
```json
{
  "amount": 5000.00,
  "description": "Tentativa de saque alto"
}
```

**PowerShell:**
```powershell
try {
    $withdrawBody = @{
        amount = 5000.00
        description = "Tentativa de saque alto"
    } | ConvertTo-Json
    
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId1/transactions/withdraw" -Method Post -ContentType "application/json" -Headers $headers -Body $withdrawBody
} catch {
    Write-Host "✓ Erro esperado: Saldo insuficiente"
}
```

**Resultado Esperado:**
- Status: 400 Bad Request
- Mensagem: "Saldo insuficiente"

---

### 3.5 ✅ Realizar Transferência Entre Contas

**Endpoint:** `POST /api/v1/accounts/{id}/transactions/transfer`

**Passo 1:** Depositar na conta da Maria
```powershell
$depositMaria = @{
    amount = 500.00
    description = "Depósito inicial Maria"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountIdMaria/transactions/deposit" -Method Post -ContentType "application/json" -Headers $headers2 -Body $depositMaria
```

**Passo 2:** João transfere para Maria
```powershell
$accountNumberMaria = $accountsMaria[0].accountNumber

$transferBody = @{
    targetAccountNumber = $accountNumberMaria
    amount = 300.00
    description = "Pagamento para Maria"
} | ConvertTo-Json

$transfer = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId1/transactions/transfer" -Method Post -ContentType "application/json" -Headers $headers -Body $transferBody
Write-Host "Transferência realizada!"
Write-Host "De: $accountNumber1"
Write-Host "Para: $accountNumberMaria"
Write-Host "Valor: R`$ 300,00"
```

**Resultado Esperado:**
- Status: 200 OK
- Saldo João: R$ 1.000,00 (1.300 - 300)
- Saldo Maria: R$ 800,00 (500 + 300)

---

### 3.6 ❌ Tentar Transferir para Conta Inexistente

**Payload:**
```json
{
  "targetAccountNumber": "99999999-9",
  "amount": 50.00,
  "description": "Transferência inválida"
}
```

**Resultado Esperado:**
- Status: 400 Bad Request
- Mensagem: "Conta destino não encontrada"

---

### 3.7 ❌ Tentar Transferir para a Própria Conta

**Payload:**
```json
{
  "targetAccountNumber": "[seu próprio número]",
  "amount": 50.00,
  "description": "Auto transferência"
}
```

**Resultado Esperado:**
- Status: 400 Bad Request
- Mensagem: "Não é possível transferir para a mesma conta"

---

## 4. Testes de Extrato

### 4.1 ✅ Consultar Extrato Completo

**Endpoint:** `GET /api/v1/accounts/{id}/transactions`

**PowerShell:**
```powershell
$extrato = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId1/transactions?page=0&size=20" -Method Get -Headers $headers
Write-Host "Total de transações: $($extrato.totalElements)"
Write-Host "Transações na página: $($extrato.content.Count)"

foreach ($tx in $extrato.content) {
    Write-Host "---"
    Write-Host "Tipo: $($tx.transactionType)"
    Write-Host "Valor: R`$ $($tx.amount)"
    Write-Host "Descrição: $($tx.description)"
    Write-Host "Data: $($tx.transactionDate)"
}
```

**Resultado Esperado:**
- Status: 200 OK
- Retorna: página com todas as transações
- Paginação funcionando

---

### 4.2 ✅ Filtrar Extrato por Tipo

**Endpoint:** `GET /api/v1/accounts/{id}/transactions/filter?type=DEPOSITO`

**PowerShell:**
```powershell
$depositos = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId1/transactions/filter?type=DEPOSITO&page=0&size=10" -Method Get -Headers $headers
Write-Host "Depósitos encontrados: $($depositos.totalElements)"
```

**Testar cada tipo:**
- `DEPOSITO`
- `SAQUE`
- `TRANSFERENCIA_ENVIADA`
- `TRANSFERENCIA_RECEBIDA`

---

### 4.3 ✅ Filtrar Extrato por Período

**Endpoint:** `GET /api/v1/accounts/{id}/transactions/date-range`

**PowerShell:**
```powershell
$hoje = Get-Date
$ontem = $hoje.AddDays(-1)
$startDate = $ontem.ToString("yyyy-MM-ddTHH:mm:ss")
$endDate = $hoje.ToString("yyyy-MM-ddTHH:mm:ss")

$extratoFiltrado = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId1/transactions/date-range?startDate=$startDate&endDate=$endDate&page=0&size=10" -Method Get -Headers $headers
Write-Host "Transações no período: $($extratoFiltrado.totalElements)"
```

---

### 4.4 ✅ Obter Comprovante de Transação

**Endpoint:** `GET /api/v1/accounts/{id}/transactions/{txId}/receipt`

**PowerShell:**
```powershell
$primeiraTransacao = $extrato.content[0].id

$comprovante = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts/$accountId1/transactions/$primeiraTransacao/receipt" -Method Get -Headers $headers
Write-Host "=== COMPROVANTE ==="
Write-Host "ID: $($comprovante.id)"
Write-Host "Tipo: $($comprovante.transactionType)"
Write-Host "Valor: R`$ $($comprovante.amount)"
Write-Host "Data: $($comprovante.transactionDate)"
Write-Host "Descrição: $($comprovante.description)"
```

---

## 5. Testes Administrativos

### 5.1 Criar Usuário Admin

**Importante:** Para ter um admin, você precisa inserir manualmente no banco ou criar via migration.

**SQL para criar admin:**
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'joao@email.com';
```

**Via Docker:**
```powershell
docker exec -it apibank-postgres psql -U apibank -d apibank -c "UPDATE users SET role = 'ADMIN' WHERE email = 'joao@email.com';"
```

Depois, faça login novamente para obter um token admin:
```powershell
$adminLogin = @{
    email = "joao@email.com"
    password = "senha123"
} | ConvertTo-Json

$adminResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method Post -ContentType "application/json" -Body $adminLogin
$tokenAdmin = $adminResponse.token
Write-Host "Token Admin obtido!"
```

---

### 5.2 ✅ Listar Todos os Usuários (Admin)

**Endpoint:** `GET /api/v1/admin/users`

**PowerShell:**
```powershell
$headersAdmin = @{
    Authorization = "Bearer $tokenAdmin"
}

$usuarios = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/users?page=0&size=10" -Method Get -Headers $headersAdmin
Write-Host "Total de usuários: $($usuarios.totalElements)"

foreach ($user in $usuarios.content) {
    Write-Host "---"
    Write-Host "ID: $($user.id)"
    Write-Host "Nome: $($user.name)"
    Write-Host "Email: $($user.email)"
    Write-Host "Role: $($user.role)"
}
```

---

### 5.3 ✅ Listar Todas as Transações (Admin)

**Endpoint:** `GET /api/v1/admin/transactions`

**PowerShell:**
```powershell
$todasTransacoes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/transactions?page=0&size=20" -Method Get -Headers $headersAdmin
Write-Host "Total de transações no sistema: $($todasTransacoes.totalElements)"
```

---

### 5.4 ✅ Consultar Logs de Auditoria (Admin)

**Endpoint:** `GET /api/v1/admin/audit-logs`

**PowerShell:**
```powershell
$auditLogs = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/audit-logs?page=0&size=20" -Method Get -Headers $headersAdmin
Write-Host "Total de logs: $($auditLogs.totalElements)"

foreach ($log in $auditLogs.content) {
    Write-Host "---"
    Write-Host "Ação: $($log.action)"
    Write-Host "Entidade: $($log.entityName) (ID: $($log.entityId))"
    Write-Host "Data: $($log.performedAt)"
    Write-Host "Descrição: $($log.description)"
}
```

---

### 5.5 ❌ Tentar Acessar Endpoint Admin sem Ser Admin

**PowerShell:**
```powershell
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/users" -Method Get -Headers $headers
} catch {
    Write-Host "✓ Erro esperado: Acesso negado (não é admin)"
}
```

**Resultado Esperado:**
- Status: 403 Forbidden
- Mensagem: "Acesso negado"

---

## 6. Testes de Segurança

### 6.1 ❌ Tentar Acessar Endpoint sem Token

**PowerShell:**
```powershell
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts" -Method Get
} catch {
    Write-Host "✓ Erro esperado: Token não fornecido"
}
```

**Resultado Esperado:**
- Status: 401 Unauthorized

---

### 6.2 ❌ Tentar Acessar com Token Inválido

**PowerShell:**
```powershell
$headersInvalid = @{
    Authorization = "Bearer token-invalido-123"
}

try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/accounts" -Method Get -Headers $headersInvalid
} catch {
    Write-Host "✓ Erro esperado: Token inválido"
}
```

---

### 6.3 ❌ Tentar Acessar com Token Expirado

*(Necessário aguardar o tempo de expiração do token - 24h por padrão)*

---

## 7. Testes de Validação

### 7.1 ❌ Campos Obrigatórios

**Teste cada endpoint sem campos obrigatórios:**

```powershell
# Registro sem nome
try {
    $invalid = @{
        email = "teste@email.com"
        cpf = "12345678909"
        password = "senha"
    } | ConvertTo-Json
    
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register" -Method Post -ContentType "application/json" -Body $invalid
} catch {
    Write-Host "✓ Validação OK: Nome é obrigatório"
}
```

---

### 7.2 ❌ Formatos Inválidos

**Email inválido:**
```json
{
  "name": "Teste",
  "email": "email-invalido",
  "cpf": "52998224725",
  "password": "senha123"
}
```

**CPF com menos de 11 dígitos:**
```json
{
  "name": "Teste",
  "email": "teste@email.com",
  "cpf": "123456",
  "password": "senha123"
}
```

---

### 7.3 ❌ Valores Inválidos

**Depósito com valor negativo:**
```json
{
  "amount": -100.00,
  "description": "Teste negativo"
}
```

**Depósito com valor zero:**
```json
{
  "amount": 0,
  "description": "Teste zero"
}
```

---

## 8. Testes de Performance (Opcional)

### 8.1 Teste de Carga Básico

**Criar múltiplos usuários:**
```powershell
for ($i = 1; $i -le 10; $i++) {
    $userBody = @{
        name = "Usuario $i"
        email = "usuario$i@email.com"
        cpf = "$(Get-Random -Minimum 10000000000 -Maximum 99999999999)"
        password = "senha$i"
    } | ConvertTo-Json
    
    try {
        Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register" -Method Post -ContentType "application/json" -Body $userBody
        Write-Host "Usuário $i criado"
    } catch {
        Write-Host "Erro ao criar usuário $i"
    }
}
```

---

## Checklist Final

### ✅ Funcionalidades Core
- [ ] Registro de usuário funciona
- [ ] Login funciona
- [ ] Conta é criada automaticamente
- [ ] Depósito funciona
- [ ] Saque funciona
- [ ] Transferência funciona
- [ ] Extrato funciona
- [ ] Paginação funciona
- [ ] Filtros funcionam

### ✅ Segurança
- [ ] JWT é obrigatório
- [ ] Não acessa recursos de outros usuários
- [ ] Admin tem acesso privilegiado
- [ ] Validações de entrada funcionam
- [ ] CPF é validado corretamente

### ✅ Regras de Negócio
- [ ] Não permite saldo negativo
- [ ] Não permite saque maior que saldo
- [ ] Validações impedem dados inválidos
- [ ] Auditoria registra ações

### ✅ Documentação
- [ ] Swagger UI está acessível
- [ ] Health check responde
- [ ] README está completo

---

## 🎯 Próximos Passos

Após executar todos os testes:

1. ✅ **Documente os resultados** - Anote quais testes passaram
2. ✅ **Corrija bugs encontrados** - Se algum teste falhar
3. ✅ **Adicione mais testes** - Pense em edge cases
4. ✅ **Automatize** - Crie scripts de teste
5. ✅ **Deploy** - Suba em produção com confiança!

---

## 📚 Recursos Adicionais

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Postman Collection:** Importe o arquivo `postman_collection.json`
- **Logs:** `docker compose logs -f api`

---

**Bons testes! 🚀**

