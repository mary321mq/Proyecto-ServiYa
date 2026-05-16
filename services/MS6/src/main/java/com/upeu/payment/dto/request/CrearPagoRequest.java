package com.upeu.payment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrearPagoRequest {
    @NotNull
    private Long solicitudId;

    @NotNull
    private Long clienteId;

    @NotNull
    private Long tecnicoId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal monto;

    @NotBlank
    @Size(max = 30)
    private String metodo;
}
