package com.upeu.technician.service;

import com.upeu.technician.client.MsServiceRequestClient;
import com.upeu.technician.dto.SolicitudDto;
import com.upeu.technician.dto.request.ActualizarRankingRequest;
import com.upeu.technician.dto.request.ActualizarTecnicoRequest;
import com.upeu.technician.dto.request.ActualizarUbicacionRequest;
import com.upeu.technician.dto.request.CrearTecnicoRequest;
import com.upeu.technician.dto.response.TecnicoCercanoDto;
import com.upeu.technician.dto.response.TecnicoResponse;
import com.upeu.technician.entity.Tecnico;
import com.upeu.technician.repository.TecnicoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TecnicoService {
    private final TecnicoRepository tecnicoRepository;
    private final MsServiceRequestClient msServiceRequestClient;

    @Transactional
    public TecnicoResponse crear(@Valid CrearTecnicoRequest request) {
        Tecnico t = Tecnico.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .lat(null)
                .lng(null)
                .ranking(0.0)
                .activo(true)
                .createdAt(Instant.now())
                .updatedAt(null)
                .build();
        return toResponse(tecnicoRepository.save(t));
    }

    @Transactional(readOnly = true)
    public TecnicoResponse obtener(Long id) {
        return tecnicoRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Técnico no encontrado"));
    }

    @Transactional
    public TecnicoResponse actualizar(Long id, @Valid ActualizarTecnicoRequest request) {
        Tecnico t = tecnicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Técnico no encontrado"));
        t.setNombre(request.getNombre());
        t.setTelefono(request.getTelefono());
        if (request.getActivo() != null) {
            t.setActivo(request.getActivo());
        }
        t.setUpdatedAt(Instant.now());
        return toResponse(tecnicoRepository.save(t));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!tecnicoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Técnico no encontrado");
        }
        tecnicoRepository.deleteById(id);
    }

    @Transactional
    public TecnicoResponse actualizarUbicacion(Long id, @Valid ActualizarUbicacionRequest request) {
        Tecnico t = tecnicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Técnico no encontrado"));
        t.setLat(request.getLat());
        t.setLng(request.getLng());
        t.setUpdatedAt(Instant.now());
        return toResponse(tecnicoRepository.save(t));
    }

    @Transactional
    public TecnicoResponse actualizarRanking(Long id, @Valid ActualizarRankingRequest request) {
        Tecnico t = tecnicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Técnico no encontrado"));
        t.setRanking(request.getRanking());
        t.setUpdatedAt(Instant.now());
        return toResponse(tecnicoRepository.save(t));
    }

    @Transactional(readOnly = true)
    public List<TecnicoCercanoDto> buscarCercanosPorSolicitud(Long solicitudId, Double radioKm) {
        SolicitudDto solicitud = obtenerSolicitud(solicitudId);
        if (solicitud == null || solicitud.getLat() == null || solicitud.getLng() == null) {
            return List.of();
        }

        return tecnicoRepository.findByActivoTrue().stream()
                .filter(t -> t.getLat() != null && t.getLng() != null)
                .map(t -> {
                    double distancia = haversineKm(solicitud.getLat(), solicitud.getLng(), t.getLat(), t.getLng());
                    return TecnicoCercanoDto.builder()
                            .tecnicoId(t.getId())
                            .ranking(t.getRanking())
                            .distanciaKm(distancia)
                            .build();
                })
                .filter(d -> radioKm == null || d.getDistanciaKm() == null || d.getDistanciaKm() <= radioKm)
                .sorted(Comparator.comparing(TecnicoCercanoDto::getDistanciaKm, Comparator.nullsLast(Double::compareTo)))
                .collect(Collectors.toList());
    }

    private SolicitudDto obtenerSolicitud(Long solicitudId) {
        try {
            return msServiceRequestClient.obtenerSolicitud(solicitudId);
        } catch (Exception ex) {
            log.warn("Fallo consultando solicitudId={}", solicitudId, ex);
            return null;
        }
    }

    private TecnicoResponse toResponse(Tecnico t) {
        return TecnicoResponse.builder()
                .id(t.getId())
                .nombre(t.getNombre())
                .email(t.getEmail())
                .telefono(t.getTelefono())
                .lat(t.getLat())
                .lng(t.getLng())
                .ranking(t.getRanking())
                .activo(t.getActivo())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }
}
