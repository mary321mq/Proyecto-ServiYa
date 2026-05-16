package com.upeu.technician.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TecnicoCercanoDto {
    private Long tecnicoId;
    private Double ranking;
    private Double distanciaKm;
}
