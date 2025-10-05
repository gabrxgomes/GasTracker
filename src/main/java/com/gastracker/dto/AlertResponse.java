package com.gastracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlertResponse {
    private String telegramUsername;
    private Integer maxGasPrice;
    private String message;
}
