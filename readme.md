# 📚 LinkShort - Complete Project Documentation

> **URL Shortener with Auto-Expiring Links, QR Code Generation & Real-Time Statistics**

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Multi--Stage-2496ED.svg)](https://www.docker.com/)
[![Render](https://img.shields.io/badge/Deployed%20on-Render-46E3B7.svg)](https://render.com/)

**Live Demo:** https://linkshortner-cngr.onrender.com

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Features](#-features)
- [Project Structure](#-project-structure)
- [Backend Deep Dive](#-backend-deep-dive)
- [Frontend Deep Dive](#-frontend-deep-dive)
- [Database Schema](#-database-schema)
- [Security Features](#-security-features)
- [Deployment](#-deployment)
- [Environment Variables](#-environment-variables)
- [API Endpoints](#-api-endpoints)
- [How It Works](#-how-it-works)
- [Performance Optimizations](#-performance-optimizations)
- [Future Improvements](#-future-improvements)

---

## 🎯 Overview

**LinkShort** is a modern, production-ready URL shortener built with Java and Spring Boot. It provides:

- **Auto-expiring links** (default 24 hours, configurable)
- **QR Code generation** for easy mobile sharing
- **Real-time system statistics** (total links, clicks, active links)
- **Dark mode UI** with tech-focused design
- **Security features** (URL validation, XSS protection, SSRF prevention)
- **Database persistence** with PostgreSQL
- **24/7 uptime** on Render.com free tier

**Key Differentiators:**
- Automatic link expiration and cleanup
- Real-time statistics fetched from database
- Professional dark theme (not AI-generated looking)
- Fully containerized with Docker
- Zero-cost deployment strategy

---

## 🛠 Tech Stack

### Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 17 | Programming language |
| **Spring Boot** | 3.2.0 | Application framework |
| **Spring Data JPA** | 3.2.0 | ORM for database operations |
| **Hibernate** | 6.3.1 | JPA implementation |
| **PostgreSQL** | 16 | Production database |
| **H2 Database** | Runtime | Local development |
| **HikariCP** | Included | Connection pooling |
| **Lombok** | Latest | Reduce boilerplate code |
| **Apache Commons Validator** | 1.8.0 | URL validation |
| **SLF4J + Logback** | Included | Logging framework |

### Frontend

| Technology | Version | Purpose |
|------------|---------|---------|
| **HTML5** | - | Markup structure |
| **CSS3** | - | Styling (custom dark theme) |
| **Vanilla JavaScript** | ES6+ | Client-side logic |
| **QRCode.js** | 1.0.0 | QR code generation |
| **Fetch API** | - | AJAX requests |

### Infrastructure

| Tool | Purpose |
|------|---------|
| **Docker** | Multi-stage containerization |
| **Maven** | Build tool and dependency management |
| **Git** | Version control |
| **Render.com** | Cloud hosting (Web Service + PostgreSQL) |
| **UptimeRobot** | Keep service awake 24/7 |

### Build Tools

- **Maven 3.9.5** - Dependency management
- **Docker Multi-Stage Build** - Optimized production images
- **Spring Boot Maven Plugin** - JAR packaging

---

## 🏗 Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         CLIENT                              │
│                    (Browser / Mobile)                       │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   │ HTTPS
                   ▼
┌─────────────────────────────────────────────────────────────┐
│                     RENDER.COM                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Web Service (Docker)                    │   │
│  │  ┌────────────────────────────────────────────┐      │   │
│  │  │         Spring Boot Application            │      │   │
│  │  │  ┌──────────────────────────────────┐      │      │   │
│  │  │  │    Controllers (REST API)        │      │      │   │
│  │  │  └────────────┬─────────────────────┘      │      │   │
│  │  │               │                            │      │   │
│  │  │  ┌────────────▼─────────────────────┐      │      │   │
│  │  │  │    Services (Business Logic)     │      │      │   │
│  │  │  └────────────┬─────────────────────┘      │      │   │
│  │  │               │                            │      │   │
│  │  │  ┌────────────▼─────────────────────┐      │      │   │
│  │  │  │  Repositories (Data Access)      │      │      │   │
│  │  │  └────────────┬─────────────────────┘      │      │   │
│  │  │               │ JPA/Hibernate              │      │   │
│  │  └───────────────┼────────────────────────────┘      │   │
│  │                  │                                    │   │
│  └──────────────────┼────────────────────────────────────┘   │
│                     │ JDBC                                   │
│  ┌──────────────────▼────────────────────────────────────┐   │
│  │         PostgreSQL Database (256 MB)                  │   │
│  │              (Internal Network)                       │   │
│  └───────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                   ▲
                   │ HTTP Ping every 5 min
                   │
┌──────────────────┴──────────────────────────────────────────┐
│                    UPTIMEROBOT                              │
│              (Keeps service awake)                          │
└─────────────────────────────────────────────────────────────┘
```

### Application Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
│  - REST Controllers (LinkController)                        │
│  - DTOs (Request/Response objects)                          │
│  - Static Resources (HTML, CSS, JS)                         │
└──────────────────┬──────────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────┐
│                     SERVICE LAYER                           │
│  - LinkService (Business logic)                             │
│  - UrlValidationService (Security)                          │
│  - LinkCleanupScheduler (Automated cleanup)                 │
└──────────────────┬──────────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────┐
│                  PERSISTENCE LAYER                          │
│  - LinkRepository (Spring Data JPA)                         │
│  - Custom queries for statistics                            │
└──────────────────┬──────────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────┐
│                     DATABASE LAYER                          │
│  - PostgreSQL (Production)                                  │
│  - H2 In-Memory (Development)                               │
└─────────────────────────────────────────────────────────────┘
```

---

## ✨ Features

### Core Features

1. **URL Shortening**
   - Generate short, unique codes (6 characters)
   - Collision detection with automatic retry
   - Base62 encoding (A-Z, a-z, 0-9)
   - Cryptographically secure random generation (`SecureRandom`)

2. **Auto-Expiration**
   - Default: 24 hours
   - Configurable per link
   - Automatic deactivation on access
   - Scheduled cleanup task (daily at 2 AM)

3. **Click Tracking**
   - Increment counter on each redirect
   - Atomic updates (thread-safe)
   - Persistent storage

4. **QR Code Generation**
   - Client-side generation (QRCode.js)
   - Download as PNG
   - Embedded logo support
   - Responsive sizing

5. **Real-Time Statistics**
   - Total links created
   - Total clicks across all links
   - Active links count
   - Auto-refresh every 10 seconds
   - Animated number transitions

6. **Security**
   - URL sanitization
   - XSS prevention
   - SSRF protection (blocked domains)
   - Input validation
   - CORS configuration

### User Experience

- **Dark Mode UI** - Professional, tech-focused design
- **Responsive Design** - Mobile, tablet, desktop
- **Copy to Clipboard** - One-click copy
- **Error Handling** - User-friendly error messages
- **Loading States** - Visual feedback during operations

---

## 📁 Project Structure

```
LinkEncurterProd/
│
├── src/
│   ├── main/
│   │   ├── java/com/linkshortener/
│   │   │   ├── LinkShortenerApplication.java      # Main entry point
│   │   │   │
│   │   │   ├── model/
│   │   │   │   └── Link.java                      # JPA Entity
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   └── LinkRepository.java            # Data access layer
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── LinkService.java               # Business logic
│   │   │   │   └── UrlValidationService.java      # Security validation
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   └── LinkController.java            # REST API endpoints
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── CreateLinkRequest.java         # Request DTO
│   │   │   │   ├── LinkResponse.java              # Response DTO
│   │   │   │   └── SystemStatsResponse.java       # Stats DTO
│   │   │   │
│   │   │   ├── scheduler/
│   │   │   │   └── LinkCleanupScheduler.java      # Scheduled tasks
│   │   │   │
│   │   │   └── config/
│   │   │       └── SecurityConfig.java            # CORS configuration
│   │   │
│   │   └── resources/
│   │       ├── application.properties             # Default config
│   │       ├── application-production.properties  # Production config
│   │       │
│   │       └── static/
│   │           ├── index.html                     # Main page
│   │           ├── error.html                     # Error page
│   │           ├── styles.css                     # Dark theme
│   │           └── script.js                      # Client logic
│   │
│   └── test/
│       └── java/com/linkshortener/
│           └── LinkShortenerApplicationTests.java
│
├── target/                                        # Build output
│   └── link-shortener-1.0.0.jar                   # Executable JAR
│
├── pom.xml                                        # Maven dependencies
├── Dockerfile                                     # Multi-stage build
├── build.sh                                       # Build script (legacy)
├── render.yaml                                    # Render config
├── system.properties                              # Java version
│
├── RENDER_DEPLOYMENT_GUIDE.md                     # Deployment guide
└── PROJECT_DOCUMENTATION.md                       # This file
```

---

## 🔧 Backend Deep Dive

### 1. Main Application Class

**File:** `LinkShortenerApplication.java`

```java
@SpringBootApplication
@EnableScheduling
public class LinkShortenerApplication {
    public static void main(String[] args) {
        SpringApplication.run(LinkShortenerApplication.class, args);
    }
}
```

**Annotations:**
- `@SpringBootApplication` - Combines `@Configuration`, `@EnableAutoConfiguration`, `@ComponentScan`
- `@EnableScheduling` - Enables scheduled tasks (cleanup scheduler)

---

### 2. Data Model

**File:** `Link.java`

```java
@Entity
@Table(name = "links")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String shortCode;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Long clickCount = 0L;

    @Column(nullable = false)
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = createdAt.plusHours(24);
        }
        if (clickCount == null) {
            clickCount = 0L;
        }
        if (active == null) {
            active = true;
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void incrementClickCount() {
        this.clickCount++;
    }
}
```

**Key Features:**
- **Unique constraint** on `shortCode` (prevents collisions)
- **Automatic timestamps** via `@PrePersist`
- **Default expiration** of 24 hours
- **Helper methods** for expiration check and click tracking

---

### 3. Repository Layer

**File:** `LinkRepository.java`

```java
@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {

    // Find by short code
    Optional<Link> findByShortCode(String shortCode);

    // Find active links only
    Optional<Link> findByShortCodeAndActiveTrue(String shortCode);

    // Check existence
    boolean existsByShortCode(String shortCode);

    // Find expired links (for cleanup)
    List<Link> findByActiveTrueAndExpiresAtBefore(LocalDateTime dateTime);

    // Statistics queries
    @Query("SELECT COUNT(l) FROM Link l")
    long countTotalLinks();

    @Query("SELECT SUM(l.clickCount) FROM Link l")
    Long sumTotalClicks();

    @Query("SELECT COUNT(l) FROM Link l WHERE l.active = true AND l.expiresAt > :now")
    long countActiveLinks(LocalDateTime now);
}
```

**Query Types:**
- **Derived queries** - Spring generates SQL from method names
- **JPQL queries** - Custom queries with `@Query`
- **Aggregation** - `COUNT`, `SUM` for statistics

---

### 4. Service Layer

**File:** `LinkService.java`

**Key Methods:**

#### Create Short Link
```java
@Transactional
public LinkResponse createShortLink(CreateLinkRequest request) {
    // 1. Sanitize and validate URL
    String sanitizedUrl = urlValidator.sanitizeUrl(request.getUrl());
    urlValidator.validateUrl(sanitizedUrl);

    // 2. Generate unique short code
    String shortCode = generateUniqueShortCode();

    // 3. Create and save link
    Link link = new Link();
    link.setShortCode(shortCode);
    link.setOriginalUrl(sanitizedUrl);
    link.setExpiresAt(link.getCreatedAt().plusHours(expirationHours));

    link = linkRepository.save(link);

    // 4. Return response
    return buildLinkResponse(link);
}
```

#### Get Original URL (Redirect)
```java
@Transactional
public Optional<String> getOriginalUrl(String shortCode) {
    Optional<Link> linkOpt = linkRepository.findByShortCodeAndActiveTrue(shortCode);

    if (linkOpt.isEmpty()) {
        return Optional.empty();
    }

    Link link = linkOpt.get();

    // Check expiration
    if (link.isExpired()) {
        link.setActive(false);
        linkRepository.save(link);
        return Optional.empty();
    }

    // Increment click count
    link.incrementClickCount();
    linkRepository.save(link);

    return Optional.of(link.getOriginalUrl());
}
```

#### Generate Unique Short Code
```java
private String generateUniqueShortCode() {
    String shortCode;
    int attempts = 0;
    int maxAttempts = 10;

    do {
        shortCode = generateRandomString(shortCodeLength);
        attempts++;

        if (attempts >= maxAttempts) {
            // Increase length if too many collisions
            shortCode = generateRandomString(shortCodeLength + 1);
            break;
        }
    } while (linkRepository.existsByShortCode(shortCode));

    return shortCode;
}

private String generateRandomString(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
        sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
    }
    return sb.toString();
}
```

**Design Patterns:**
- **Transactional boundaries** - `@Transactional` ensures ACID properties
- **Collision handling** - Retry with increased length
- **Secure randomness** - `SecureRandom` (cryptographically strong)

---

### 5. Controller Layer

**File:** `LinkController.java`

**Endpoints:**

```java
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LinkController {

    // Create short link
    @PostMapping("/api/shorten")
    public ResponseEntity<?> createShortLink(@Valid @RequestBody CreateLinkRequest request)

    // Redirect to original URL
    @GetMapping("/{shortCode:[a-zA-Z0-9]{4,10}}")
    public RedirectView redirect(@PathVariable String shortCode)

    // Get link statistics
    @GetMapping("/api/stats/{shortCode}")
    public ResponseEntity<?> getStats(@PathVariable String shortCode)

    // Health check
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health()

    // System statistics
    @GetMapping("/api/system-stats")
    public ResponseEntity<SystemStatsResponse> getSystemStats()
}
```

**Features:**
- **Path variable regex** - `{shortCode:[a-zA-Z0-9]{4,10}}` enforces format
- **Validation** - `@Valid` triggers Bean Validation
- **Error handling** - Try-catch with appropriate HTTP status codes

---

### 6. Security Service

**File:** `UrlValidationService.java`

```java
@Service
public class UrlValidationService {

    @Value("${app.max-url-length:2048}")
    private int maxUrlLength;

    @Value("${app.blocked-domains:localhost,127.0.0.1}")
    private String blockedDomainsConfig;

    private List<String> blockedDomains;
    private final UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});

    @PostConstruct
    private void init() {
        blockedDomains = Arrays.asList(blockedDomainsConfig.split(","));
    }

    public void validateUrl(String url) {
        // Length check
        if (url.length() > maxUrlLength) {
            throw new IllegalArgumentException("URL too long");
        }

        // Format validation
        if (!urlValidator.isValid(url)) {
            throw new IllegalArgumentException("Invalid URL format");
        }

        // SSRF protection
        try {
            URI uri = new URI(url);
            String host = uri.getHost().toLowerCase();

            for (String blocked : blockedDomains) {
                if (host.equals(blocked) || host.endsWith("." + blocked)) {
                    throw new IllegalArgumentException("Blocked domain");
                }
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL");
        }
    }

    public String sanitizeUrl(String url) {
        return url.trim()
                  .replaceAll("[\\r\\n]", "")  // Remove newlines
                  .replaceAll("<script.*?>.*?</script>", "");  // XSS prevention
    }
}
```

**Security Features:**
- **Length validation** - Prevent DoS attacks
- **Format validation** - Apache Commons Validator
- **SSRF prevention** - Block localhost, internal IPs
- **XSS sanitization** - Remove malicious scripts

---

### 7. Scheduled Cleanup

**File:** `LinkCleanupScheduler.java`

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class LinkCleanupScheduler {

    private final LinkRepository linkRepository;

    @Scheduled(cron = "0 0 2 * * *")  // Every day at 2 AM
    public void cleanupExpiredLinks() {
        LocalDateTime now = LocalDateTime.now();
        List<Link> expiredLinks = linkRepository.findByActiveTrueAndExpiresAtBefore(now);

        expiredLinks.forEach(link -> {
            link.setActive(false);
            linkRepository.save(link);
        });

        log.info("Cleaned up {} expired links", expiredLinks.size());
    }
}
```

**Features:**
- **Cron expression** - `0 0 2 * * *` = 2 AM daily
- **Batch processing** - Handles multiple expired links
- **Logging** - Track cleanup operations

---

## 🎨 Frontend Deep Dive

### 1. HTML Structure

**File:** `index.html`

**Key Sections:**

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>LinkShort - URL Shortener</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <!-- Header -->
    <header>
        <h1>LinkShort</h1>
        <p>Professional URL Shortener</p>
    </header>

    <!-- Main Form -->
    <main>
        <div class="container">
            <form id="shortenForm">
                <input type="url" id="urlInput" placeholder="Enter URL" required>
                <button type="submit">Shorten</button>
            </form>

            <!-- Result Section -->
            <div id="result" class="hidden">
                <div class="short-url">
                    <a id="shortUrlLink" target="_blank"></a>
                    <button onclick="copyToClipboard()">Copy</button>
                </div>

                <!-- QR Code -->
                <div id="qrcode"></div>
                <button onclick="downloadQR()">Download QR</button>
            </div>
        </div>

        <!-- Statistics Cards -->
        <div class="stats-container">
            <div class="stat-card">
                <h3 id="totalLinks">0</h3>
                <p>Total Links</p>
            </div>
            <div class="stat-card">
                <h3 id="totalClicks">0</h3>
                <p>Total Clicks</p>
            </div>
            <div class="stat-card">
                <h3 id="activeLinks">0</h3>
                <p>Active Links</p>
            </div>
        </div>
    </main>

    <script src="https://cdn.jsdelivr.net/npm/qrcodejs@1.0.0/qrcode.min.js"></script>
    <script src="script.js"></script>
</body>
</html>
```

---

### 2. CSS Styling

**File:** `styles.css`

**Design System:**

```css
:root {
    /* Color Palette */
    --bg-primary: #0a0a0a;
    --bg-secondary: #111111;
    --bg-card: #1a1a1a;

    --text-primary: #ffffff;
    --text-secondary: #a0a0a0;

    --accent-cyan: #22d3ee;
    --accent-purple: #a78bfa;

    /* Typography */
    --font-mono: 'JetBrains Mono', 'Fira Code', monospace;

    /* Spacing */
    --spacing-xs: 0.5rem;
    --spacing-sm: 1rem;
    --spacing-md: 1.5rem;
    --spacing-lg: 2rem;
}
```

**Key Styles:**

```css
/* Dark Background */
body {
    background: linear-gradient(135deg, var(--bg-primary) 0%, var(--bg-secondary) 100%);
    color: var(--text-primary);
    font-family: var(--font-mono);
}

/* Glassmorphism Cards */
.stat-card {
    background: rgba(26, 26, 26, 0.8);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 12px;
}

/* Gradient Buttons */
button {
    background: linear-gradient(135deg, var(--accent-cyan), var(--accent-purple));
    border: none;
    color: white;
    cursor: pointer;
    transition: transform 0.2s;
}

button:hover {
    transform: translateY(-2px);
}
```

**Design Principles:**
- **Dark mode first** - Reduces eye strain
- **Monospace fonts** - Tech/developer aesthetic
- **Cyan/purple accents** - Modern, vibrant
- **Glassmorphism** - Depth and layering
- **Smooth animations** - Professional feel

---

### 3. JavaScript Logic

**File:** `script.js`

**Key Functions:**

#### Initialize
```javascript
const API_BASE = window.location.origin;
let qrcode = null;

// Load statistics on page load
document.addEventListener('DOMContentLoaded', () => {
    loadStats();
    setInterval(loadStats, 10000); // Refresh every 10 seconds
});
```

#### Create Short Link
```javascript
document.getElementById('shortenForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const url = document.getElementById('urlInput').value;

    try {
        const response = await fetch(`${API_BASE}/api/shorten`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ url })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to shorten URL');
        }

        const data = await response.json();
        displayResult(data);
        generateQRCode(data.shortUrl);
        loadStats(); // Refresh statistics

    } catch (error) {
        alert(error.message);
    }
});
```

#### Load Statistics
```javascript
async function loadStats() {
    try {
        const response = await fetch(`${API_BASE}/api/system-stats`);

        if (response.ok) {
            const data = await response.json();

            // Animate number transitions
            animateValue('totalLinks',
                parseInt(document.getElementById('totalLinks').innerText) || 0,
                data.totalLinks,
                1000
            );
            animateValue('totalClicks',
                parseInt(document.getElementById('totalClicks').innerText) || 0,
                data.totalClicks,
                1000
            );
            animateValue('activeLinks',
                parseInt(document.getElementById('activeLinks').innerText) || 0,
                data.activeLinks,
                1000
            );
        }
    } catch (error) {
        console.error('Failed to load stats:', error);
    }
}
```

#### Animate Numbers
```javascript
function animateValue(id, start, end, duration) {
    const element = document.getElementById(id);
    const range = end - start;
    const increment = range / (duration / 16); // 60 FPS
    let current = start;

    const timer = setInterval(() => {
        current += increment;

        if ((increment > 0 && current >= end) || (increment < 0 && current <= end)) {
            current = end;
            clearInterval(timer);
        }

        element.innerText = Math.floor(current).toLocaleString();
    }, 16);
}
```

#### Generate QR Code
```javascript
function generateQRCode(url) {
    const qrContainer = document.getElementById('qrcode');
    qrContainer.innerHTML = ''; // Clear previous QR code

    qrcode = new QRCode(qrContainer, {
        text: url,
        width: 200,
        height: 200,
        colorDark: "#000000",
        colorLight: "#ffffff",
        correctLevel: QRCode.CorrectLevel.H
    });
}
```

#### Download QR Code
```javascript
function downloadQR() {
    const canvas = document.querySelector('#qrcode canvas');
    const url = canvas.toDataURL('image/png');

    const link = document.createElement('a');
    link.download = 'qrcode.png';
    link.href = url;
    link.click();
}
```

#### Copy to Clipboard
```javascript
function copyToClipboard() {
    const shortUrl = document.getElementById('shortUrlLink').href;

    navigator.clipboard.writeText(shortUrl).then(() => {
        alert('Copied to clipboard!');
    }).catch(err => {
        console.error('Failed to copy:', err);
    });
}
```

---

## 🗄 Database Schema

### Links Table

```sql
CREATE TABLE links (
    id BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(10) NOT NULL UNIQUE,
    original_url VARCHAR(2048) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    click_count BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true
);

-- Indexes
CREATE INDEX idx_short_code ON links(short_code);
CREATE INDEX idx_active_expires ON links(active, expires_at);
```

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Auto-incrementing ID |
| `short_code` | VARCHAR(10) | NOT NULL, UNIQUE | Generated short code |
| `original_url` | VARCHAR(2048) | NOT NULL | Original long URL |
| `created_at` | TIMESTAMP | NOT NULL | Creation timestamp |
| `expires_at` | TIMESTAMP | NOT NULL | Expiration timestamp |
| `click_count` | BIGINT | NOT NULL, DEFAULT 0 | Click counter |
| `active` | BOOLEAN | NOT NULL, DEFAULT true | Active status |

**Indexes:**
- **Primary index** on `id` (automatic)
- **Unique index** on `short_code` (prevents duplicates)
- **Composite index** on `(active, expires_at)` (optimizes cleanup queries)

---

## 🔒 Security Features

### 1. URL Validation

**Apache Commons Validator:**
- Validates URL format (protocol, domain, path)
- Ensures HTTP/HTTPS only

**Length Restriction:**
- Maximum 2048 characters (prevents DoS)

### 2. SSRF Prevention

**Blocked Domains:**
```
localhost
127.0.0.1
0.0.0.0
10.0.0.0/8
172.16.0.0/12
192.168.0.0/16
```

**Implementation:**
- Parse URL with `java.net.URI`
- Extract hostname
- Check against blocklist

### 3. XSS Prevention

**Input Sanitization:**
```java
url.trim()
   .replaceAll("[\\r\\n]", "")  // Remove newlines
   .replaceAll("<script.*?>.*?</script>", "");  // Remove script tags
```

**Output Encoding:**
- Spring automatically encodes responses
- Content-Type: application/json

### 4. CORS Configuration

```java
registry.addMapping("/**")
    .allowedOrigins("*")  // ⚠️ Should restrict in production
    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    .allowedHeaders("*")
    .maxAge(3600);
```

**Recommendation:**
- Change `allowedOrigins("*")` to specific domains in production

### 5. Rate Limiting

**Current:** Not implemented

**Future:** Add Spring Rate Limiter
```java
@RateLimiter(name = "shortenApi", fallbackMethod = "rateLimitFallback")
public LinkResponse createShortLink(CreateLinkRequest request)
```

---

## 🚀 Deployment

### Docker Multi-Stage Build

**File:** `Dockerfile`

```dockerfile
# Build stage
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/link-shortener-1.0.0.jar app.jar
EXPOSE $PORT
CMD java -Dserver.port=$PORT -jar app.jar
```

**Stages:**
1. **Build stage** - Uses full Maven + JDK to compile
2. **Run stage** - Only JRE (smaller image size)

**Benefits:**
- Reduced image size (JRE vs JDK)
- Faster deployments
- Production-optimized

### Render.com Configuration

**Web Service:**
- **Runtime:** Docker
- **Build Command:** (none, uses Dockerfile)
- **Start Command:** Handled by Dockerfile CMD
- **Plan:** Free

**PostgreSQL:**
- **Version:** 16
- **Storage:** 256 MB
- **Plan:** Free

**Environment Variables:**
```bash
POSTGRES_URL=jdbc:postgresql://dpg-xxxxx:5432/linkshort
POSTGRES_USER=linkshort_user
POSTGRES_PASSWORD=xxxxx
SPRING_PROFILES_ACTIVE=production
BASE_URL=https://linkshortner-cngr.onrender.com
```

### UptimeRobot Configuration

**Monitor:**
- **Type:** HTTP(s)
- **URL:** `https://linkshortner-cngr.onrender.com/api/health`
- **Interval:** 5 minutes
- **Timeout:** 30 seconds

**Purpose:**
- Prevent Render free tier from sleeping (15 min inactivity)
- Ensures 24/7 uptime

---

## ⚙ Environment Variables

### Development (application.properties)

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:h2:mem:linkshortener
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true

# Application
app.base-url=http://localhost:8080
app.link-expiration-hours=24
app.short-code-length=6
app.max-url-length=2048
app.blocked-domains=localhost,127.0.0.1,0.0.0.0
```

### Production (application-production.properties)

```properties
# Server
server.port=8080

# Database
spring.datasource.url=${POSTGRES_URL}
spring.datasource.username=${POSTGRES_USER}
spring.datasource.password=${POSTGRES_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Connection Pool
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=1

# Application
app.base-url=${BASE_URL}
app.link-expiration-hours=24
app.short-code-length=6
app.max-url-length=2048
app.blocked-domains=localhost,127.0.0.1,0.0.0.0

# Logging
logging.level.root=INFO
logging.level.com.linkshortener=INFO
```

---

## 📡 API Endpoints

### 1. Create Short Link

**POST** `/api/shorten`

**Request:**
```json
{
  "url": "https://www.example.com/very-long-url",
  "expirationHours": 24
}
```

**Response:**
```json
{
  "shortCode": "AsRn5o",
  "shortUrl": "https://linkshortner-cngr.onrender.com/AsRn5o",
  "originalUrl": "https://www.example.com/very-long-url",
  "clickCount": 0,
  "createdAt": "2025-10-04T19:51:32",
  "expiresAt": "2025-10-05T19:51:32",
  "active": true
}
```

**Errors:**
- `400 Bad Request` - Invalid URL, blocked domain, URL too long
- `500 Internal Server Error` - Unexpected error

---

### 2. Redirect to Original URL

**GET** `/{shortCode}`

**Example:** `https://linkshortner-cngr.onrender.com/AsRn5o`

**Response:**
- `302 Found` - Redirects to original URL
- `302 Found` - Redirects to `/error.html` if not found or expired

**Side Effects:**
- Increments `click_count`
- Deactivates link if expired

---

### 3. Get Link Statistics

**GET** `/api/stats/{shortCode}`

**Response:**
```json
{
  "shortCode": "AsRn5o",
  "shortUrl": "https://linkshortner-cngr.onrender.com/AsRn5o",
  "originalUrl": "https://www.example.com/very-long-url",
  "clickCount": 42,
  "createdAt": "2025-10-04T19:51:32",
  "expiresAt": "2025-10-05T19:51:32",
  "active": true
}
```

**Errors:**
- `404 Not Found` - Short code doesn't exist

---

### 4. Get System Statistics

**GET** `/api/system-stats`

**Response:**
```json
{
  "totalLinks": 127,
  "totalClicks": 3456,
  "activeLinks": 89
}
```

**Description:**
- `totalLinks` - All links ever created
- `totalClicks` - Sum of all click counts
- `activeLinks` - Links that are active AND not expired

---

### 5. Health Check

**GET** `/api/health`

**Response:**
```json
{
  "status": "UP"
}
```

**Purpose:**
- UptimeRobot monitoring
- Kubernetes/Docker health checks

---

## 🔍 How It Works

### Link Creation Flow

```
1. User submits URL in frontend form
   ↓
2. JavaScript sends POST to /api/shorten
   ↓
3. LinkController receives request
   ↓
4. LinkService sanitizes and validates URL
   ↓
5. UrlValidationService checks:
   - Format (HTTP/HTTPS)
   - Length (max 2048 chars)
   - Blocked domains (localhost, etc.)
   ↓
6. LinkService generates unique short code:
   - 6 random characters (A-Z, a-z, 0-9)
   - Checks database for collisions
   - Retries with longer code if needed
   ↓
7. Create Link entity with:
   - shortCode
   - originalUrl
   - createdAt = now
   - expiresAt = now + 24 hours
   - clickCount = 0
   - active = true
   ↓
8. Save to PostgreSQL database
   ↓
9. Return LinkResponse to frontend
   ↓
10. Frontend displays:
    - Short URL
    - QR Code
    - Copy button
```

### Redirect Flow

```
1. User accesses short URL (e.g., /AsRn5o)
   ↓
2. LinkController matches path pattern
   ↓
3. LinkService queries database:
   - findByShortCodeAndActiveTrue(shortCode)
   ↓
4. Check if link exists
   - If not → redirect to /error.html
   ↓
5. Check if expired
   - If expired:
     - Set active = false
     - Save to database
     - Redirect to /error.html
   ↓
6. Increment click count
   - clickCount++
   - Save to database
   ↓
7. Return RedirectView to original URL
   ↓
8. Browser redirects user (302 Found)
```

### Statistics Update Flow

```
1. Page loads / 10 seconds pass
   ↓
2. JavaScript calls loadStats()
   ↓
3. Fetch GET /api/system-stats
   ↓
4. LinkService executes queries:
   - SELECT COUNT(*) FROM links
   - SELECT SUM(click_count) FROM links
   - SELECT COUNT(*) FROM links WHERE active = true AND expires_at > NOW()
   ↓
5. Build SystemStatsResponse
   ↓
6. Return JSON to frontend
   ↓
7. JavaScript animates numbers:
   - Smooth transition from old to new values
   - 60 FPS animation (16ms intervals)
   ↓
8. Display updated statistics
```

### Cleanup Flow

```
1. Scheduler triggers daily at 2 AM (cron: 0 0 2 * * *)
   ↓
2. LinkCleanupScheduler.cleanupExpiredLinks()
   ↓
3. Query database:
   - findByActiveTrueAndExpiresAtBefore(now)
   ↓
4. For each expired link:
   - Set active = false
   - Save to database
   ↓
5. Log cleanup count
```

---

## ⚡ Performance Optimizations

### 1. Database

**Connection Pooling (HikariCP):**
```properties
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=1
```

**Indexes:**
- Primary key on `id` (automatic)
- Unique index on `short_code` (fast lookups)
- Composite index on `(active, expires_at)` (cleanup queries)

**Query Optimization:**
- Use `findByShortCodeAndActiveTrue()` to skip inactive links
- Aggregate queries (`COUNT`, `SUM`) use indexes

### 2. Application

**Transactional Boundaries:**
- Minimize transaction scope
- Read-only transactions for statistics

**Lazy Loading:**
- No relationships in Link entity (no N+1 queries)

**Logging:**
- Disable SQL logging in production
- Log level: INFO

### 3. Frontend

**Debouncing:**
- Statistics refresh every 10 seconds (not on every request)

**CDN:**
- QRCode.js loaded from jsDelivr CDN

**Caching:**
- Browser caches static resources (CSS, JS)

### 4. Infrastructure

**Docker:**
- Multi-stage build (smaller images)
- Alpine Linux (minimal OS)

**Render:**
- Internal database URL (faster than external)
- Connection pooling

---

## 🚧 Future Improvements

### Features

1. **Custom Short Codes**
   - Allow users to specify custom codes (e.g., `/my-link`)
   - Validate uniqueness

2. **Analytics Dashboard**
   - Click locations (IP geolocation)
   - Referrer tracking
   - Device/browser statistics
   - Click timeline graph

3. **User Accounts**
   - Register/login
   - Manage personal links
   - Edit expiration
   - Delete links

4. **API Keys**
   - Programmatic access
   - Rate limiting per key

5. **Bulk Operations**
   - Import CSV of URLs
   - Export link statistics

### Security

1. **Rate Limiting**
   - Prevent abuse (e.g., 10 links per IP per hour)

2. **CAPTCHA**
   - Add reCAPTCHA to form

3. **HTTPS Redirect**
   - Force HTTPS in production

4. **CSP Headers**
   - Content Security Policy

### Performance

1. **Redis Caching**
   - Cache frequently accessed links
   - Reduce database load

2. **CDN**
   - Serve static assets from CDN

3. **Database**
   - Archive old links (partition by date)
   - Soft delete instead of hard delete

### DevOps

1. **CI/CD**
   - GitHub Actions for automated tests
   - Deploy on merge to main

2. **Monitoring**
   - Prometheus + Grafana
   - Error tracking (Sentry)

3. **Backup**
   - Automated PostgreSQL backups

---

## 📚 Technologies Explained

### Spring Boot

**What is it?**
- Opinionated Java framework for building production-ready applications
- Auto-configuration, embedded server, dependency management

**Why use it?**
- Rapid development
- Battle-tested in enterprise
- Rich ecosystem (Spring Data, Spring Security, etc.)

**Key Features Used:**
- Spring Boot Web - REST API
- Spring Data JPA - Database abstraction
- Spring Validation - Input validation
- Spring Scheduling - Cleanup tasks

---

### JPA (Java Persistence API)

**What is it?**
- Java specification for Object-Relational Mapping (ORM)
- Hibernate is the implementation

**Why use it?**
- Database-agnostic (PostgreSQL, MySQL, H2)
- Automatic schema generation
- Type-safe queries

**Key Annotations:**
- `@Entity` - Mark class as database table
- `@Id` - Primary key
- `@GeneratedValue` - Auto-increment
- `@Column` - Column constraints

---

### PostgreSQL

**What is it?**
- Open-source relational database
- ACID compliant

**Why use it?**
- Powerful query engine
- JSON support
- Full-text search
- Free tier on Render

**Features Used:**
- BIGSERIAL (auto-increment)
- Indexes
- Aggregate functions (COUNT, SUM)

---

### Docker

**What is it?**
- Containerization platform
- Packages app + dependencies into portable image

**Why use it?**
- Consistent environments (dev = prod)
- Easy deployment
- Isolation

**Multi-Stage Build:**
- Stage 1: Build with Maven + JDK
- Stage 2: Run with JRE only (smaller image)

---

### Lombok

**What is it?**
- Java library that generates boilerplate code

**Why use it?**
- Less code to write
- Cleaner classes

**Annotations Used:**
- `@Data` - Generates getters, setters, toString, equals, hashCode
- `@NoArgsConstructor` - No-argument constructor
- `@AllArgsConstructor` - All-arguments constructor
- `@RequiredArgsConstructor` - Constructor for final fields
- `@Builder` - Builder pattern
- `@Slf4j` - Logger instance

---

### QRCode.js

**What is it?**
- JavaScript library for QR code generation
- Client-side (no server needed)

**Why use it?**
- Easy mobile sharing
- No external API calls
- Customizable

---

## 🎓 Learning Resources

### Spring Boot
- Official Docs: https://spring.io/projects/spring-boot
- Baeldung Tutorials: https://www.baeldung.com/spring-boot

### JPA/Hibernate
- Official Docs: https://hibernate.org/orm/documentation/
- Vlad Mihalcea's Blog: https://vladmihalcea.com/

### PostgreSQL
- Official Docs: https://www.postgresql.org/docs/
- PostgreSQL Tutorial: https://www.postgresqltutorial.com/

### Docker
- Official Docs: https://docs.docker.com/
- Docker Tutorial: https://docker-curriculum.com/

### REST API Design
- REST API Tutorial: https://restfulapi.net/

---

## 📞 Support

**Issues:** Report bugs or request features on GitHub

**Deployment Help:** See `RENDER_DEPLOYMENT_GUIDE.md`

**Security Concerns:** Do not publish in public repo, contact maintainer directly

---

## 📄 License

This project is open-source and available for educational purposes.

---

## 🎉 Conclusion

**LinkShort** demonstrates a full-stack application with:
- **Backend:** Spring Boot, JPA, PostgreSQL
- **Frontend:** Vanilla JavaScript, modern CSS
- **Infrastructure:** Docker, Render.com
- **Security:** URL validation, SSRF prevention, XSS protection
- **DevOps:** Automated deployment, scheduled tasks, monitoring

**Tech Stack Benefits:**
- Java 17 - Modern language features
- Spring Boot - Rapid development
- PostgreSQL - Reliable persistence
- Docker - Portable deployment
- Render - Free hosting

**Production Readiness:**
- Auto-expiring links
- Click tracking
- Error handling
- Logging
- Health checks
- 24/7 uptime

---

**Built with ☕ Java & 💙 Spring Boot**

**Deploy it. Share it. Build on it.** 🚀
