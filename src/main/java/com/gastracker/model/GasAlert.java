package com.gastracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "gas_alerts", indexes = {
    @Index(name = "idx_user_sent", columnList = "user_id, sent_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GasAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "gas_price", nullable = false)
    private Integer gasPrice; // Gas price em Gwei

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "success", nullable = false)
    private Boolean success = true;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
        if (success == null) {
            success = true;
        }
    }
}
