package com.upeu.payment.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MsNotificationFallbackFactory implements FallbackFactory<MsNotificationClient> {
    private static final Logger log = LoggerFactory.getLogger(MsNotificationFallbackFactory.class);

    @Override
    public MsNotificationClient create(Throwable cause) {
        return new MsNotificationClient() {
            @Override
            public void crearNotificacion(Map<String, Object> request) {
                log.warn("Circuit breaker activo para ms-notification. Se omite notificacion de pago.", cause);
            }
        };
    }
}
