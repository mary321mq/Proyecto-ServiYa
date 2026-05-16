package com.upeu.review.service;

import com.upeu.review.client.MsTechnicianClient;
import com.upeu.review.dto.request.ActualizarRankingRequest;
import com.upeu.review.dto.request.CrearReviewRequest;
import com.upeu.review.dto.response.ReputacionResponse;
import com.upeu.review.dto.response.ReviewResponse;
import com.upeu.review.entity.Review;
import com.upeu.review.mapper.ReviewMapper;
import com.upeu.review.repository.ReviewRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final MsTechnicianClient msTechnicianClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewResponse crear(@Valid CrearReviewRequest request) {
        Review r = Review.builder()
                .solicitudId(request.getSolicitudId())
                .clienteId(request.getClienteId())
                .tecnicoId(request.getTecnicoId())
                .puntuacion(request.getPuntuacion())
                .comentario(request.getComentario())
                .createdAt(Instant.now())
                .build();

        Review saved = reviewRepository.save(r);
        actualizarRankingTecnico(request.getTecnicoId());
        return reviewMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> listarPorTecnico(Long tecnicoId) {
        return reviewRepository.findByTecnicoId(tecnicoId).stream().map(reviewMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReputacionResponse reputacion(Long tecnicoId) {
        long total = reviewRepository.findByTecnicoId(tecnicoId).size();
        Double prom = reviewRepository.promedioByTecnicoId(tecnicoId);
        return ReputacionResponse.builder()
                .tecnicoId(tecnicoId)
                .promedio(prom != null ? prom : 0.0)
                .totalReviews(total)
                .build();
    }

    private void actualizarRankingTecnico(Long tecnicoId) {
        Double prom = reviewRepository.promedioByTecnicoId(tecnicoId);
        double ranking = prom != null ? prom : 0.0;
        CircuitBreaker cb = circuitBreakerFactory.create("msTechnician");
        cb.run(
                () -> {
                    msTechnicianClient.actualizarRanking(tecnicoId, new ActualizarRankingRequest(ranking));
                    return null;
                },
                ex -> {
                    log.warn("Fallo actualizando ranking tecnicoId={}: {}", tecnicoId, Objects.toString(ex));
                    return null;
                }
        );
    }
}

