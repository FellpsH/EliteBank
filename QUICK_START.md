# 🚀 Guia de Início Rápido - ApiBank

## 1️⃣ Clone e Execute em 2 Minutos

```bash
# Clone o repositório
git clone https://github.com/fellps/apibank.git
cd apibank

# Suba o ambiente completo
docker-compose up -d

# Aguarde ~30 segundos para a aplicação iniciar
```

✅ **Pronto!** A API está rodando em `http://localhost:8080`

---

## 2️⃣ Acesse a Documentação Interativa

Abra no navegador: **http://localhost:8080/swagger-ui.html**

---

## 3️⃣ Teste Rapidamente com curl

### Registrar Usuário

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@email.com",
    "cpf": "52998224725",
    "password": "senha123"
  }'
```

Você receberá um token JWT na resposta. Copie-o!

### Consultar Suas Contas

```bash
curl -X GET http://localhost:8080/api/v1/accounts \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

### Fazer um Depósito

```bash
curl -X POST http://localhost:8080/api/v1/accounts/1/transactions/deposit \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000.00,
    "description": "Meu primeiro depósito"
  }'
```

### Consultar Extrato

```bash
curl -X GET http://localhost:8080/api/v1/accounts/1/transactions \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

---

## 4️⃣ Importar no Postman

1. Importe o arquivo `postman_collection.json`
2. Configure a variável `jwt_token` com seu token
3. Explore todos os endpoints!

---

## 5️⃣ Verificar Status da API

```bash
curl http://localhost:8080/actuator/health
```

---

## 🛑 Parar o Ambiente

```bash
docker-compose down
```

---

## 📚 Próximos Passos

- Leia o [README.md](README.md) completo para entender a arquitetura
- Explore os testes em `src/test/`
- Veja as migrations em `src/main/resources/db/migration/`
- Personalize as configurações em `application.yml`

---

## ⚙️ Variáveis de Ambiente

Para ambientes de produção, configure:

- `JWT_SECRET`: Chave secreta para JWT
- `DB_PASSWORD`: Senha do banco de dados
- `DB_HOST`: Host do PostgreSQL

---

## 🐛 Problemas Comuns

### Porta 8080 já está em uso

```bash
# Altere a porta no docker-compose.yml
ports:
  - "8081:8080"
```

### PostgreSQL não iniciou

```bash
# Verifique os logs
docker-compose logs postgres

# Reinicie apenas o PostgreSQL
docker-compose restart postgres
```

### Erro de permissão no Docker

```bash
# Linux: adicione seu usuário ao grupo docker
sudo usermod -aG docker $USER
# Faça logout e login novamente
```

---

## 💬 Suporte

Encontrou algum problema? Abra uma issue no GitHub!

**Boa sorte e bom desenvolvimento! 🎉**

