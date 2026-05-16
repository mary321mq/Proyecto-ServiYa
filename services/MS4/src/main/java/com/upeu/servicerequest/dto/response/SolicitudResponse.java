package com.upeu.servicerequest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudResponse {
    private Long id;
    private Long clienteId;
    private String descripcion;
    private Double lat;
    private Double lng;
    private String estado;
    private Long tecnicoAsignadoId;
    private Instant createdAt;
    private Instant updatedAt;
}
