package com.upeu.servicerequest.service;

import com.upeu.servicerequest.dto.request.ActualizarEstadoSolicitudRequest;
import com.upeu.servicerequest.dto.request.CrearSolicitudRequest;
import com.upeu.servicerequest.dto.response.SolicitudResponse;
import com.upeu.servicerequest.entity.SolicitudServicio;
import com.upeu.servicerequest.repository.SolicitudRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SolicitudService {
    private final SolicitudRepository solicitudRepository;

    @Transactional
    public SolicitudResponse crear(@Valid CrearSolicitudRequest request) {
        SolicitudServicio s = SolicitudServicio.builder()
                .clienteId(request.getClienteId())
                .descripcion(request.getDescripcion())
                .lat(request.getLat())
                .lng(request.getLng())
                .estado("CREADA")
                .tecnicoAsignadoId(null)
                .createdAt(Instant.now())
                .updatedAt(null)
                .build();
        return toResponse(solicitudRepository.save(s));
    }

    @Transactional(readOnly = true)
    public SolicitudResponse obtener(Long id) {
        return solicitudRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
    }

    @Transactional
    public void actualizarEstado(Long id, @Valid ActualizarEstadoSolicitudRequest request) {
        SolicitudServicio s = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
        s.setEstado(request.getEstado());
        if (request.getTecnicoId() != null) {
            s.setTecnicoAsignadoId(request.getTecnicoId());
        }
        s.setUpdatedAt(Instant.now());
        solicitudRepository.save(s);
    }

    private SolicitudResponse toResponse(SolicitudServicio s) {
        return SolicitudResponse.builder()
                .id(s.getId())
                .clienteId(s.getClienteId())
                .descripcion(s.getDescripcion())
                .lat(s.getLat())
                .lng(s.getLng())
                .estado(s.getEstado())
                .tecnicoAsignadoId(s.getTecnicoAsignadoId())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
