package com.upeu.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrearReviewRequest {
    @NotNull
    private Long solicitudId;

    @NotNull
    private Long clienteId;

    @NotNull
    private Long tecnicoId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer puntuacion;

    @Size(max = 500)
    private String comentario;
}
