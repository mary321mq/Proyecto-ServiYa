package com.upeu.notification.mapper;

import com.upeu.notification.dto.response.NotificacionResponse;
import com.upeu.notification.entity.Notificacion;
import org.springframework.stereotype.Component;

@Component
public class NotificacionMapper {
    public NotificacionResponse toResponse(Notificacion n) {
        return NotificacionResponse.builder()
                .id(n.getId())
                .destinatario(n.getDestinatario())
                .canal(n.getCanal())
                .titulo(n.getTitulo())
                .mensaje(n.getMensaje())
                .estado(n.getEstado())
                .createdAt(n.getCreatedAt())
                .build();
    }
}

