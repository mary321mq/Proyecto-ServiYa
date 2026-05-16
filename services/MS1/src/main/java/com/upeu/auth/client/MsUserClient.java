package com.upeu.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "ms-user")
public interface MsUserClient {
    @PostMapping("/api/v1/clientes")
    void crearCliente(@RequestBody Map<String, Object> request);
}
