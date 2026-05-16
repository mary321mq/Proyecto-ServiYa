package com.upeu.technician.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarUbicacionRequest {
    @NotNull
    @Min(-90)
    @Max(90)
    private Double lat;

    @NotNull
    @Min(-180)
    @Max(180)
    private Double lng;
}
