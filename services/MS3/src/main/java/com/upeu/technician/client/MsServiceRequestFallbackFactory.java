package com.upeu.technician.client;

import com.upeu.technician.dto.SolicitudDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MsServiceRequestFallbackFactory implements FallbackFactory<MsServiceRequestClient> {
    @Override
    public MsServiceRequestClient create(Throwable cause) {
        return new MsServiceRequestClient() {
            @Override
            public SolicitudDto obtenerSolicitud(Long id) {
                log.warn("Circuit breaker activo para ms-service-request. solicitudId={}", id, cause);
                return null;
            }
        };
    }
}
