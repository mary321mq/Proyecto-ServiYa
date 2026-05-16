package com.upeu.payment.mapper;

import com.upeu.payment.dto.response.PagoResponse;
import com.upeu.payment.entity.Pago;
import org.springframework.stereotype.Component;

@Component
public class PagoMapper {
    public PagoResponse toResponse(Pago p) {
        return PagoResponse.builder()
                .id(p.getId())
                .solicitudId(p.getSolicitudId())
                .clienteId(p.getClienteId())
                .tecnicoId(p.getTecnicoId())
                .monto(p.getMonto())
                .comision(p.getComision())
                .neto(p.getNeto())
                .metodo(p.getMetodo())
                .estado(p.getEstado())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}

