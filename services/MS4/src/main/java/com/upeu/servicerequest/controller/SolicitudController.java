package com.upeu.servicerequest.controller;

import com.upeu.servicerequest.dto.request.ActualizarEstadoSolicitudRequest;
import com.upeu.servicerequest.dto.request.CrearSolicitudRequest;
import com.upeu.servicerequest.dto.response.SolicitudResponse;
import com.upeu.servicerequest.service.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {
    private final SolicitudService solicitudService;

    @PostMapping
    public ResponseEntity<SolicitudResponse> crear(@Valid @RequestBody CrearSolicitudRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitudService.crear(request));
    }

    @GetMapping("/{id}")
    public SolicitudResponse obtener(@PathVariable Long id) {
        return solicitudService.obtener(id);
    }

    @PutMapping("/{solicitudId}/estado")
    public ResponseEntity<Void> actualizarEstado(
            @PathVariable Long solicitudId,
            @Valid @RequestBody ActualizarEstadoSolicitudRequest request
    ) {
        solicitudService.actualizarEstado(solicitudId, request);
        return ResponseEntity.noContent().build();
    }
}
