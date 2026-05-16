package com.upeu.payment.client;

import com.upeu.payment.dto.external.SolicitudResponse;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class MsServiceRequestFallbackFactory implements FallbackFactory<MsServiceRequestClient> {
    @Override
    public MsServiceRequestClient create(Throwable cause) {
        return new MsServiceRequestClient() {
            @Override
            public SolicitudResponse obtenerSolicitud(Long id) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "MS4 no disponible temporalmente. Circuit breaker activo.",
                        cause
                );
            }
        };
    }
}
