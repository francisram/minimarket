-- Contadores del limitador de intentos fallidos (login, clave de supervisor al anular):
-- en la base sobreviven a un reinicio y los comparten varias instancias.
CREATE TABLE intentos_fallidos (
    clave varchar(150) PRIMARY KEY,
    fallos integer NOT NULL,
    primer_fallo timestamp(6),
    bloqueado_hasta timestamp(6)
);
