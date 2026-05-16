package com.upeu.notification.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotificacionNotFoundException extends RuntimeException {
    public NotificacionNotFoundException(String message) {
        super(message);
    }
}

