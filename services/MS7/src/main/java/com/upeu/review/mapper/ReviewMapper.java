package com.upeu.review.mapper;

import com.upeu.review.dto.response.ReviewResponse;
import com.upeu.review.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    public ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .solicitudId(r.getSolicitudId())
                .clienteId(r.getClienteId())
                .tecnicoId(r.getTecnicoId())
                .puntuacion(r.getPuntuacion())
                .comentario(r.getComentario())
                .createdAt(r.getCreatedAt())
                .build();
    }
}

