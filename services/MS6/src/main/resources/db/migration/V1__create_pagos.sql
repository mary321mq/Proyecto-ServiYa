CREATE TABLE pagos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  solicitud_id BIGINT NOT NULL,
  cliente_id BIGINT NOT NULL,
  tecnico_id BIGINT NOT NULL,
  monto DECIMAL(10,2) NOT NULL,
  comision DECIMAL(10,2) NOT NULL,
  neto DECIMAL(10,2) NOT NULL,
  metodo VARCHAR(30) NOT NULL,
  estado VARCHAR(30) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_pago_solicitud (solicitud_id),
  INDEX idx_pago_tecnico (tecnico_id),
  INDEX idx_pago_estado (estado)
);
