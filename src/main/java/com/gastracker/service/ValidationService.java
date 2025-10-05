package com.gastracker.service;

import lombok.extern.slf4j.Slf4j;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@Slf4j
public class ValidationService {

    private static final Pattern TELEGRAM_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{5,32}$");
    private static final int MIN_GAS_PRICE = 1;
    private static final int MAX_GAS_PRICE = 1000;

    private final PolicyFactory sanitizer = Sanitizers.FORMATTING.and(Sanitizers.BLOCKS);

    /**
     * Valida e sanitiza username do Telegram
     */
    public String validateAndSanitizeTelegramUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username do Telegram não pode ser vazio");
        }

        // Remove @ se presente
        String cleanUsername = username.trim().replaceFirst("^@", "");

        // Sanitiza contra XSS
        cleanUsername = sanitizer.sanitize(cleanUsername);

        // Valida formato
        if (!TELEGRAM_USERNAME_PATTERN.matcher(cleanUsername).matches()) {
            throw new IllegalArgumentException(
                "Username inválido. Deve ter 5-32 caracteres (letras, números, underscore)"
            );
        }

        return cleanUsername.toLowerCase();
    }

    /**
     * Valida gas price
     */
    public void validateGasPrice(Integer gasPrice) {
        if (gasPrice == null) {
            throw new IllegalArgumentException("Gas price não pode ser nulo");
        }

        if (gasPrice < MIN_GAS_PRICE || gasPrice > MAX_GAS_PRICE) {
            throw new IllegalArgumentException(
                String.format("Gas price deve estar entre %d e %d Gwei", MIN_GAS_PRICE, MAX_GAS_PRICE)
            );
        }
    }

    /**
     * Sanitiza texto genérico
     */
    public String sanitizeText(String text) {
        if (text == null) {
            return null;
        }
        return sanitizer.sanitize(text.trim());
    }
}
