package com.upeu.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "ms-notification")
public interface MsNotificationClient {
    @PostMapping("/api/v1/notificaciones")
    void crearNotificacion(@RequestBody Map<String, Object> request);
}
