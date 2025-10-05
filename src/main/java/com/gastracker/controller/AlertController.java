package com.gastracker.controller;

import com.gastracker.dto.AlertResponse;
import com.gastracker.dto.CreateAlertRequest;
import com.gastracker.model.User;
import com.gastracker.repository.UserRepository;
import com.gastracker.service.AlertService;
import com.gastracker.service.GasService;
import com.gastracker.service.ValidationService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AlertController {

    private final UserRepository userRepository;
    private final ValidationService validationService;
    private final GasService gasService;
    private final AlertService alertService;

    // Rate limiting: 10 requisições por IP a cada 10 minutos
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillGreedy(10, Duration.ofMinutes(10))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket resolveBucket(String ip) {
        return cache.computeIfAbsent(ip, k -> createNewBucket());
    }

    /**
     * Cria novo alerta de gas price
     */
    @PostMapping("/alert")
    public ResponseEntity<?> createAlert(
            @Valid @RequestBody CreateAlertRequest request,
            @RequestHeader(value = "X-Forwarded-For", required = false) String xForwardedFor,
            @RequestHeader(value = "X-Real-IP", required = false) String xRealIp
    ) {
        try {
            // Rate limiting
            String clientIp = xForwardedFor != null ? xForwardedFor : (xRealIp != null ? xRealIp : "unknown");
            Bucket bucket = resolveBucket(clientIp);

            if (!bucket.tryConsume(1)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Muitas requisições. Tente novamente mais tarde."));
            }

            // Valida e sanitiza username
            String cleanUsername = validationService.validateAndSanitizeTelegramUsername(
                    request.getTelegramUsername()
            );

            // Valida gas price
            validationService.validateGasPrice(request.getMaxGasPrice());

            // Verifica se usuário já existe
            Optional<User> existingUser = userRepository.findByTelegramUsername(cleanUsername);

            User user;
            String message;

            if (existingUser.isPresent()) {
                // Atualiza usuário existente
                user = existingUser.get();
                user.setMaxGasPrice(request.getMaxGasPrice());
                user.setIsActive(true);
                message = "Alerta atualizado com sucesso! Use /start no bot do Telegram para ativar.";
            } else {
                // Cria novo usuário
                user = new User();
                user.setTelegramUsername(cleanUsername);
                user.setMaxGasPrice(request.getMaxGasPrice());
                user.setIsActive(true);
                message = "Alerta criado com sucesso! Use /start no bot do Telegram (@" +
                         System.getenv("TELEGRAM_BOT_USERNAME") + ") para ativar.";
            }

            userRepository.save(user);

            log.info("Alerta criado/atualizado para usuário: {} com gas price: {}",
                    cleanUsername, request.getMaxGasPrice());

            return ResponseEntity.ok(new AlertResponse(
                    cleanUsername,
                    request.getMaxGasPrice(),
                    message
            ));

        } catch (IllegalArgumentException e) {
            log.warn("Validação falhou: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Erro ao criar alerta: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    /**
     * Obtém gas price atual
     */
    @GetMapping("/gas-price")
    public ResponseEntity<?> getCurrentGasPrice() {
        try {
            Integer gasPrice = gasService.getCurrentGasPrice();

            if (gasPrice == null) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "Não foi possível obter o gas price"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("gasPrice", gasPrice);
            response.put("unit", "Gwei");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao obter gas price: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    /**
     * Obtém estatísticas do sistema
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        try {
            AlertService.AlertStats stats = alertService.getAlertStats();

            Map<String, Object> response = new HashMap<>();
            response.put("activeUsers", stats.getActiveUsers());
            response.put("totalAlerts24h", stats.getTotalAlerts24h());
            response.put("successfulAlerts24h", stats.getSuccessfulAlerts24h());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao obter estatísticas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
