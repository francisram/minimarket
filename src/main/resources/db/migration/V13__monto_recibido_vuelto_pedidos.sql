-- =============================================================================
-- Migración V13: Soporte para monto recibido y vuelto en pedidos en efectivo
-- =============================================================================

ALTER TABLE pedidos
ADD COLUMN IF NOT EXISTS monto_recibido NUMERIC(12, 0),
ADD COLUMN IF NOT EXISTS vuelto NUMERIC(12, 0);

COMMENT ON COLUMN pedidos.monto_recibido IS 'Importe entregado en efectivo por el cliente en Guaraníes';
COMMENT ON COLUMN pedidos.vuelto IS 'Cambio o vuelto devuelto al cliente en Guaraníes';
