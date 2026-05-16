package com.upeu.technician.dto.response;

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
public class TecnicoResponse {
    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private Double lat;
    private Double lng;
    private Double ranking;
    private Boolean activo;
    private Instant createdAt;
    private Instant updatedAt;
}
