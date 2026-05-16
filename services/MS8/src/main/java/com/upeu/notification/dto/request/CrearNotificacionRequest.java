package com.upeu.notification.dto.request;

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
public class CrearNotificacionRequest {
    @NotBlank
    @Size(max = 255)
    private String destinatario;

    @NotBlank
    @Size(max = 30)
    private String canal;

    @NotBlank
    @Size(max = 150)
    private String titulo;

    @NotBlank
    @Size(max = 500)
    private String mensaje;
}

