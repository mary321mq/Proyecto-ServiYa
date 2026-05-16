package com.upeu.payment.service;

import com.upeu.payment.client.MsNotificationClient;
import com.upeu.payment.client.MsServiceRequestClient;
import com.upeu.payment.dto.external.SolicitudResponse;
import com.upeu.payment.dto.request.ActualizarEstadoPagoRequest;
import com.upeu.payment.dto.request.ActualizarPagoRequest;
import com.upeu.payment.dto.request.CrearPagoRequest;
import com.upeu.payment.dto.response.PagoResponse;
import com.upeu.payment.entity.Pago;
import com.upeu.payment.exception.PagoNotFoundException;
import com.upeu.payment.mapper.PagoMapper;
import com.upeu.payment.repository.PagoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class PagoService {
    private final PagoRepository pagoRepository;
    private final BigDecimal comisionPorcentaje;
    private final PagoMapper pagoMapper;
    private final MsServiceRequestClient msServiceRequestClient;
    private final MsNotificationClient msNotificationClient;

    public PagoService(
            PagoRepository pagoRepository,
            @Value("${serviya.comision.porcentaje:${SERVIYA_COMISION_PORCENTAJE:0.10}}") BigDecimal comisionPorcentaje,
            PagoMapper pagoMapper,
            MsServiceRequestClient msServiceRequestClient,
            MsNotificationClient msNotificationClient
    ) {
        this.pagoRepository = pagoRepository;
        this.comisionPorcentaje = comisionPorcentaje;
        this.pagoMapper = pagoMapper;
        this.msServiceRequestClient = msServiceRequestClient;
        this.msNotificationClient = msNotificationClient;
    }

    @Transactional
    public PagoResponse crear(@Valid CrearPagoRequest request) {
        validarConsistenciaSolicitud(request.getSolicitudId(), request.getClienteId(), request.getTecnicoId());

        Pago pago = Pago.builder()
                .createdAt(Instant.now())
                .updatedAt(null)
                .build();
        aplicarDatosPago(
                pago,
                request.getSolicitudId(),
                request.getClienteId(),
                request.getTecnicoId(),
                request.getMonto(),
                request.getMetodo(),
                "PAGADO"
        );
        pago.setUpdatedAt(null);

        Pago saved = pagoRepository.save(pago);
        enviarNotificacionPago(saved);
        return pagoMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listar(Long clienteId, Long tecnicoId, String estado) {
        List<Pago> pagos;
        if (clienteId != null) {
            pagos = pagoRepository.findByClienteId(clienteId);
        } else if (tecnicoId != null) {
            pagos = pagoRepository.findByTecnicoId(tecnicoId);
        } else if (estado != null && !estado.isBlank()) {
            pagos = pagoRepository.findByEstadoIgnoreCase(estado);
        } else {
            pagos = pagoRepository.findAll();
        }

        return pagos.stream().map(pagoMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PagoResponse obtener(Long id) {
        return pagoRepository.findById(id)
                .map(pagoMapper::toResponse)
                .orElseThrow(() -> new PagoNotFoundException("Pago no encontrado"));
    }

    @Transactional(readOnly = true)
    public boolean existe(Long id) {
        return pagoRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorSolicitud(Long solicitudId) {
        return pagoRepository.findBySolicitudId(solicitudId).stream().map(pagoMapper::toResponse).toList();
    }

    @Transactional
    public PagoResponse actualizar(Long id, @Valid ActualizarPagoRequest request) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new PagoNotFoundException("Pago no encontrado"));

        validarConsistenciaSolicitud(request.getSolicitudId(), request.getClienteId(), request.getTecnicoId());
        aplicarDatosPago(
                pago,
                request.getSolicitudId(),
                request.getClienteId(),
                request.getTecnicoId(),
                request.getMonto(),
                request.getMetodo(),
                request.getEstado()
        );

        return pagoMapper.toResponse(pagoRepository.save(pago));
    }

    @Transactional
    public PagoResponse actualizarEstado(Long id, @Valid ActualizarEstadoPagoRequest request) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new PagoNotFoundException("Pago no encontrado"));
        pago.setEstado(request.getEstado());
        pago.setUpdatedAt(Instant.now());
        return pagoMapper.toResponse(pagoRepository.save(pago));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!pagoRepository.existsById(id)) {
            throw new PagoNotFoundException("Pago no encontrado");
        }
        pagoRepository.deleteById(id);
    }

    private void validarConsistenciaSolicitud(Long solicitudId, Long clienteId, Long tecnicoId) {
        SolicitudResponse solicitud;
        try {
            solicitud = msServiceRequestClient.obtenerSolicitud(solicitudId);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo validar la solicitud en MS4");
        }

        if (solicitud == null || solicitud.getId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada");
        }
        if (!clienteId.equals(solicitud.getClienteId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cliente no coincide con la solicitud");
        }
        if (solicitud.getTecnicoAsignadoId() == null || !tecnicoId.equals(solicitud.getTecnicoAsignadoId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tecnico no coincide con la asignacion de la solicitud");
        }
    }

    private void aplicarDatosPago(
            Pago pago,
            Long solicitudId,
            Long clienteId,
            Long tecnicoId,
            BigDecimal montoRequest,
            String metodo,
            String estado
    ) {
        BigDecimal monto = montoRequest.setScale(2, RoundingMode.HALF_UP);
        BigDecimal comision = monto.multiply(comisionPorcentaje).setScale(2, RoundingMode.HALF_UP);
        BigDecimal neto = monto.subtract(comision).setScale(2, RoundingMode.HALF_UP);

        pago.setSolicitudId(solicitudId);
        pago.setClienteId(clienteId);
        pago.setTecnicoId(tecnicoId);
        pago.setMonto(monto);
        pago.setComision(comision);
        pago.setNeto(neto);
        pago.setMetodo(metodo);
        pago.setEstado(estado);
        pago.setUpdatedAt(Instant.now());
    }

    private void enviarNotificacionPago(Pago pago) {
        try {
            msNotificationClient.crearNotificacion(Map.of(
                    "destinatario", "cliente-" + pago.getClienteId(),
                    "canal", "APP",
                    "titulo", "Pago registrado",
                    "mensaje", "Se registro el pago de la solicitud " + pago.getSolicitudId()
            ));
        } catch (Exception ignored) {
            // No romper el flujo de pago por una caida temporal de MS8.
        }
    }
}
