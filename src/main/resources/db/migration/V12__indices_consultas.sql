-- Índices para las consultas que crecen con las ventas: reportes, monitor de pedidos, libro IVA, caja y rankings.
-- (PostgreSQL no indexa solo las claves foráneas ni las columnas de fecha/estado.)
CREATE INDEX IF NOT EXISTS idx_pedidos_fecha ON pedidos (fecha_creacion);
CREATE INDEX IF NOT EXISTS idx_pedidos_estado_fecha ON pedidos (estado, fecha_creacion);
CREATE INDEX IF NOT EXISTS idx_pedidos_sesion_caja ON pedidos (sesion_caja_id);
CREATE INDEX IF NOT EXISTS idx_pedidos_punto_expedicion ON pedidos (punto_expedicion_id);
CREATE INDEX IF NOT EXISTS idx_pedidos_numero_factura ON pedidos (numero_factura) WHERE numero_factura IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_detalles_pedido_pedido ON detalles_pedido (pedido_id);
CREATE INDEX IF NOT EXISTS idx_detalles_pedido_producto_simple ON detalles_pedido (producto_simple_id);
CREATE INDEX IF NOT EXISTS idx_detalles_pedido_presentacion ON detalles_pedido (presentacion_id);
CREATE INDEX IF NOT EXISTS idx_detalle_pedido_sabores_detalle ON detalle_pedido_sabores (detalle_id);

CREATE INDEX IF NOT EXISTS idx_sesiones_caja_estado ON sesiones_caja (estado);
CREATE INDEX IF NOT EXISTS idx_sesiones_caja_apertura ON sesiones_caja (fecha_apertura);
