package com.upeu.review.client;

import com.upeu.review.dto.request.ActualizarRankingRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-technician", path = "/api/v1")
public interface MsTechnicianClient {
    @PutMapping("/tecnicos/{id}/ranking")
    void actualizarRanking(@PathVariable("id") Long tecnicoId, @RequestBody ActualizarRankingRequest request);
}

