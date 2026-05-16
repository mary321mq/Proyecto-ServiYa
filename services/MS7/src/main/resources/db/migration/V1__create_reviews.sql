CREATE TABLE reviews (
  id BIGINT NOT NULL AUTO_INCREMENT,
  solicitud_id BIGINT NOT NULL,
  cliente_id BIGINT NOT NULL,
  tecnico_id BIGINT NOT NULL,
  puntuacion INT NOT NULL,
  comentario VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_review_tecnico (tecnico_id),
  INDEX idx_review_solicitud (solicitud_id)
);
