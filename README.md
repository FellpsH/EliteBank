# 🏦 ApiBank - API de Banco Digital

[![Java](https://img.shields.io/badge/Java-17-red)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

API RESTful completa de um banco digital desenvolvida com **Spring Boot 3**, simulando operações bancárias reais com segurança, auditoria e boas práticas de desenvolvimento.

---

## 📋 Sobre o Projeto

Este projeto foi desenvolvido como parte do meu portfólio profissional, com foco em demonstrar habilidades para **Java/Spring Boot**.

### ✨ Principais Destaques

- 🔐 **Autenticação e Autorização** com JWT e Spring Security
- 💰 **Operações Bancárias Completas** (Depósito, Saque, Transferência)
- 📊 **Extrato com Paginação e Filtros**
- 🔍 **Sistema de Auditoria** para rastreamento de todas operações
- 👥 **Controle de Acesso por Roles** (USER e ADMIN)
- 📝 **Validação de CPF** real
- 🐳 **Docker Compose** para ambiente completo
- 📚 **Documentação Swagger/OpenAPI**
- ✅ **Migrations com Flyway**
- 🧪 **Testes Automatizados**

---

## 🛠️ Tecnologias Utilizadas

### Core
- **Java 17**
- **Spring Boot 3.2.0**
- **Maven**

### Spring Ecosystem
- Spring Data JPA + Hibernate
- Spring Security
- Spring Validation
- Spring Actuator
- Spring Web

### Database
- PostgreSQL 16
- Flyway (Migrations)

### Security & JWT
- JWT (jsonwebtoken 0.12.3)
- BCrypt

### Documentation
- SpringDoc OpenAPI 3 (Swagger UI)

### DevOps
- Docker & Docker Compose
- Lombok

---

## 📐 Arquitetura

O projeto segue uma arquitetura em camadas bem definida:

```
src/main/java/com/fellps/apibank/
├── config/          # Configurações (Security, OpenAPI)
├── controller/      # Camada de apresentação (REST)
├── dto/            # Data Transfer Objects
├── enums/          # Enumerações (Role, AccountType, TransactionType)
├── exception/      # Tratamento de exceções customizado
├── model/          # Entidades JPA
├── repository/     # Camada de persistência
├── security/       # JWT e autenticação
├── service/        # Lógica de negócio
└── util/           # Utilitários (validadores, geradores)
```

---

## 🚀 Como Executar

### Pré-requisitos

- Docker e Docker Compose instalados
- Java 17+ (se for executar localmente sem Docker)
- Maven 3.6+ (se for executar localmente)

### Opção 1: Com Docker Compose (Recomendado)

```bash
# Clone o repositório
git clone https://github.com/fellps/apibank.git
cd apibank

# Suba o ambiente completo (banco + API)
docker-compose up -d

# A API estará disponível em http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### Opção 2: Execução Local

```bash
# 1. Suba apenas o PostgreSQL
docker-compose up -d postgres

# 2. Execute a aplicação
./mvnw spring-boot:run

# Ou compile e execute
./mvnw clean package
java -jar target/apibank-1.0.0.jar
```

---

## 📖 Documentação da API

Acesse a documentação interativa via Swagger UI:

**http://localhost:8080/swagger-ui.html**

### 🔑 Endpoints Principais

#### Autenticação

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/v1/auth/register` | Registrar novo usuário |
| POST | `/api/v1/auth/login` | Login e obtenção de token JWT |

#### Contas

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| GET | `/api/v1/accounts` | Listar minhas contas | ✅ |
| GET | `/api/v1/accounts/{id}` | Consultar saldo e dados | ✅ |

#### Transações

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| POST | `/api/v1/accounts/{id}/transactions/deposit` | Realizar depósito | ✅ |
| POST | `/api/v1/accounts/{id}/transactions/withdraw` | Realizar saque | ✅ |
| POST | `/api/v1/accounts/{id}/transactions/transfer` | Realizar transferência | ✅ |
| GET | `/api/v1/accounts/{id}/transactions` | Consultar extrato (paginado) | ✅ |
| GET | `/api/v1/accounts/{id}/transactions/filter` | Filtrar por tipo | ✅ |
| GET | `/api/v1/accounts/{id}/transactions/date-range` | Filtrar por período | ✅ |
| GET | `/api/v1/accounts/{id}/transactions/{txId}/receipt` | Obter comprovante | ✅ |

#### Administração (ROLE_ADMIN)

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| GET | `/api/v1/admin/users` | Listar todos usuários | 🔒 ADMIN |
| GET | `/api/v1/admin/transactions` | Listar todas transações | 🔒 ADMIN |
| GET | `/api/v1/admin/audit-logs` | Consultar logs de auditoria | 🔒 ADMIN |

#### Health Check

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/actuator/health` | Status da aplicação |

---

## 💡 Exemplos de Uso

### 1. Registrar Usuário

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@email.com",
    "cpf": "12345678901",
    "password": "senha123"
  }'
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "name": "João Silva",
  "email": "joao@email.com",
  "role": "USER"
}
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@email.com",
    "password": "senha123"
  }'
```

### 3. Consultar Contas

```bash
curl -X GET http://localhost:8080/api/v1/accounts \
  -H "Authorization: Bearer SEU_TOKEN_JWT"
```

### 4. Realizar Depósito

```bash
curl -X POST http://localhost:8080/api/v1/accounts/1/transactions/deposit \
  -H "Authorization: Bearer SEU_TOKEN_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000.00,
    "description": "Depósito inicial"
  }'
```

### 5. Realizar Transferência

```bash
curl -X POST http://localhost:8080/api/v1/accounts/1/transactions/transfer \
  -H "Authorization: Bearer SEU_TOKEN_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "targetAccountNumber": "00000002-1",
    "amount": 150.00,
    "description": "Pagamento"
  }'
```

### 6. Consultar Extrato (paginado)

```bash
curl -X GET "http://localhost:8080/api/v1/accounts/1/transactions?page=0&size=10" \
  -H "Authorization: Bearer SEU_TOKEN_JWT"
```

---

## 🗄️ Modelo de Dados

### Principais Entidades

#### User
- `id` (PK)
- `name`
- `email` (unique)
- `cpf` (unique, validado)
- `password` (BCrypt)
- `role` (USER, ADMIN)
- `active`
- `createdAt`, `updatedAt`

#### Account
- `id` (PK)
- `accountNumber` (unique, gerado automaticamente)
- `agency`
- `accountType` (CORRENTE, POUPANCA)
- `balance` (nunca negativo)
- `active`
- `userId` (FK)
- `createdAt`, `updatedAt`

#### Transaction
- `id` (PK)
- `transactionType` (DEPOSITO, SAQUE, TRANSFERENCIA_*)
- `amount`
- `description`
- `transactionDate`
- `accountId` (FK)
- `targetAccountId` (FK, nullable)
- `reversed`
- `createdAt`

#### AuditLog
- `id` (PK)
- `entityName`
- `entityId`
- `action`
- `performedBy` (FK)
- `performedAt`
- `oldValue`, `newValue` (JSON)
- `ipAddress`
- `description`

---

## 🔒 Segurança

### Autenticação JWT

Todas as rotas protegidas requerem um token JWT válido no header:

```
Authorization: Bearer {token}
```

### Roles e Permissões

- **USER**: Acesso às próprias contas e transações
- **ADMIN**: Acesso total, incluindo endpoints administrativos

### Validações

- ✅ CPF válido (algoritmo completo de validação)
- ✅ Email único
- ✅ Senhas criptografadas com BCrypt
- ✅ Saldo nunca negativo
- ✅ Validação de propriedade de recursos

---

## 📊 Auditoria

Todo o sistema possui rastreamento completo de ações:

- Criação de usuários
- Todas as transações
- Alterações em contas
- Quem fez, quando, e o que mudou

Logs de auditoria podem ser consultados por administradores via endpoint `/api/v1/admin/audit-logs`.

---

## 🧪 Testes

```bash
# Executar todos os testes
./mvnw test

# Executar com coverage
./mvnw verify
```

---

## 📦 Estrutura do Docker Compose

```yaml
services:
  postgres:    # PostgreSQL 16
  api:         # Spring Boot Application
```

### Variáveis de Ambiente

Configure via arquivo `.env` (use `.env.example` como referência):

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=apibank
DB_USERNAME=apibank
DB_PASSWORD=apibank123
JWT_SECRET=sua-chave-secreta
JWT_EXPIRATION=86400000
SERVER_PORT=8080
```

---

## 📈 Próximas Melhorias (Roadmap)

- [ ] Agendamento de transferências
- [ ] PIX (chave PIX, QR Code)
- [ ] Rate Limiting com Bucket4j
- [ ] Notificações por e-mail/webhook
- [ ] Análise de crédito
- [ ] Cartão de crédito virtual
- [ ] CI/CD com GitHub Actions
- [ ] Observabilidade (Prometheus + Grafana)

---

## 👨‍💻 Autor

**Fellipe Babeto**

- GitHub: [@fellps](https://github.com/fellps)
- LinkedIn: [Fellipe Babeto](https://linkedin.com/in/fellipe-babeto)
- Email: seu-email@exemplo.com

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 🙏 Agradecimentos

Projeto desenvolvido como parte do meu portfólio profissional para demonstrar habilidades em:

- ✅ Desenvolvimento Java/Spring Boot avançado
- ✅ Arquitetura de microsserviços
- ✅ Segurança e autenticação
- ✅ Boas práticas e padrões de projeto
- ✅ Docker e containerização
- ✅ Documentação técnica

---

## 📞 Contato

Para dúvidas, sugestões ou oportunidades profissionais, entre em contato!

**⭐ Se este projeto foi útil, considere dar uma estrela no GitHub!**

