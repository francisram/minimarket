-- El timbrado con el que se emitió cada factura queda en el propio pedido: el del punto de
-- expedición cambia al renovar el talonario y no sirve para reconstruir facturas viejas.
ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS timbrado character varying(30);

UPDATE pedidos p
   SET timbrado = pe.timbrado
  FROM puntos_expedicion pe
 WHERE p.punto_expedicion_id = pe.id
   AND p.numero_factura IS NOT NULL
   AND p.timbrado IS NULL;

-- Defensa en la base: un mismo número de factura no puede repetirse dentro de un timbrado.
-- COALESCE para que los pedidos viejos sin timbrado conocido también queden protegidos.
CREATE UNIQUE INDEX IF NOT EXISTS uk_pedidos_timbrado_factura
    ON pedidos (COALESCE(timbrado, ''), numero_factura)
    WHERE numero_factura IS NOT NULL;
