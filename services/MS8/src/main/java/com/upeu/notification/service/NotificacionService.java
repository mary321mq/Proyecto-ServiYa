package com.upeu.notification.service;

import com.upeu.notification.dto.request.CrearNotificacionRequest;
import com.upeu.notification.dto.response.NotificacionResponse;
import com.upeu.notification.entity.Notificacion;
import com.upeu.notification.exception.NotificacionNotFoundException;
import com.upeu.notification.mapper.NotificacionMapper;
import com.upeu.notification.repository.NotificacionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {
    private final NotificacionRepository notificacionRepository;
    private final NotificacionMapper notificacionMapper;

    @Transactional
    public NotificacionResponse crear(@Valid CrearNotificacionRequest request) {
        Notificacion n = Notificacion.builder()
                .destinatario(request.getDestinatario())
                .canal(request.getCanal().toUpperCase())
                .titulo(request.getTitulo())
                .mensaje(request.getMensaje())
                .estado("ENVIADA")
                .createdAt(Instant.now())
                .build();
        return notificacionMapper.toResponse(notificacionRepository.save(n));
    }

    @Transactional(readOnly = true)
    public NotificacionResponse obtener(Long id) {
        return notificacionRepository.findById(id)
                .map(notificacionMapper::toResponse)
                .orElseThrow(() -> new NotificacionNotFoundException("Notificación no encontrada"));
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarPorDestinatario(String destinatario) {
        return notificacionRepository.findByDestinatario(destinatario).stream().map(notificacionMapper::toResponse).toList();
    }
}

