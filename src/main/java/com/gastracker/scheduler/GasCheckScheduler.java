package com.gastracker.scheduler;

import com.gastracker.service.AlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GasCheckScheduler {

    private final AlertService alertService;

    public GasCheckScheduler(AlertService alertService) {
        this.alertService = alertService;
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void checkGasPriceAndSendAlerts() {
        log.info("Iniciando verificacao de gas price...");

        try {
            alertService.checkAndSendAlerts();
            log.info("Verificacao de gas price concluida");
        } catch (Exception e) {
            log.error("Erro durante verificacao de gas price: {}", e.getMessage(), e);
        }
    }
}
