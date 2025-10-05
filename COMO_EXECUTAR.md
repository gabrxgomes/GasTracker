# 🚀 Como Executar o GasTracker

## ✅ Projeto Corrigido para Java 21

O projeto foi **completamente revisado** para funcionar com Java 21 + Spring Boot 3.3.0.

---

## 📋 Mudanças Aplicadas

### 1. **pom.xml**
- ✅ Spring Boot atualizado: `3.2.0` → `3.3.0`
- ✅ Java version: `17` → `21`
- ✅ Telegram Bot API atualizado: `6.8.0` → `7.2.1` (nova arquitetura)
- ✅ Bucket4j atualizado: `8.7.0` → `8.10.1`
- ✅ OWASP Sanitizer atualizado: `20220608.1` → `20240325.1`
- ✅ Removidas dependências JAXB problemáticas

### 2. **TelegramBotService.java**
- ✅ Atualizado para usar nova API do Telegram 7.x
- ✅ Usa `OkHttpTelegramClient` (moderno)
- ✅ Implementa `LongPollingSingleThreadUpdateConsumer`

### 3. **system.properties**
- ✅ Java runtime: `17` → `21`

---

## 🔧 Como Executar

### **Passo 1: Limpar cache do VS Code**

No VS Code, pressione `Ctrl+Shift+P` e digite:
```
Java: Clean Java Language Server Workspace
```

Clique em **"Restart and delete"**

### **Passo 2: Aguardar Maven baixar dependências**

Observe o canto inferior direito do VS Code. Aguarde até aparecer:
```
✓ Maven dependencies downloaded
```

### **Passo 3: Executar a aplicação**

- Pressione **F5**
- OU clique em **Run** > **Run Without Debugging**
- OU clique no botão ▶️ no canto superior direito

### **Passo 4: Verificar se iniciou corretamente**

Você deve ver no terminal:
```
Started GasTrackerApplication in X.XXX seconds
Tomcat initialized with port 8080 (http)
```

---

## 🌐 Acessar a Aplicação

Após iniciar, abra o navegador em:

```
http://localhost:8080
```

---

## ⚙️ Configurar Variáveis de Ambiente (Opcional)

Para testar com Telegram e Etherscan reais, configure:

**Windows (PowerShell):**
```powershell
$env:TELEGRAM_BOT_TOKEN="seu-token-aqui"
$env:TELEGRAM_BOT_USERNAME="seu-bot-username"
$env:ETHERSCAN_API_KEY="sua-api-key"
```

**Windows (CMD):**
```cmd
set TELEGRAM_BOT_TOKEN=seu-token-aqui
set TELEGRAM_BOT_USERNAME=seu-bot-username
set ETHERSCAN_API_KEY=sua-api-key
```

---

## 🐛 Solução de Problemas

### Erro: "NoClassDefFoundError: javax/xml/bind/annotation/XmlElement"
✅ **CORRIGIDO** - Atualizamos para Spring Boot 3.3.0 que não tem esse bug

### Erro: "class file has wrong version"
✅ **CORRIGIDO** - Configuramos tudo para Java 21

### Erro: Telegram API não inicializa
✅ **CORRIGIDO** - Atualizamos para Telegram Bot API 7.2.1

---

## 📊 Versões Finais

| Componente | Versão |
|------------|--------|
| Java | 21 |
| Spring Boot | 3.3.0 |
| Hibernate | 6.5.2 (do Spring Boot 3.3.0) |
| Telegram Bot API | 7.2.1 |
| H2 Database | 2.2.224 |
| PostgreSQL Driver | 42.7.3 |

---

## ✅ Checklist de Execução

- [ ] VS Code aberto no diretório do projeto
- [ ] Java 21 instalado e configurado
- [ ] Maven instalado (ou usar wrapper do VS Code)
- [ ] Executei "Clean Java Language Server Workspace"
- [ ] Aguardei download das dependências
- [ ] Executei a aplicação (F5)
- [ ] Acessei http://localhost:8080

---

**🎉 O projeto agora está 100% compatível com Java 21!**

Se ainda houver erros, copie a mensagem completa do erro e me envie.
