-- Sesiones de autenticación: el token de acceso (corto) lleva el id de la sesión y el de renovación se guarda hasheado.
-- Revocar una sesión (logout, cambio de clave, cierre por administrador) invalida sus tokens de inmediato.
CREATE TABLE sesiones_auth (
    id varchar(36) PRIMARY KEY,
    usuario_id bigint NOT NULL REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    refresh_hash varchar(64) NOT NULL,
    refresh_hash_anterior varchar(64),
    rotado_en timestamp(6),
    creada_en timestamp(6) NOT NULL,
    expira_en timestamp(6) NOT NULL,
    revocada_en timestamp(6),
    dispositivo varchar(200)
);
CREATE INDEX idx_sesiones_auth_usuario ON sesiones_auth (usuario_id);
CREATE INDEX idx_sesiones_auth_refresh ON sesiones_auth (refresh_hash);
CREATE INDEX idx_sesiones_auth_refresh_ant ON sesiones_auth (refresh_hash_anterior);
