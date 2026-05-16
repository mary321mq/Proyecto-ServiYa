package com.upeu.review.dto.response;

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
public class ReviewResponse {
    private Long id;
    private Long solicitudId;
    private Long clienteId;
    private Long tecnicoId;
    private Integer puntuacion;
    private String comentario;
    private Instant createdAt;
}

