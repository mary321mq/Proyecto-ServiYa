package com.upeu.payment.dto.external;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudResponse {
    private Long id;
    private Long clienteId;
    private String estado;
    private Long tecnicoAsignadoId;
}
