-- Control anti-retroceso del reloj del servidor (fila única, id = 1)
CREATE TABLE control_reloj (
    id bigint PRIMARY KEY,
    ultima_fecha_vista timestamp(6),
    retroceso_detectado boolean NOT NULL,
    fecha_retroceso timestamp(6),
    referencia_retroceso timestamp(6),
    desfase_minutos bigint
);
