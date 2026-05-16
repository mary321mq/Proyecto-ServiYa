package com.upeu.review.controller;

import com.upeu.review.dto.request.CrearReviewRequest;
import com.upeu.review.dto.response.ReputacionResponse;
import com.upeu.review.dto.response.ReviewResponse;
import com.upeu.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> crear(@Valid @RequestBody CrearReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.crear(request));
    }

    @GetMapping("/tecnico/{tecnicoId}")
    public List<ReviewResponse> listarPorTecnico(@PathVariable Long tecnicoId) {
        return reviewService.listarPorTecnico(tecnicoId);
    }

    @GetMapping("/tecnico/{tecnicoId}/reputacion")
    public ReputacionResponse reputacion(@PathVariable Long tecnicoId) {
        return reviewService.reputacion(tecnicoId);
    }
}

