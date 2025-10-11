# 🚀 Como Executar o ApiBank

## ⚡ Modo Rápido (Recomendado)

### 1. Inicie o projeto:
```powershell
.\start.ps1
```

### 2. Teste a API:
```powershell
.\test-api.ps1
```

### 3. Acesse o Swagger:
```
http://localhost:8080/swagger-ui.html
```

---

## 📋 Requisitos

Você precisa ter instalado:

- ✅ **Java 17** → https://adoptium.net/
- ✅ **Docker Desktop** → https://www.docker.com/products/docker-desktop/
- ✅ **Maven** (opcional) → https://maven.apache.org/

---

## 🐳 Comandos Docker

### Iniciar tudo:
```powershell
docker compose up -d
```

### Ver logs:
```powershell
docker compose logs -f
```

### Ver status:
```powershell
docker compose ps
```

### Parar tudo:
```powershell
docker compose down
```

### Limpar tudo (incluindo dados):
```powershell
docker compose down -v
```

---

## 🧪 Testes Manuais com curl

### 1. Health Check:
```powershell
curl http://localhost:8080/actuator/health
```

### 2. Registrar Usuário:
```powershell
curl -X POST http://localhost:8080/api/v1/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "name": "João Silva",
    "email": "joao@email.com",
    "cpf": "52998224725",
    "password": "senha123"
  }'
```

### 3. Login (copie o token da resposta):
```powershell
curl -X POST http://localhost:8080/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d '{
    "email": "joao@email.com",
    "password": "senha123"
  }'
```

### 4. Consultar Contas (use seu token):
```powershell
$token = "SEU_TOKEN_AQUI"
curl -H "Authorization: Bearer $token" http://localhost:8080/api/v1/accounts
```

### 5. Fazer Depósito:
```powershell
curl -X POST http://localhost:8080/api/v1/accounts/1/transactions/deposit `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{
    "amount": 1000.00,
    "description": "Depósito inicial"
  }'
```

---

## 🔧 Executar sem Docker (avançado)

Se você não quiser usar Docker, precisa:

1. **Instalar PostgreSQL localmente**
2. **Criar o banco de dados:**
   ```sql
   CREATE DATABASE apibank;
   CREATE USER apibank WITH PASSWORD 'apibank123';
   GRANT ALL PRIVILEGES ON DATABASE apibank TO apibank;
   ```

3. **Executar com Maven:**
   ```powershell
   mvn spring-boot:run
   ```

---

## ❓ Problemas Comuns

### "Docker não está instalado"
→ Instale o Docker Desktop e reinicie o PC

### "Porta 8080 já em uso"
→ Pare outros serviços ou mude a porta no `docker-compose.yml`

### "API não responde"
→ Aguarde 30 segundos após `docker compose up -d`

### "Erro ao conectar no banco"
→ Verifique se o PostgreSQL está rodando:
```powershell
docker compose ps
```

---

## 📚 Links Úteis

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **API Docs**: http://localhost:8080/api-docs

---

## 🎉 Tudo Funcionando?

Se a API respondeu, você está pronto! 🚀

Próximos passos:
1. Explore o Swagger UI
2. Importe o `postman_collection.json` no Postman
3. Leia o `README.md` para entender a arquitetura

