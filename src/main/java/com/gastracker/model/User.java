package com.gastracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_telegram_username", columnList = "telegram_username", unique = true),
    @Index(name = "idx_chat_id", columnList = "chat_id", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_username", nullable = false, unique = true, length = 100)
    private String telegramUsername;

    @Column(name = "chat_id", unique = true)
    private Long chatId;

    @Column(name = "max_gas_price", nullable = false)
    private Integer maxGasPrice; // Gas price em Gwei

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_notification_at")
    private LocalDateTime lastNotificationAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }
}
