CREATE TABLE notificaciones (
  id BIGINT NOT NULL AUTO_INCREMENT,
  destinatario VARCHAR(255) NOT NULL,
  canal VARCHAR(30) NOT NULL,
  titulo VARCHAR(150) NOT NULL,
  mensaje VARCHAR(500) NOT NULL,
  estado VARCHAR(30) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_notif_destinatario (destinatario),
  INDEX idx_notif_estado (estado)
);
