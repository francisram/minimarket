-- =============================================================================
-- SCRIPT DE LIMPIEZA DE DATOS TEMPORALES / DE PRUEBA
-- Restablece transacciones y catálogos de prueba sin eliminar usuarios ni roles
-- =============================================================================

BEGIN;

-- 1. Eliminar transacciones de pedidos y notas de crédito
DELETE FROM detalle_pedido_sabores;
DELETE FROM detalle_pedido_toppings;
DELETE FROM detalles_pedido;
DELETE FROM pedidos;
DELETE FROM detalles_nota_credito;
DELETE FROM notas_credito;
DELETE FROM series_nota_credito;
DELETE FROM trabajos_impresion;

-- 2. Eliminar compras e inventarios
DELETE FROM detalles_compra;
DELETE FROM compras;
DELETE FROM movimientos_stock;
DELETE FROM detalles_toma_inventario;
DELETE FROM tomas_inventario;

-- 3. Movimientos de caja y reinicio de sesión de caja activa
DELETE FROM movimientos_caja;
UPDATE sesiones_caja 
SET cantidad_pedidos = 0,
    total_ventas_efectivo = 0,
    total_ventas_tarjeta_debito = 0,
    total_ventas_tarjeta_credito = 0,
    total_ventas_transferencia_qr = 0,
    total_ventas_general = 0,
    total_ingresos_caja = 0,
    total_egresos_caja = 0,
    monto_esperado_efectivo = monto_inicial;

-- 4. Catálogos de prueba
DELETE FROM producto_proveedores;
DELETE FROM productos;
DELETE FROM productos_simples;
DELETE FROM toppings;
DELETE FROM sabores;
DELETE FROM presentaciones;
DELETE FROM proveedores;
DELETE FROM clientes WHERE id > 1; -- Conserva el cliente 'Consumidor Final'

-- 5. Restablecer correlativos de facturación fiscal
UPDATE institucion SET ultimo_numero_factura = 0 WHERE id = 1;
UPDATE puntos_expedicion SET ultimo_numero_factura = 0 WHERE id = 1;
UPDATE sesiones_caja SET punto_expedicion_id = 1 WHERE punto_expedicion_id > 1;
DELETE FROM puntos_expedicion WHERE id > 1;

-- 6. Reiniciar secuencias
SELECT setval('clientes_id_seq', (SELECT COALESCE(MAX(id), 1) FROM clientes));
SELECT setval('proveedores_id_seq', 1, false);
SELECT setval('presentaciones_id_seq', 1, false);
SELECT setval('sabores_id_seq', 1, false);
SELECT setval('toppings_id_seq', 1, false);
SELECT setval('productos_simples_id_seq', 1, false);
SELECT setval('productos_id_seq', 1, false);
SELECT setval('compras_id_seq', 1, false);
SELECT setval('detalles_compra_id_seq', 1, false);
SELECT setval('pedidos_id_seq', 1, false);
SELECT setval('detalles_pedido_id_seq', 1, false);
SELECT setval('puntos_expedicion_id_seq', 1, true);

COMMIT;
