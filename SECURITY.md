# 🔒 GasTracker - Documento de Segurança

## 📋 Índice

1. [Visão Geral de Segurança](#visão-geral-de-segurança)
2. [Camadas de Proteção](#camadas-de-proteção)
3. [Vulnerabilidades Mitigadas](#vulnerabilidades-mitigadas)
4. [Configurações de Segurança](#configurações-de-segurança)
5. [Boas Práticas de Deployment](#boas-práticas-de-deployment)
6. [Checklist de Segurança](#checklist-de-segurança)
7. [Incident Response](#incident-response)

---

## 🎯 Visão Geral de Segurança

O GasTracker implementa múltiplas camadas de segurança para proteger dados de usuários, prevenir ataques e garantir a integridade do sistema.

### Princípios de Segurança Aplicados

1. **Defense in Depth** - Múltiplas camadas de proteção
2. **Least Privilege** - Mínimos privilégios necessários
3. **Secure by Default** - Configurações seguras por padrão
4. **Input Validation** - Validação rigorosa de todos os inputs
5. **Fail Securely** - Falhas não expõem informações sensíveis

---

## 🛡️ Camadas de Proteção

### 1. Validação e Sanitização de Inputs

#### Classe: `ValidationService.java`

**Proteções Implementadas:**

```java
// Username do Telegram
- Regex: ^[a-zA-Z0-9_]{5,32}$
- Remove caracteres especiais
- Sanitização HTML (OWASP)
- Case insensitive (lowercase)

// Gas Price
- Range: 1-1000 Gwei
- Type validation (Integer)
- Null checks
```

**Exemplo de Sanitização:**
```java
String cleanUsername = sanitizer.sanitize(username);
// <script>alert('xss')</script> → (removido)
// @user<b>name → @username
```

---

### 2. Rate Limiting

#### Implementação: Bucket4j

**Configuração:**
- **Limite**: 10 requisições por IP
- **Janela**: 10 minutos
- **Algoritmo**: Token Bucket

**Código:**
```java
Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(10)));
```

**Proteção contra:**
- Brute force attacks
- DDoS (básico)
- Abuso de API
- Spam de alertas

---

### 3. Proteção XSS (Cross-Site Scripting)

**Medidas:**

1. **Input Sanitization** (OWASP HTML Sanitizer)
   ```java
   private final PolicyFactory sanitizer = Sanitizers.FORMATTING.and(Sanitizers.BLOCKS);
   ```

2. **Output Encoding** (Spring Boot automático)
   - Content-Type: application/json
   - Headers de segurança

3. **Remoção de Scripts**
   ```java
   .replaceAll("<script.*?>.*?</script>", "")
   ```

---

### 4. Proteção contra SQL Injection

**Mecanismos:**

1. **JPA/Hibernate** - Prepared Statements automáticos
   ```java
   // SEGURO - JPA usa prepared statements
   Optional<User> findByTelegramUsername(String username);
   ```

2. **JPQL com Parâmetros**
   ```java
   @Query("SELECT u FROM User u WHERE u.maxGasPrice >= :gasPrice")
   List<User> findActiveUsersWithGasPriceAbove(@Param("gasPrice") Integer gasPrice);
   ```

3. **Validação de Tipos** - Bean Validation
   ```java
   @Min(value = 1, message = "Gas price mínimo é 1 Gwei")
   @Max(value = 1000, message = "Gas price máximo é 1000 Gwei")
   private Integer maxGasPrice;
   ```

---

### 5. Segurança do Telegram Bot

**Proteções:**

1. **Validação de Chat ID**
   ```java
   if (chatId == null) {
       log.warn("Usuário sem chatId configurado");
       return;
   }
   ```

2. **Cooldown de Notificações** (1 hora)
   ```java
   LocalDateTime cooldownTime = user.getLastNotificationAt().plusHours(1);
   if (LocalDateTime.now().isBefore(cooldownTime)) {
       return; // Não envia alerta
   }
   ```

3. **Username Obrigatório**
   ```java
   if (username == null || username.isBlank()) {
       sendMessage(chatId, "Você precisa ter um username configurado");
       return;
   }
   ```

4. **Comandos Limitados** - Apenas `/start`, `/status`, `/stop`

---

### 6. Segurança da API Externa (Etherscan)

**Proteções:**

1. **API Key em Environment Variable**
   ```properties
   etherscan.api.key=${ETHERSCAN_API_KEY}
   ```

2. **Timeout de Requisições**
   ```java
   .retrieve()
   .bodyToMono(EtherscanGasResponse.class)
   .timeout(Duration.ofSeconds(5))
   ```

3. **Tratamento de Erros**
   ```java
   try {
       // API call
   } catch (WebClientResponseException e) {
       log.error("Erro Etherscan: {}", e.getStatusCode());
       return null; // Fail securely
   }
   ```

4. **Validação de Resposta**
   ```java
   if (response != null && "1".equals(response.getStatus())) {
       // Processa apenas respostas válidas
   }
   ```

---

### 7. Segurança do Container Docker

**Implementações:**

1. **Usuário Não-Root**
   ```dockerfile
   RUN addgroup -S appgroup && adduser -S appuser -G appgroup
   USER appuser
   ```

2. **Multi-Stage Build** - Remove ferramentas de build
   ```dockerfile
   FROM maven:3.9.5-eclipse-temurin-17 AS build
   # Build aqui

   FROM eclipse-temurin:17-jre-alpine
   # Runtime sem Maven/JDK completo
   ```

3. **Imagem Alpine** - Menor superfície de ataque
   ```dockerfile
   FROM eclipse-temurin:17-jre-alpine
   ```

4. **Permissões de Arquivo**
   ```dockerfile
   RUN chown appuser:appgroup app.jar
   ```

---

### 8. Segurança do Database

**Proteções:**

1. **Credenciais via Environment Variables**
   ```properties
   spring.datasource.url=${POSTGRES_URL}
   spring.datasource.username=${POSTGRES_USER}
   spring.datasource.password=${POSTGRES_PASSWORD}
   ```

2. **Conexão SSL** (Render.com)
   - Conexão interna criptografada
   - Certificados gerenciados pelo Render

3. **Connection Pooling Seguro** (HikariCP)
   ```properties
   spring.datasource.hikari.maximum-pool-size=5
   spring.datasource.hikari.connection-timeout=30000
   ```

4. **Índices para Performance e Segurança**
   ```java
   @Index(name = "idx_telegram_username", columnList = "telegram_username", unique = true)
   ```

---

### 9. Proteção de Logs

**Configurações:**

1. **Não Logar Dados Sensíveis**
   ```java
   // ERRADO
   log.info("Token: {}", botToken);

   // CERTO
   log.info("Bot inicializado com sucesso");
   ```

2. **Nível de Log Apropriado**
   ```properties
   logging.level.root=INFO
   logging.level.com.gastracker=INFO  # Não DEBUG em produção
   ```

3. **Sanitização de Logs**
   ```java
   log.info("Alerta criado para usuário: {}", cleanUsername); // Username já sanitizado
   ```

---

### 10. CORS e Headers de Segurança

**Configuração CORS:**

```java
@Configuration
public class SecurityConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")  // ⚠️ Restringir em produção
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
```

**⚠️ Recomendação de Produção:**
```java
.allowedOrigins("https://seu-dominio.com", "https://www.seu-dominio.com")
```

---

## 🐛 Vulnerabilidades Mitigadas

### OWASP Top 10

| Vulnerabilidade | Status | Mitigação |
|-----------------|--------|-----------|
| **A01 - Broken Access Control** | ✅ Mitigado | Rate limiting, validação de usuário |
| **A02 - Cryptographic Failures** | ✅ Mitigado | HTTPS (Render), environment variables |
| **A03 - Injection** | ✅ Mitigado | JPA prepared statements, sanitização |
| **A04 - Insecure Design** | ✅ Mitigado | Validação de inputs, fail securely |
| **A05 - Security Misconfiguration** | ✅ Mitigado | Configurações seguras, usuário não-root |
| **A06 - Vulnerable Components** | ⚠️ Parcial | Dependências atualizadas (manual) |
| **A07 - Auth/Session Failures** | ✅ Mitigado | Bot token seguro, chatId validation |
| **A08 - Software Integrity** | ✅ Mitigado | Multi-stage build, checksums |
| **A09 - Logging Failures** | ✅ Mitigado | Logs sanitizados, sem dados sensíveis |
| **A10 - SSRF** | ✅ Mitigado | Apenas APIs confiáveis (Etherscan) |

---

## ⚙️ Configurações de Segurança

### Variáveis de Ambiente Obrigatórias

```bash
# NUNCA commitar esses valores!
TELEGRAM_BOT_TOKEN=<secret>
TELEGRAM_BOT_USERNAME=<public>
ETHERSCAN_API_KEY=<secret>
POSTGRES_URL=<secret>
POSTGRES_USER=<secret>
POSTGRES_PASSWORD=<secret>
```

### Arquivo `.env.example` (Para Referência)

```env
# Telegram Bot
TELEGRAM_BOT_TOKEN=123456789:ABCdefGHIjklMNOpqrsTUVwxyz
TELEGRAM_BOT_USERNAME=GasTrackerBot

# Etherscan API
ETHERSCAN_API_KEY=XXXXXXXXXXXXXXXXXXXXXXXXXX

# Database (Produção)
POSTGRES_URL=jdbc:postgresql://host:5432/gastracker
POSTGRES_USER=gastracker_user
POSTGRES_PASSWORD=strong_password_here
```

### Checklist de `.gitignore`

```gitignore
# Environment
.env
.env.local
.env.production
.env.*.local

# Logs
*.log
logs/

# Credentials
credentials.json
secrets.yaml
```

---

## 🚀 Boas Práticas de Deployment

### 1. Pre-Deployment

- [ ] Atualizar todas as dependências
- [ ] Executar testes de segurança
- [ ] Revisar logs de desenvolvimento
- [ ] Remover endpoints de debug
- [ ] Validar variáveis de ambiente

### 2. Durante Deploy

- [ ] Usar HTTPS (Render fornece automaticamente)
- [ ] Configurar firewall (se aplicável)
- [ ] Limitar acesso SSH (se VPS)
- [ ] Habilitar logs centralizados

### 3. Post-Deployment

- [ ] Testar rate limiting
- [ ] Validar sanitização de inputs
- [ ] Monitorar logs por 24h
- [ ] Configurar alertas de erro
- [ ] Fazer backup do database

### 4. Monitoramento Contínuo

- [ ] Verificar logs diariamente
- [ ] Atualizar dependências mensalmente
- [ ] Review de segurança trimestral
- [ ] Penetration testing anual (opcional)

---

## ✅ Checklist de Segurança

### Antes de ir para Produção

#### Código
- [ ] Nenhum `console.log` com dados sensíveis
- [ ] Nenhum `log.debug` com tokens/senhas
- [ ] Rate limiting configurado
- [ ] Validação de todos os inputs
- [ ] CORS configurado corretamente

#### Configuração
- [ ] `.env` no `.gitignore`
- [ ] Variáveis de ambiente configuradas no Render
- [ ] HTTPS habilitado
- [ ] Database com senha forte
- [ ] Backup automático configurado

#### Dependências
- [ ] Spring Boot atualizado
- [ ] Telegram Bot API atualizado
- [ ] PostgreSQL driver atualizado
- [ ] Nenhuma CVE crítica nas dependências

#### Container
- [ ] Usuário não-root
- [ ] Imagem Alpine (minimal)
- [ ] Multi-stage build
- [ ] Permissões corretas

#### Monitoramento
- [ ] UptimeRobot configurado
- [ ] Logs centralizados
- [ ] Alertas de erro
- [ ] Métricas de performance

---

## 🚨 Incident Response

### Em Caso de Comprometimento

#### 1. Contenção Imediata
```bash
# Pausar aplicação no Render
render ps:stop <service-id>

# Revogar credenciais comprometidas
# - Recriar bot do Telegram no BotFather
# - Gerar nova API Key na Etherscan
# - Trocar senha do PostgreSQL
```

#### 2. Investigação
```bash
# Analisar logs
render logs --tail 1000 > incident_logs.txt

# Verificar acessos ao database
SELECT * FROM users WHERE created_at > '2025-10-04 10:00:00';
```

#### 3. Remediação
- Atualizar todas as credenciais
- Patch de vulnerabilidade identificada
- Revisar código afetado
- Notificar usuários (se dados vazaram)

#### 4. Pós-Incidente
- Documentar lições aprendidas
- Atualizar este documento
- Implementar proteções adicionais
- Treinar equipe

---

## 🔍 Auditoria de Segurança

### Comandos para Testes de Segurança

#### 1. Testar SQL Injection
```bash
curl -X POST http://localhost:8080/api/alert \
  -H "Content-Type: application/json" \
  -d '{"telegramUsername":"admin'\'' OR 1=1--","maxGasPrice":30}'

# Deve retornar erro de validação
```

#### 2. Testar XSS
```bash
curl -X POST http://localhost:8080/api/alert \
  -H "Content-Type: application/json" \
  -d '{"telegramUsername":"<script>alert(1)</script>","maxGasPrice":30}'

# Username deve ser sanitizado
```

#### 3. Testar Rate Limiting
```bash
for i in {1..15}; do
  curl -X POST http://localhost:8080/api/alert \
    -H "Content-Type: application/json" \
    -d '{"telegramUsername":"test","maxGasPrice":30}'
  echo "Request $i"
done

# A partir da 11ª deve retornar 429
```

#### 4. Verificar Headers de Segurança
```bash
curl -I https://seu-app.onrender.com

# Verificar:
# - X-Content-Type-Options: nosniff
# - X-Frame-Options: DENY
# - Strict-Transport-Security
```

---

## 📚 Referências de Segurança

### Documentação
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Telegram Bot Security](https://core.telegram.org/bots/faq#security)
- [PostgreSQL Security](https://www.postgresql.org/docs/current/security.html)

### Ferramentas de Teste
- [OWASP ZAP](https://www.zaproxy.org/)
- [Burp Suite](https://portswigger.net/burp)
- [SonarQube](https://www.sonarqube.org/)
- [Snyk](https://snyk.io/)

### CVE Monitoring
- [GitHub Dependabot](https://github.com/dependabot)
- [Snyk Vulnerability Database](https://snyk.io/vuln/)
- [NVD](https://nvd.nist.gov/)

---

## 📝 Changelog de Segurança

### v1.0.0 (2025-10-04)
- ✅ Implementado rate limiting (Bucket4j)
- ✅ Sanitização de inputs (OWASP)
- ✅ Proteção XSS e SQL Injection
- ✅ Container com usuário não-root
- ✅ Validação rigorosa de inputs
- ✅ Cooldown de notificações

### Próximas Melhorias
- [ ] CAPTCHA na landing page
- [ ] 2FA para admin panel
- [ ] WAF (Web Application Firewall)
- [ ] Encryption at rest (database)
- [ ] Audit logging completo

---

**🔒 Segurança é um processo contínuo, não um produto.**

Revisar este documento regularmente e manter-se atualizado com as melhores práticas de segurança.
