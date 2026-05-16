CREATE TABLE solicitudes_servicio (
  id BIGINT NOT NULL AUTO_INCREMENT,
  cliente_id BIGINT NOT NULL,
  descripcion VARCHAR(500) NOT NULL,
  lat DOUBLE NOT NULL,
  lng DOUBLE NOT NULL,
  estado VARCHAR(30) NOT NULL,
  tecnico_asignado_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_solicitud_estado (estado),
  INDEX idx_solicitud_tecnico (tecnico_asignado_id),
  INDEX idx_solicitud_cliente (cliente_id)
);
