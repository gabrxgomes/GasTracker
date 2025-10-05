package com.gastracker.service;

import com.gastracker.model.GasAlert;
import com.gastracker.model.User;
import com.gastracker.repository.GasAlertRepository;
import com.gastracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final UserRepository userRepository;
    private final GasAlertRepository gasAlertRepository;
    private final TelegramBotService telegramBotService;
    private final GasService gasService;

    /**
     * Verifica gas price e envia alertas para usuários elegíveis
     */
    @Transactional
    public void checkAndSendAlerts() {
        Integer currentGasPrice = gasService.getCurrentGasPrice();

        if (currentGasPrice == null) {
            log.warn("Não foi possível obter o gas price atual. Pulando verificação.");
            return;
        }

        log.info("Gas price atual: {} Gwei", currentGasPrice);

        // Busca usuários ativos com threshold maior ou igual ao gas atual
        List<User> eligibleUsers = userRepository.findActiveUsersWithGasPriceAbove(currentGasPrice);

        log.info("Encontrados {} usuários elegíveis para alerta", eligibleUsers.size());

        for (User user : eligibleUsers) {
            // Verifica se já enviou alerta recentemente (cooldown de 1 hora)
            if (shouldSendAlert(user)) {
                sendAlertToUser(user, currentGasPrice);
            }
        }
    }

    /**
     * Verifica se deve enviar alerta (cooldown)
     */
    private boolean shouldSendAlert(User user) {
        if (user.getLastNotificationAt() == null) {
            return true;
        }

        LocalDateTime cooldownTime = user.getLastNotificationAt().plusHours(1);
        return LocalDateTime.now().isAfter(cooldownTime);
    }

    /**
     * Envia alerta para usuário específico
     */
    private void sendAlertToUser(User user, Integer currentGasPrice) {
        if (user.getChatId() == null) {
            log.warn("Usuário {} não tem chatId configurado", user.getTelegramUsername());
            return;
        }

        boolean success = telegramBotService.sendGasAlert(
            user.getChatId(),
            currentGasPrice,
            user.getMaxGasPrice()
        );

        // Registra o alerta
        GasAlert alert = new GasAlert();
        alert.setUser(user);
        alert.setGasPrice(currentGasPrice);
        alert.setSuccess(success);
        gasAlertRepository.save(alert);

        // Atualiza timestamp do último alerta
        if (success) {
            user.setLastNotificationAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("Alerta enviado com sucesso para {}", user.getTelegramUsername());
        } else {
            log.error("Falha ao enviar alerta para {}", user.getTelegramUsername());
        }
    }

    /**
     * Obtém estatísticas de alertas
     */
    public AlertStats getAlertStats() {
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);

        long totalAlerts = gasAlertRepository.countAlertsSince(last24Hours);
        long successfulAlerts = gasAlertRepository.countSuccessfulAlertsSince(last24Hours);
        long activeUsers = userRepository.countActiveUsers();

        return new AlertStats(totalAlerts, successfulAlerts, activeUsers);
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class AlertStats {
        private long totalAlerts24h;
        private long successfulAlerts24h;
        private long activeUsers;
    }
}
