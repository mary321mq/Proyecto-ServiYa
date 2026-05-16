package com.upeu.servicerequest.repository;

import com.upeu.servicerequest.entity.SolicitudServicio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudRepository extends JpaRepository<SolicitudServicio, Long> {
}
