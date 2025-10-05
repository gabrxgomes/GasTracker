package com.gastracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
public class GasService {

    @Value("${etherscan.api.key}")
    private String etherscanApiKey;

    @Value("${etherscan.api.url:https://api.etherscan.io/v2/api}")
    private String etherscanApiUrl;

    private final WebClient webClient;

    public GasService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Obtém o preço atual do gas em Gwei da Etherscan API v2
     *
     * @return Gas price em Gwei
     */
    public Integer getCurrentGasPrice() {
        try {
            String url = String.format("%s?chainid=1&module=gastracker&action=gasoracle&apikey=%s",
                    etherscanApiUrl, etherscanApiKey);

            log.info("Chamando Etherscan API v2: {}", url.replace(etherscanApiKey, "***"));

            EtherscanGasResponse response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(EtherscanGasResponse.class)
                    .block();

            log.info("Resposta da Etherscan: status={}, message={}, result={}",
                    response != null ? response.getStatus() : "null",
                    response != null ? response.getMessage() : "null",
                    response != null ? response.getResult() : "null");

            if (response != null && "1".equals(response.getStatus()) && response.getResult() != null) {
                GasResult result = response.getResult();

                // Tenta pegar SafeGasPrice (API v1 legacy)
                String safeGasPrice = result.getSafeGasPrice();

                // Se não tiver, usa suggestBaseFee (API v2) + priority fee
                if (safeGasPrice == null || safeGasPrice.isEmpty()) {
                    String suggestBaseFee = result.getSuggestBaseFee();
                    log.info("Usando suggestBaseFee da API v2: {}", suggestBaseFee);

                    if (suggestBaseFee != null && !suggestBaseFee.isEmpty()) {
                        // API v2: suggestBaseFee + priority fee (estimado 2 Gwei)
                        // Base fee está em Gwei decimal, precisamos adicionar priority fee
                        BigDecimal baseFee = new BigDecimal(suggestBaseFee);
                        BigDecimal priorityFee = new BigDecimal("2"); // Priority fee médio
                        Integer gasPrice = baseFee.add(priorityFee)
                                .setScale(0, RoundingMode.HALF_UP)
                                .intValue();

                        log.info("Gas price calculado (v2): base={} + priority=2 = {} Gwei",
                                baseFee.setScale(2, RoundingMode.HALF_UP), gasPrice);
                        return gasPrice;
                    }
                } else {
                    log.info("SafeGasPrice extraído (v1): {}", safeGasPrice);
                    Integer gasPrice = new BigDecimal(safeGasPrice)
                            .setScale(0, RoundingMode.HALF_UP)
                            .intValue();
                    log.info("Gas price calculado (v1): {} Gwei", gasPrice);
                    return gasPrice;
                }
            }

            log.warn("Resposta inválida da Etherscan API v2: {}", response);
            return null;

        } catch (WebClientResponseException e) {
            log.error("Erro ao consultar Etherscan API v2: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("Erro inesperado ao obter gas price: {}", e.getMessage(), e);
            return null;
        }
    }

    // DTOs para resposta da Etherscan API v2
    @lombok.Data
    private static class EtherscanGasResponse {
        private String status;
        private String message;
        private GasResult result;
    }

    @lombok.Data
    private static class GasResult {
        private String LastBlock;
        private String SafeGasPrice;
        private String ProposeGasPrice;
        private String FastGasPrice;
        private String suggestBaseFee;  // API v2 - base fee em Gwei (decimal)
        private String gasUsedRatio;
    }
}
