package com.upeu.payment.repository;

import com.upeu.payment.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findBySolicitudId(Long solicitudId);

    List<Pago> findByClienteId(Long clienteId);

    List<Pago> findByTecnicoId(Long tecnicoId);

    List<Pago> findByEstadoIgnoreCase(String estado);
}
