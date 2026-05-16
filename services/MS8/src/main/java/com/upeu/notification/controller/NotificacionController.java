package com.upeu.notification.controller;

import com.upeu.notification.dto.request.CrearNotificacionRequest;
import com.upeu.notification.dto.response.NotificacionResponse;
import com.upeu.notification.service.NotificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {
    private final NotificacionService notificacionService;

    @PostMapping
    public ResponseEntity<NotificacionResponse> crear(@Valid @RequestBody CrearNotificacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificacionService.crear(request));
    }

    @GetMapping("/{id}")
    public NotificacionResponse obtener(@PathVariable Long id) {
        return notificacionService.obtener(id);
    }

    @GetMapping
    public List<NotificacionResponse> listar(@RequestParam("destinatario") String destinatario) {
        return notificacionService.listarPorDestinatario(destinatario);
    }
}

