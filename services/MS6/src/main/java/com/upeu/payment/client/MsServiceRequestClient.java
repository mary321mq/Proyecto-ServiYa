package com.upeu.payment.client;

import com.upeu.payment.dto.external.SolicitudResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-service-request", path = "/api/v1", fallbackFactory = MsServiceRequestFallbackFactory.class)
public interface MsServiceRequestClient {
    @GetMapping("/solicitudes/{id}")
    SolicitudResponse obtenerSolicitud(@PathVariable("id") Long id);
}
