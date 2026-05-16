package com.upeu.technician.client;

import com.upeu.technician.dto.SolicitudDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-service-request", path = "/api/v1", fallbackFactory = MsServiceRequestFallbackFactory.class)
public interface MsServiceRequestClient {

    @GetMapping("/solicitudes/{id}")
    SolicitudDto obtenerSolicitud(@PathVariable("id") Long id);
}
