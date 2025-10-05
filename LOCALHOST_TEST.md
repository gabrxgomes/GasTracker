# 🧪 GasTracker - Guia de Teste em Localhost

## 📋 Requisitos

- Java 17 ou superior
- Maven 3.9+
- Telegram Bot Token
- Etherscan API Key

---

## 🚀 Passo a Passo para Testar em Localhost

### 1️⃣ Obter Credenciais

#### Telegram Bot (5 minutos)

1. Abra o Telegram e busque por `@BotFather`
2. Digite `/newbot`
3. Escolha um nome: `GasTracker Test Bot`
4. Escolha um username: `GasTrackerTestBot` (ou outro disponível)
5. **Copie o Token** (formato: `123456789:ABCdefGHIjklMNOpqrsTUVwxyz`)

**Configurar Comandos:**
1. No BotFather, digite `/setcommands`
2. Selecione seu bot
3. Cole:
```
start - Ativar alertas de gas price
status - Ver configuração atual
stop - Desativar alertas
```

#### Etherscan API Key (3 minutos)

1. Acesse: https://etherscan.io/register
2. Crie uma conta (gratuita)
3. Acesse: https://etherscan.io/myapikey
4. Clique em **"Add"**
5. **Copie a API Key**

---

### 2️⃣ Configurar Variáveis de Ambiente

#### Windows (PowerShell):

```powershell
$env:TELEGRAM_BOT_TOKEN="cole-seu-token-aqui"
$env:TELEGRAM_BOT_USERNAME="seu-bot-username"
$env:ETHERSCAN_API_KEY="cole-sua-api-key-aqui"
```

#### Windows (CMD):

```cmd
set TELEGRAM_BOT_TOKEN=cole-seu-token-aqui
set TELEGRAM_BOT_USERNAME=seu-bot-username
set ETHERSCAN_API_KEY=cole-sua-api-key-aqui
```

#### Linux/Mac (Bash):

```bash
export TELEGRAM_BOT_TOKEN="cole-seu-token-aqui"
export TELEGRAM_BOT_USERNAME="seu-bot-username"
export ETHERSCAN_API_KEY="cole-sua-api-key-aqui"
```

---

### 3️⃣ Executar a Aplicação

No diretório `D:\Estudo\Java\GasTracker`:

```bash
mvn spring-boot:run
```

**Aguarde até ver:**
```
Started GasTrackerApplication in X.XXX seconds
Telegram Bot inicializado com sucesso: seu-bot-username
```

---

### 4️⃣ Acessar a Aplicação

Abra o navegador em: **http://localhost:8080**

Você verá:
- Landing page com design dark mode
- Gas price atual em Gwei
- Formulário para criar alerta

---

### 5️⃣ Testar Criação de Alerta

#### Na Landing Page:

1. **Preço máximo de gas**: Digite `30` (Gwei)
2. **Username do Telegram**: Digite `@seu_username` (seu username real do Telegram)
3. Clique em **"Criar Alerta"**

**Mensagem esperada:**
```
✅ Alerta criado com sucesso! Use /start no bot do Telegram...
```

---

### 6️⃣ Ativar Bot no Telegram

1. Abra o Telegram
2. Busque por `@seu_bot_username` (o username que você criou)
3. Clique em **"Start"** ou digite `/start`

**Mensagem esperada:**
```
👋 Olá, @seu_username!

Seu alerta está ativo para gas price ≤ 30 Gwei.

Comandos disponíveis:
/status - Ver configuração atual
/stop - Desativar alertas
```

---

### 7️⃣ Testar Comandos do Bot

#### `/status` - Ver configuração

```
📊 Status do Alerta

Username: @seu_username
Gas Price Máximo: 30 Gwei
Status: ✅ Ativo
Última notificação: Nunca
```

#### `/stop` - Desativar alertas

```
✅ Alertas desativados. Use /start para reativar.
```

---

### 8️⃣ Testar APIs Manualmente

#### API 1: Consultar Gas Price Atual

```bash
curl http://localhost:8080/api/gas-price
```

**Resposta:**
```json
{
  "gasPrice": 25,
  "unit": "Gwei",
  "timestamp": 1696441234567
}
```

#### API 2: Consultar Estatísticas

```bash
curl http://localhost:8080/api/stats
```

**Resposta:**
```json
{
  "activeUsers": 1,
  "totalAlerts24h": 0,
  "successfulAlerts24h": 0
}
```

#### API 3: Health Check

```bash
curl http://localhost:8080/api/health
```

**Resposta:**
```json
{
  "status": "UP"
}
```

#### API 4: Criar Alerta via cURL

```bash
curl -X POST http://localhost:8080/api/alert \
  -H "Content-Type: application/json" \
  -d "{\"telegramUsername\":\"seu_username\",\"maxGasPrice\":30}"
```

---

### 9️⃣ Verificar Cron Job (Automático)

O sistema verifica o gas price **a cada 5 minutos** automaticamente.

**Para ver logs do cron job:**

Observe o console onde você executou `mvn spring-boot:run`:

```
Iniciando verificação de gas price...
Gas price atual: 25 Gwei
Encontrados 1 usuários elegíveis para alerta
Verificação de gas price concluída
```

**Quando receber alerta:**

Se o gas price estiver abaixo do seu threshold (ex: 25 < 30), você receberá no Telegram:

```
⛽ ALERTA DE GAS PRICE!

Gas atual: 25 Gwei
Seu limite: 30 Gwei

💡 Agora é um bom momento para fazer transações!
```

---

### 🔟 Acessar Database H2 (Opcional)

Para ver os dados salvos:

1. Acesse: **http://localhost:8080/h2-console**
2. Configure:
   - **JDBC URL**: `jdbc:h2:mem:gastracker`
   - **User Name**: `sa`
   - **Password**: *(deixe em branco)*
