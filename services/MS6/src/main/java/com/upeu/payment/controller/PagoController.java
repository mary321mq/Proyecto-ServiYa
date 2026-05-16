package com.upeu.payment.controller;

import com.upeu.payment.dto.request.ActualizarEstadoPagoRequest;
import com.upeu.payment.dto.request.ActualizarPagoRequest;
import com.upeu.payment.dto.request.CrearPagoRequest;
import com.upeu.payment.dto.response.PagoResponse;
import com.upeu.payment.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
public class PagoController {
    private final PagoService pagoService;

    @GetMapping
    public List<PagoResponse> listar(
            @RequestParam(value = "clienteId", required = false) Long clienteId,
            @RequestParam(value = "tecnicoId", required = false) Long tecnicoId,
            @RequestParam(value = "estado", required = false) String estado
    ) {
        return pagoService.listar(clienteId, tecnicoId, estado);
    }

    @PostMapping
    public ResponseEntity<PagoResponse> crear(@Valid @RequestBody CrearPagoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.crear(request));
    }

    @GetMapping("/{id}")
    public PagoResponse obtener(@PathVariable Long id) {
        return pagoService.obtener(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> existe(@PathVariable Long id) {
        return pagoService.existe(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public PagoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarPagoRequest request) {
        return pagoService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public PagoResponse actualizarEstado(@PathVariable Long id, @Valid @RequestBody ActualizarEstadoPagoRequest request) {
        return pagoService.actualizarEstado(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/solicitud/{solicitudId}")
    public List<PagoResponse> porSolicitud(@PathVariable Long solicitudId) {
        return pagoService.listarPorSolicitud(solicitudId);
    }

    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> opcionesColeccion() {
        return opciones(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.POST, HttpMethod.OPTIONS);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> opcionesRecurso() {
        return opciones(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE, HttpMethod.OPTIONS);
    }

    private ResponseEntity<Void> opciones(HttpMethod... metodos) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAllow(Set.of(metodos));
        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }
}
