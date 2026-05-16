package com.upeu.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "ms-notification", fallbackFactory = MsNotificationFallbackFactory.class)
public interface MsNotificationClient {
    @PostMapping("/api/v1/notificaciones")
    void crearNotificacion(@RequestBody Map<String, Object> request);
}
