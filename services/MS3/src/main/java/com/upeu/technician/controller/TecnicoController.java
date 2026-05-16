package com.upeu.technician.controller;

import com.upeu.technician.dto.request.ActualizarRankingRequest;
import com.upeu.technician.dto.request.ActualizarTecnicoRequest;
import com.upeu.technician.dto.request.ActualizarUbicacionRequest;
import com.upeu.technician.dto.request.CrearTecnicoRequest;
import com.upeu.technician.dto.response.TecnicoCercanoDto;
import com.upeu.technician.dto.response.TecnicoResponse;
import com.upeu.technician.service.TecnicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tecnicos")
@RequiredArgsConstructor
public class TecnicoController {
    private final TecnicoService tecnicoService;

    @PostMapping
    public ResponseEntity<TecnicoResponse> crear(@Valid @RequestBody CrearTecnicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tecnicoService.crear(request));
    }

    @GetMapping("/{id}")
    public TecnicoResponse obtener(@PathVariable Long id) {
        return tecnicoService.obtener(id);
    }

    @PutMapping("/{id}")
    public TecnicoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarTecnicoRequest request) {
        return tecnicoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tecnicoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/ubicacion")
    public TecnicoResponse actualizarUbicacion(@PathVariable Long id, @Valid @RequestBody ActualizarUbicacionRequest request) {
        return tecnicoService.actualizarUbicacion(id, request);
    }

    @PutMapping("/{id}/ranking")
    public TecnicoResponse actualizarRanking(@PathVariable Long id, @Valid @RequestBody ActualizarRankingRequest request) {
        return tecnicoService.actualizarRanking(id, request);
    }

    @GetMapping("/cercanos")
    public List<TecnicoCercanoDto> cercanos(
            @RequestParam("solicitudId") Long solicitudId,
            @RequestParam(value = "radioKm", required = false) Double radioKm
    ) {
        return tecnicoService.buscarCercanosPorSolicitud(solicitudId, radioKm);
    }
}
