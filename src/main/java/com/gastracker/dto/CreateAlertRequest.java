package com.gastracker.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateAlertRequest {

    @NotBlank(message = "Username do Telegram é obrigatório")
    @Size(min = 5, max = 32, message = "Username deve ter entre 5 e 32 caracteres")
    private String telegramUsername;

    @NotNull(message = "Preço máximo de gas é obrigatório")
    @Min(value = 1, message = "Gas price mínimo é 1 Gwei")
    @Max(value = 1000, message = "Gas price máximo é 1000 Gwei")
    private Integer maxGasPrice;
}