3. Clique em **"Connect"**

**Queries úteis:**

```sql
-- Ver usuários
SELECT * FROM users;

-- Ver alertas enviados
SELECT * FROM gas_alerts ORDER BY sent_at DESC;

-- Contar usuários ativos
SELECT COUNT(*) FROM users WHERE is_active = true;
```

---

## 🧪 Testes de Segurança

### Teste 1: Rate Limiting

Execute 11 requisições seguidas:

```bash
# Windows PowerShell
for ($i=1; $i -le 11; $i++) {
    curl -X POST http://localhost:8080/api/alert `
      -H "Content-Type: application/json" `
      -d "{\"telegramUsername\":\"test\",\"maxGasPrice\":30}"
    Write-Host "Request $i"
}
```

```bash
# Linux/Mac
for i in {1..11}; do
  curl -X POST http://localhost:8080/api/alert \
    -H "Content-Type: application/json" \
    -d '{"telegramUsername":"test","maxGasPrice":30}'
  echo "Request $i"
done
```

**Resultado esperado:** A 11ª requisição deve retornar:
```json
{
  "error": "Muitas requisições. Tente novamente mais tarde."
}
```

### Teste 2: Validação de Input (XSS)

```bash
curl -X POST http://localhost:8080/api/alert \
  -H "Content-Type: application/json" \
  -d "{\"telegramUsername\":\"<script>alert('xss')</script>\",\"maxGasPrice\":30}"
```

**Resultado esperado:**
```json
{
  "error": "Username inválido. Deve ter 5-32 caracteres..."
}
```

### Teste 3: Validação de Gas Price

```bash
# Gas price inválido (0)
curl -X POST http://localhost:8080/api/alert \
  -H "Content-Type: application/json" \
  -d "{\"telegramUsername\":\"test\",\"maxGasPrice\":0}"
```

**Resultado esperado:**
```json
{
  "error": "Gas price deve estar entre 1 e 1000 Gwei"
}
```

---

## 🛑 Parar a Aplicação

No terminal onde está rodando `mvn spring-boot:run`:

- **Windows**: `Ctrl + C`
- **Linux/Mac**: `Ctrl + C`

---

## 🐛 Troubleshooting

### Problema: "Port 8080 already in use"

**Solução:**

```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

### Problema: "Telegram Bot não responde"

**Verificações:**

1. Token está correto?
```bash
echo $TELEGRAM_BOT_TOKEN
```

2. Teste a API diretamente:
```bash
curl https://api.telegram.org/bot<SEU_TOKEN>/getMe
```

3. Verifique logs no console

### Problema: "Gas price sempre retorna null"

**Verificações:**

1. API Key está correta?
```bash
echo $ETHERSCAN_API_KEY
```

2. Teste a API diretamente:
```bash
curl "https://api.etherscan.io/api?module=gastracker&action=gasoracle&apikey=<SUA_KEY>"
```

3. Limite de requisições da Etherscan:
   - Free tier: 5 req/segundo
   - Aguarde 1 minuto e tente novamente

### Problema: "mvn command not found"

**Solução:**

1. Instale o Maven: https://maven.apache.org/download.cgi
2. Ou use o wrapper (se disponível):
```bash
./mvnw spring-boot:run  # Linux/Mac
mvnw.cmd spring-boot:run  # Windows
```

---

## 📊 Monitoramento Durante Testes

### Logs Importantes

Observe no console:

```
✅ Sucesso:
- "Started GasTrackerApplication"
- "Telegram Bot inicializado com sucesso"
- "Iniciando verificação de gas price..."
- "Alerta criado/atualizado para usuário"
- "Alerta enviado com sucesso"

❌ Erros:
- "Erro ao inicializar Telegram Bot"
- "Erro ao consultar Etherscan API"
- "Validação falhou"
- "Erro ao enviar mensagem Telegram"
```

### Estatísticas em Tempo Real

Acesse a landing page e veja:

- **Usuários Ativos**: Quantos usuários têm alertas ativos
- **Alertas 24h**: Total de alertas enviados nas últimas 24h
- **Taxa de Sucesso**: Percentual de alertas enviados com sucesso

---

## ✅ Checklist de Teste Completo

- [ ] Aplicação inicia sem erros
- [ ] Landing page carrega corretamente
- [ ] Gas price atual é exibido
- [ ] Criar alerta retorna sucesso
- [ ] Bot responde ao `/start`
- [ ] Bot responde ao `/status`
- [ ] Bot responde ao `/stop`
- [ ] APIs REST funcionam (gas-price, stats, health)
- [ ] Rate limiting bloqueia após 10 requisições
- [ ] XSS é bloqueado (script tags)
- [ ] Validação de gas price funciona
- [ ] H2 Console acessível
- [ ] Logs não mostram erros

---

## 🎯 Próximo Passo: Deploy no Render

Após testar localmente com sucesso:

1. Crie repositório no GitHub
2. Faça push do código
3. Siga o **SETUP_GUIDE.md** na seção "Deploy no Render"

---

## 📝 Resumo de Comandos

```bash
# 1. Configurar variáveis (Windows PowerShell)
$env:TELEGRAM_BOT_TOKEN="seu-token"
$env:TELEGRAM_BOT_USERNAME="seu-bot-username"
$env:ETHERSCAN_API_KEY="sua-api-key"

# 2. Executar
mvn spring-boot:run

# 3. Acessar
# http://localhost:8080

# 4. Testar API
curl http://localhost:8080/api/health

# 5. Parar
# Ctrl + C
```

---

**✨ Pronto! Agora você pode testar o GasTracker localmente.**

Qualquer dúvida, consulte o **SETUP_GUIDE.md** completo.
