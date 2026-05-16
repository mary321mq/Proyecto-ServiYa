package com.upeu.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponse {
    private Long id;
    private Long solicitudId;
    private Long clienteId;
    private Long tecnicoId;
    private BigDecimal monto;
    private BigDecimal comision;
    private BigDecimal neto;
    private String metodo;
    private String estado;
    private Instant createdAt;
    private Instant updatedAt;
}
