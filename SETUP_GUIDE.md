# 🚀 GasTracker - Guia Completo de Setup e Execução

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Pré-requisitos](#pré-requisitos)
3. [Configuração do Telegram Bot](#configuração-do-telegram-bot)
4. [Configuração da Etherscan API](#configuração-da-etherscan-api)
5. [Execução Local](#execução-local)
6. [Deploy no Render](#deploy-no-render)
7. [Segurança e Boas Práticas](#segurança-e-boas-práticas)
8. [Testes](#testes)
9. [Troubleshooting](#troubleshooting)

---

## 🎯 Visão Geral

**GasTracker** é um sistema de alertas de gas price da Ethereum que notifica usuários via Telegram quando o preço do gas está abaixo do threshold configurado.

### Funcionalidades

- ✅ Landing page para criação de alertas
- ✅ Bot do Telegram para receber notificações
- ✅ Cron job que verifica gas price a cada 5 minutos
- ✅ API REST completa
- ✅ Proteção contra XSS, SQL Injection e Rate Limiting
- ✅ Sanitização de inputs
- ✅ Sistema de cooldown (1 hora entre alertas)

### Stack Tecnológica

- **Backend**: Java 17 + Spring Boot 3.2.0
- **Database**: PostgreSQL (produção) / H2 (dev)
- **Bot**: Telegram Bot API
- **Gas Price**: Etherscan API
- **Infra**: Docker + Render.com
- **Security**: OWASP HTML Sanitizer + Bucket4j (Rate Limiting)

---

## 🔧 Pré-requisitos

### Software Necessário

- Java 17 ou superior
- Maven 3.9+
- Docker (opcional, para desenvolvimento)
- Git

### Verificação de Instalação

```bash
java -version
# Deve mostrar: java version "17.x.x"

mvn -version
# Deve mostrar: Apache Maven 3.9.x

docker --version
# Deve mostrar: Docker version 24.x.x
```

---

## 🤖 Configuração do Telegram Bot

### Passo 1: Criar Bot no Telegram

1. Abra o Telegram e busque por `@BotFather`
2. Digite `/newbot`
3. Escolha um nome para o bot (ex: `GasTracker Alert Bot`)
4. Escolha um username (ex: `GasTrackerAlertBot`)
5. Copie o **token** fornecido (formato: `123456789:ABCdefGHIjklMNOpqrsTUVwxyz`)

### Passo 2: Configurar Comandos do Bot

1. No BotFather, digite `/setcommands`
2. Selecione seu bot
3. Cole os comandos:

```
start - Ativar alertas de gas price
status - Ver configuração atual
stop - Desativar alertas
```

### Passo 3: Salvar Credenciais

Anote:
- **Bot Token**: `123456789:ABCdefGHIjklMNOpqrsTUVwxyz`
- **Bot Username**: `GasTrackerAlertBot`

---

## 🔑 Configuração da Etherscan API

### Passo 1: Criar Conta na Etherscan

1. Acesse: https://etherscan.io/register
2. Crie uma conta gratuita

### Passo 2: Gerar API Key

1. Acesse: https://etherscan.io/myapikey
2. Clique em **"Add"**
3. Nomeie a API Key (ex: `GasTracker`)
4. Copie a **API Key** gerada

### Passo 3: Salvar Credenciais

Anote:
- **Etherscan API Key**: `XXXXXXXXXXXXXXXXXXXXXXXXXX`

---

## 💻 Execução Local

### Opção 1: Usando Maven (Recomendado para Dev)

#### 1. Clone o Repositório

```bash
git clone <seu-repositorio>
cd GasTracker
```

#### 2. Configure Variáveis de Ambiente

**Windows (PowerShell):**

```powershell
$env:TELEGRAM_BOT_TOKEN="seu-telegram-bot-token"
$env:TELEGRAM_BOT_USERNAME="seu-bot-username"
$env:ETHERSCAN_API_KEY="sua-etherscan-api-key"
```

**Linux/Mac (Bash):**

```bash
export TELEGRAM_BOT_TOKEN="seu-telegram-bot-token"
export TELEGRAM_BOT_USERNAME="seu-bot-username"
export ETHERSCAN_API_KEY="sua-etherscan-api-key"
```

#### 3. Execute a Aplicação

```bash
mvn spring-boot:run
```

#### 4. Acesse a Aplicação

- **Landing Page**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:gastracker`
  - Username: `sa`
  - Password: *(deixe em branco)*

---

### Opção 2: Usando Docker

#### 1. Build da Imagem

```bash
docker build -t gastracker:latest .
```

#### 2. Execute o Container

```bash
docker run -d \
  -p 8080:8080 \
  -e TELEGRAM_BOT_TOKEN="seu-telegram-bot-token" \
  -e TELEGRAM_BOT_USERNAME="seu-bot-username" \
  -e ETHERSCAN_API_KEY="sua-etherscan-api-key" \
  -e SPRING_PROFILES_ACTIVE="production" \
  -e POSTGRES_URL="jdbc:postgresql://seu-db:5432/gastracker" \
  -e POSTGRES_USER="seu-usuario" \
  -e POSTGRES_PASSWORD="sua-senha" \
  gastracker:latest
```

---

### Opção 3: Docker Compose (Ambiente Completo)

#### 1. Crie o arquivo `docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: gastracker
      POSTGRES_USER: gastracker_user
      POSTGRES_PASSWORD: gastracker_pass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: production
      POSTGRES_URL: jdbc:postgresql://postgres:5432/gastracker
      POSTGRES_USER: gastracker_user
      POSTGRES_PASSWORD: gastracker_pass
      TELEGRAM_BOT_TOKEN: ${TELEGRAM_BOT_TOKEN}
      TELEGRAM_BOT_USERNAME: ${TELEGRAM_BOT_USERNAME}
      ETHERSCAN_API_KEY: ${ETHERSCAN_API_KEY}
      PORT: 8080
    depends_on:
      - postgres

volumes:
  postgres_data:
```

#### 2. Crie arquivo `.env`:

```env
TELEGRAM_BOT_TOKEN=seu-telegram-bot-token
TELEGRAM_BOT_USERNAME=seu-bot-username
ETHERSCAN_API_KEY=sua-etherscan-api-key
```

#### 3. Execute:

```bash
docker-compose up -d
```

---

## 🚀 Deploy no Render

### Passo 1: Preparar Repositório

1. Crie um repositório no GitHub
2. Faça push do código:

```bash
git init
git add .
git commit -m "Initial commit - GasTracker"
git remote add origin <seu-repo-url>
git push -u origin main
```

### Passo 2: Criar Conta no Render

1. Acesse: https://render.com
2. Faça login com GitHub

### Passo 3: Criar Database PostgreSQL

1. No Dashboard do Render, clique em **"New +"**
2. Selecione **"PostgreSQL"**
3. Configure:
   - **Name**: `gastracker-db`
   - **Database**: `gastracker`
   - **User**: `gastracker_user`
   - **Region**: `Oregon (US West)`
   - **Plan**: `Free`
4. Clique em **"Create Database"**
5. Anote as credenciais:
   - Internal Database URL
   - Username
   - Password

### Passo 4: Criar Web Service

1. Clique em **"New +"** → **"Web Service"**
2. Conecte seu repositório GitHub
3. Configure:
   - **Name**: `gastracker`
   - **Region**: `Oregon (US West)`
   - **Branch**: `main`
   - **Runtime**: `Docker`
   - **Plan**: `Free`

### Passo 5: Configurar Environment Variables

No painel de Environment Variables, adicione:

```
PORT=10000
SPRING_PROFILES_ACTIVE=production
POSTGRES_URL=<internal-database-url-do-render>
POSTGRES_USER=gastracker_user
POSTGRES_PASSWORD=<password-do-banco>
TELEGRAM_BOT_TOKEN=<seu-telegram-bot-token>
TELEGRAM_BOT_USERNAME=<seu-bot-username>
ETHERSCAN_API_KEY=<sua-etherscan-api-key>
```

### Passo 6: Deploy

1. Clique em **"Create Web Service"**
2. Aguarde o build (5-10 minutos)
3. Acesse a URL fornecida (ex: `https://gastracker.onrender.com`)

### Passo 7: Configurar UptimeRobot (Evitar Sleep)

1. Acesse: https://uptimerobot.com
2. Crie uma conta gratuita
3. Adicione novo monitor:
   - **Monitor Type**: HTTP(s)
   - **Friendly Name**: GasTracker
   - **URL**: `https://gastracker.onrender.com/api/health`
   - **Monitoring Interval**: 5 minutes
4. Salve

---

## 🔒 Segurança e Boas Práticas

### Medidas de Segurança Implementadas

#### 1. **Validação de Inputs**
- Username do Telegram: Regex pattern `^[a-zA-Z0-9_]{5,32}$`
- Gas Price: Range de 1-1000 Gwei
- Sanitização com OWASP HTML Sanitizer

#### 2. **Rate Limiting**
- 10 requisições por IP a cada 10 minutos
- Implementado com Bucket4j

#### 3. **Proteção XSS**
- Sanitização de todos os inputs
- Remoção de tags `<script>`
- Content Security Policy

#### 4. **Proteção contra SSRF**
- Validação de URLs da Etherscan API
- Nenhuma URL externa aceita de usuários

#### 5. **Segurança do Telegram Bot**
- Validação de chatId e username
- Cooldown de 1 hora entre alertas
- Desativação automática de usuários inativos

#### 6. **Database Security**
- Prepared statements (JPA protege contra SQL Injection)
- Conexão via SSL (Render.com)
- Credenciais via variáveis de ambiente

#### 7. **Container Security**
- Execução com usuário não-root
- Imagem Alpine Linux (menor superfície de ataque)
- Multi-stage build (sem ferramentas de build em produção)

### Checklist de Segurança

- [ ] **NÃO** commitar `.env` ou credenciais no Git
- [ ] Usar variáveis de ambiente para secrets
- [ ] Ativar HTTPS em produção (Render já fornece)
- [ ] Revisar logs regularmente
- [ ] Manter dependências atualizadas
- [ ] Limitar permissões do bot do Telegram
- [ ] Configurar firewall no servidor (se aplicável)

### Variáveis de Ambiente Sensíveis

**NUNCA** exponha:
- `TELEGRAM_BOT_TOKEN`
- `ETHERSCAN_API_KEY`
- `POSTGRES_PASSWORD`

**Como proteger:**
```bash
# Adicione ao .gitignore
echo ".env" >> .gitignore
echo ".env.local" >> .gitignore
echo ".env.production" >> .gitignore
```

---

## 🧪 Testes

### Teste Manual - Local

#### 1. Criar Alerta

```bash
curl -X POST http://localhost:8080/api/alert \
  -H "Content-Type: application/json" \
  -d '{
    "telegramUsername": "seu_username",
    "maxGasPrice": 30
  }'
```

**Resposta esperada:**
```json
{
  "telegramUsername": "seu_username",
  "maxGasPrice": 30,
  "message": "Alerta criado com sucesso! Use /start no bot..."
}
```

#### 2. Consultar Gas Price Atual

```bash
curl http://localhost:8080/api/gas-price
```

**Resposta esperada:**
```json
{
  "gasPrice": 25,
  "unit": "Gwei",
  "timestamp": 1696441234567
}
```

#### 3. Consultar Estatísticas

```bash
curl http://localhost:8080/api/stats
```

**Resposta esperada:**
```json
{
  "activeUsers": 1,
  "totalAlerts24h": 5,
  "successfulAlerts24h": 5
}
```

#### 4. Health Check

```bash
curl http://localhost:8080/api/health
```

**Resposta esperada:**
```json
{
  "status": "UP"
}
```

### Teste do Bot Telegram

1. Abra o Telegram
2. Busque por `@seu_bot_username`
3. Digite `/start`
4. Verifique se recebe mensagem de boas-vindas
5. Digite `/status` para ver configuração

### Teste de Rate Limiting

Execute 11 requisições seguidas:

```bash
for i in {1..11}; do
  curl -X POST http://localhost:8080/api/alert \
    -H "Content-Type: application/json" \
    -d '{"telegramUsername":"test","maxGasPrice":30}'
  echo ""
done
```

A 11ª deve retornar HTTP 429 (Too Many Requests).

---

## 🐛 Troubleshooting

### Problema: "Bot não responde no Telegram"

**Causas:**
- Token incorreto
- Bot não foi iniciado no código
- Firewall bloqueando Telegram API

**Solução:**
1. Verifique o token: `echo $TELEGRAM_BOT_TOKEN`
2. Confira logs: `tail -f logs/spring.log`
3. Teste conectividade: `curl https://api.telegram.org/bot<TOKEN>/getMe`

---

### Problema: "Gas price sempre retorna null"

**Causas:**
- API Key da Etherscan inválida
- Limite de requisições excedido (5 req/sec grátis)

**Solução:**
1. Verifique API Key: `echo $ETHERSCAN_API_KEY`
2. Teste API manualmente:
```bash
curl "https://api.etherscan.io/api?module=gastracker&action=gasoracle&apikey=<SUA_KEY>"
```

---

### Problema: "Erro de conexão com PostgreSQL"

**Causas:**
- URL de conexão incorreta
- Credenciais inválidas
- Database não criado

**Solução:**
1. Verifique variáveis:
```bash
echo $POSTGRES_URL
echo $POSTGRES_USER
echo $POSTGRES_PASSWORD
```

2. Teste conexão:
```bash
psql $POSTGRES_URL
```

---

### Problema: "Container Docker não inicia"

**Causas:**
- Porta 8080 já em uso
- Variáveis de ambiente não definidas
- Imagem corrompida

**Solução:**
1. Libere a porta:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

2. Reconstrua a imagem:
```bash
docker build --no-cache -t gastracker:latest .
```

---

### Problema: "Alertas não são enviados"

**Causas:**
- Cron job não está executando
- Usuário sem chatId configurado
- Gas price nunca ficou abaixo do threshold

**Solução:**
1. Verifique logs do scheduler:
```bash
grep "GasCheckScheduler" logs/spring.log
```

2. Confira database:
```sql
SELECT * FROM users WHERE is_active = true;
SELECT * FROM gas_alerts ORDER BY sent_at DESC LIMIT 10;
```

3. Force verificação manual (adicione endpoint temporário):
```bash
curl http://localhost:8080/api/admin/check-gas
```

---

## 📊 Monitoramento em Produção

### Logs do Render

1. Acesse o Dashboard do Render
2. Selecione seu Web Service
3. Clique na aba **"Logs"**
4. Monitore:
   - Erros de API (Telegram/Etherscan)
   - Execuções do cron job
   - Rate limiting

### Métricas Importantes

- **Uptime**: Deve ser 99%+
- **Response Time**: < 500ms
- **Taxa de erro**: < 1%
- **Alertas enviados/dia**: Depende do gas price

---

## 📚 Estrutura do Projeto

```
GasTracker/
├── src/
│   ├── main/
│   │   ├── java/com/gastracker/
│   │   │   ├── GasTrackerApplication.java
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   └── AlertController.java
│   │   │   ├── dto/
│   │   │   │   ├── AlertResponse.java
│   │   │   │   └── CreateAlertRequest.java
│   │   │   ├── model/
│   │   │   │   ├── GasAlert.java
│   │   │   │   └── User.java
│   │   │   ├── repository/
│   │   │   │   ├── GasAlertRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── scheduler/
│   │   │   │   └── GasCheckScheduler.java
│   │   │   └── service/
│   │   │       ├── AlertService.java
│   │   │       ├── GasService.java
│   │   │       ├── TelegramBotService.java
│   │   │       └── ValidationService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-production.properties
│   │       └── static/
│   │           ├── index.html
│   │           ├── script.js
│   │           └── styles.css
├── Dockerfile
├── docker-compose.yml (criar se necessário)
├── pom.xml
├── render.yaml
├── system.properties
└── SETUP_GUIDE.md (este arquivo)
```

---

## 🎯 Próximos Passos (Melhorias)

### Curto Prazo
- [ ] Adicionar testes unitários
- [ ] Implementar CI/CD (GitHub Actions)
- [ ] Dashboard admin para métricas

### Médio Prazo
- [ ] Suporte a múltiplas redes (Polygon, BSC)
- [ ] Notificações personalizadas (Discord, Email)
- [ ] Histórico de gas price

### Longo Prazo
- [ ] Machine Learning para prever gas price
- [ ] API pública para desenvolvedores
- [ ] Mobile app (React Native)

---

## 📞 Suporte

- **Issues**: Abra uma issue no GitHub
- **Email**: seu-email@example.com
- **Telegram**: @seu_username

---

## 📄 Licença

Este projeto é open-source para fins educacionais.

---

## ✅ Checklist de Deploy

Antes de fazer deploy em produção:

- [ ] Criar bot no Telegram via BotFather
- [ ] Obter API Key da Etherscan
- [ ] Criar conta no Render.com
- [ ] Criar database PostgreSQL no Render
- [ ] Configurar todas as variáveis de ambiente
- [ ] Fazer push do código para GitHub
- [ ] Criar Web Service no Render
- [ ] Testar health check (`/api/health`)
- [ ] Testar criação de alerta
- [ ] Configurar bot no Telegram (`/start`)
- [ ] Configurar UptimeRobot
- [ ] Monitorar logs por 24h
- [ ] Documentar URL pública

---

**🚀 GasTracker está pronto para uso!**

Acesse: `http://localhost:8080` (local) ou `https://seu-app.onrender.com` (produção)
