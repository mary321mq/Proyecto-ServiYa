package com.upeu.technician.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarTecnicoRequest {
    @NotBlank
    @Size(max = 200)
    private String nombre;

    @Size(max = 50)
    private String telefono;

    private Boolean activo;
}
