-- =============================================================================
-- SCRIPT DE CARGA DE DATOS TEMPORALES / DEMOSTRACIÓN PARA PRUEBAS (ERP & POS)
-- Contexto: Heladería & Minimarket (Paraguay - Gs. / DNIT Módulo 11)
-- =============================================================================

BEGIN;

-- 1. DATOS FISCALES DEL NEGOCIO (INSTITUCIÓN)
UPDATE institucion
SET nombre = 'Heladería & Minimarket Delicias',
    ruc = '80099887-1',
    timbrado = '17894561',
    timbrado_inicio = CURRENT_DATE - INTERVAL '1 month',
    timbrado_vencimiento = CURRENT_DATE + INTERVAL '1 year',
    numero_inicio_factura = 1,
    numero_fin_factura = 1000,
    ultimo_numero_factura = 1,
    establecimiento = '001',
    punto_emision = '001',
    direccion = 'Av. Mariscal López 1234 c/ San Martín',
    telefono = '(021) 600-123 / (0981) 123-456',
    ciudad = 'Asunción'
WHERE id = 1;

-- 2. PUNTOS DE EXPEDICIÓN FISCAL (DNIT SET)
UPDATE puntos_expedicion
SET timbrado = '17894561',
    timbrado_inicio = CURRENT_DATE - INTERVAL '1 month',
    timbrado_vencimiento = CURRENT_DATE + INTERVAL '1 year',
    numero_inicio_factura = 1,
    numero_fin_factura = 1000,
    ultimo_numero_factura = 1,
    activo = true
WHERE id = 1;

INSERT INTO puntos_expedicion (id, nombre, establecimiento, punto_emision, timbrado, timbrado_inicio, timbrado_vencimiento, numero_inicio_factura, numero_fin_factura, ultimo_numero_factura, activo)
VALUES (2, 'Caja 2 - Take Away / Salón', '001', '002', '17894561', CURRENT_DATE - INTERVAL '1 month', CURRENT_DATE + INTERVAL '1 year', 1, 1000, 0, true)
ON CONFLICT (id) DO UPDATE 
SET timbrado = EXCLUDED.timbrado,
    timbrado_vencimiento = EXCLUDED.timbrado_vencimiento,
    activo = true;

-- 3. PROVEEDORES
INSERT INTO proveedores (id, razon_social, ruc, contacto, telefono, email, direccion, activo, fecha_creacion)
VALUES 
  (1, 'Lácteos del Valle S.A.', '80098765-4', 'Carlos Benítez (Ventas)', '021-987654', 'ventas@lacteosdelvalle.com.py', 'Km 14 Ruta 2, San Lorenzo', true, NOW()),
  (2, 'Distribuidora Bebidas del Sur', '80054321-9', 'Mariela Bogado', '021-456789', 'pedidos@bebidassur.com.py', 'Av. Fernando de la Mora 890, Asunción', true, NOW()),
  (3, 'Envases & Descartables Paraguay', '80033221-5', 'Jorge Martínez', '021-223344', 'ventas@envasespy.com.py', 'Mariano Roque Alonso', true, NOW())
ON CONFLICT (ruc) DO UPDATE 
SET razon_social = EXCLUDED.razon_social,
    contacto = EXCLUDED.contacto,
    telefono = EXCLUDED.telefono,
    email = EXCLUDED.email,
    activo = true;

-- 4. CLIENTES (CON RUC OFICIAL MÓDULO 11)
INSERT INTO clientes (id, razon_social, ruc, telefono, email, direccion, activo, fecha_creacion)
VALUES 
  (1, 'Consumidor Final', '4444444-1', '000-000', 'sin_email@pos.com', 'Asunción', true, NOW())
ON CONFLICT (ruc) DO NOTHING;

INSERT INTO clientes (razon_social, ruc, telefono, email, direccion, activo, fecha_creacion)
VALUES 
  ('María Elena Gómez Ortiz', '3456789-5', '0981-123456', 'maria.gomez@gmail.com', 'Barrio Herrera, Asunción', true, NOW()),
  ('Carlos Alberto Benítez', '1234567-9', '0971-654321', 'carlos.benitez@hotmail.com', 'San Lorenzo', true, NOW()),
  ('Distribuidora Central S.R.L.', '80012345-0', '021-555123', 'contacto@central.com.py', 'Av. Eusebio Ayala 1230', true, NOW()),
  ('Heladerías y Postres del Este S.A.', '80023456-1', '061-512345', 'info@postresdeleste.com.py', 'Ciudad del Este', true, NOW())
ON CONFLICT (ruc) DO UPDATE 
SET razon_social = EXCLUDED.razon_social,
    telefono = EXCLUDED.telefono,
    direccion = EXCLUDED.direccion,
    activo = true;

-- 5. PRESENTACIONES / FORMATOS DE HELADO
INSERT INTO presentaciones (id, nombre, precio, max_sabores, peso_gramos_aprox, stock, stock_minimo, activo, tipo_iva)
VALUES
  (1, 'Cucurucho 1 Bocha', 12000, 1, 100, 50, 10, true, 'IVA_10'),
  (2, 'Cucurucho 2 Bochas', 18000, 2, 180, 50, 10, true, 'IVA_10'),
  (3, 'Vaso Mediano', 18000, 2, 150, 40, 10, true, 'IVA_10'),
  (4, 'Copa Helada Especial', 28000, 3, 300, 25, 5, true, 'IVA_10'),
  (5, 'Pote 1/4 Kg', 25000, 3, 250, 30, 10, true, 'IVA_10'),
  (6, 'Pote 1/2 Kg', 45000, 3, 500, 30, 10, true, 'IVA_10'),
  (7, 'Pote 1 Kg', 80000, 4, 1000, 30, 10, true, 'IVA_10')
ON CONFLICT (nombre) DO UPDATE 
SET precio = EXCLUDED.precio,
    max_sabores = EXCLUDED.max_sabores,
    stock = EXCLUDED.stock,
    activo = true;

-- 6. SABORES DE HELADO
INSERT INTO sabores (id, nombre, descripcion, categoria, apto_celiaco, es_vegano, sin_azucar, disponible, stock_kilos, stock_minimo_kilos)
VALUES
  (1, 'Dulce de Leche Granizado', 'Dulce de leche tradicional con finas escamas de chocolate amargo', 'DULCE_DE_LECHE', true, false, false, true, 12.5, 2.0),
  (2, 'Dulce de Leche Clásico', 'El auténtico sabor tradicional argentino', 'DULCE_DE_LECHE', true, false, false, true, 10.0, 2.0),
  (3, 'Chocolate Suizo', 'Chocolate intenso con dulce de leche natural y nueces', 'CHOCOLATE', false, false, false, true, 14.0, 2.0),
  (4, 'Chocolate Amargo 70%', 'Puro cacao amargo al agua, apto vegano', 'CHOCOLATE', true, true, false, true, 8.5, 2.0),
  (5, 'Frutilla al Agua', 'Frutillas frescas seleccionadas, base agua', 'FRUTAL', true, true, false, true, 15.0, 2.0),
  (6, 'Limón al Agua', 'Zumo natural de limones frescos, refrescante', 'FRUTAL', true, true, false, true, 11.0, 2.0),
  (7, 'Maracuyá Silvestre', 'Pulpa de maracuyá natural con semillas', 'FRUTAL', true, true, false, true, 9.0, 2.0),
  (8, 'Sambayón Italiano', 'Crema con yemas de huevo frescas y vino oporto', 'CREMA', true, false, false, true, 10.0, 2.0),
  (9, 'Tramontana', 'Crema chantilly, dulce de leche natural y galletitas crocantes', 'CREMA', false, false, false, true, 13.0, 2.0),
  (10, 'Frutos del Bosque', 'Crema americana veteada con salsa de frambuesas y moras', 'CREMA', true, false, false, true, 10.5, 2.0),
  (11, 'Vainilla Diet (Stevia)', 'Vainilla natural sin azúcar agregada endulzada con stevia', 'CREMA', true, false, true, true, 7.0, 2.0),
  (12, 'Menta Granizada', 'Menta fresca con chispas de chocolate semiamargo', 'CREMA', true, false, false, true, 8.0, 2.0)
ON CONFLICT (nombre) DO UPDATE 
SET disponible = true,
    stock_kilos = EXCLUDED.stock_kilos;

-- 7. TOPPINGS Y SALSAS
INSERT INTO toppings (id, nombre, precio_extra, disponible)
VALUES
  (1, 'Baño de Chocolate', 3000, true),
  (2, 'Salsa de Caramelo', 3000, true),
  (3, 'Almendras Tostadas', 3000, true),
  (4, 'Cucurucho Extra', 3000, true),
  (5, 'Chispas de Colores', 2000, true),
  (6, 'Galletitas Oreo Molidas', 4000, true)
ON CONFLICT (nombre) DO UPDATE 
SET precio_extra = EXCLUDED.precio_extra,
    disponible = true;

-- 8. PRODUCTOS SIMPLES (VENTA DIRECTA / BEBIDAS / ENVASADOS)
INSERT INTO productos_simples (id, nombre, descripcion, precio, categoria, stock, stock_minimo, activo, tipo_iva, codigo, codigo_barra, proveedor_id)
VALUES
  (1, 'Agua Mineral sin gas 500ml', 'Agua mineral natural de manantial sin gas', 10000, 'BEBIDAS', 50, 5, true, 'IVA_10', 'P-1', '7840001002', 2),
  (2, 'Agua Mineral con gas 500ml', 'Agua mineral natural carbonatada', 10000, 'BEBIDAS', 40, 5, true, 'IVA_10', 'P-2', '7840001001', 2),
  (3, 'Coca Cola 500ml', 'Gaseosa Coca Cola botella personal 500ml', 12000, 'BEBIDAS', 48, 10, true, 'IVA_10', 'P-3', '7840001005', 2),
  (4, 'Sprite 500ml', 'Gaseosa lima limón botella 500ml', 12000, 'BEBIDAS', 36, 10, true, 'IVA_10', 'P-4', '7840001006', 2),
  (5, 'Alfajor Helado Artesanal', 'Relleno de dulce de leche con baño de chocolate', 15000, 'ENVASADOS', 25, 5, true, 'IVA_10', 'P-5', '7840001003', 1),
  (6, 'Paleta Bombón Artesanal', 'Helado de crema americana bañado en chocolate con almendras', 12000, 'ENVASADOS', 30, 5, true, 'IVA_10', 'P-6', '7840001004', 1),
  (7, 'Papas Fritas Lays 85g', 'Papas fritas clásicas saladas', 14000, 'GOLOSINAS', 20, 5, true, 'IVA_10', 'P-7', '7840001007', 2)
ON CONFLICT (nombre) DO UPDATE 
SET precio = EXCLUDED.precio,
    stock = EXCLUDED.stock,
    activo = true;

-- 9. CATÁLOGO MAESTRO DE PRODUCTOS (RETAIL ERP)
-- Presentaciones como ENVASE
INSERT INTO productos (nombre, tipo_articulo, precio_venta, precio_costo, tipo_iva, stock, stock_minimo, unidad_medida, max_sabores, peso_gramos_aprox, activo, fecha_creacion, sku)
SELECT p.nombre, 'ENVASE', p.precio, ROUND(p.precio * 0.15), 'IVA_10', p.stock, p.stock_minimo, 'UNIDAD', p.max_sabores, p.peso_gramos_aprox, true, NOW(), 'ENV-' || p.id
FROM presentaciones p
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = p.nombre);

-- Sabores
INSERT INTO productos (nombre, descripcion, tipo_articulo, categoria_sabor, precio_venta, precio_costo, tipo_iva, stock, stock_minimo, unidad_medida, apto_celiaco, es_vegano, sin_azucar, activo, fecha_creacion, sku)
SELECT s.nombre, s.descripcion, 'SABOR', s.categoria, 0, 35000, 'IVA_10', s.stock_kilos, s.stock_minimo_kilos, 'KG', s.apto_celiaco, s.es_vegano, s.sin_azucar, true, NOW(), 'SAB-' || s.id
FROM sabores s
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = s.nombre);

-- Toppings
INSERT INTO productos (nombre, tipo_articulo, precio_venta, precio_costo, tipo_iva, stock, stock_minimo, unidad_medida, activo, fecha_creacion, sku)
SELECT t.nombre, 'TOPPING', t.precio_extra, ROUND(t.precio_extra * 0.25), 'IVA_10', 100, 10, 'UNIDAD', true, NOW(), 'TOP-' || t.id
FROM toppings t
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = t.nombre);

-- Productos Simples
INSERT INTO productos (nombre, descripcion, tipo_articulo, categoria, precio_venta, precio_costo, tipo_iva, stock, stock_minimo, unidad_medida, codigo_barra, sku, activo, fecha_creacion)
SELECT ps.nombre, ps.descripcion, 'VENTA_DIRECTA', ps.categoria, ps.precio, ROUND(ps.precio * 0.60), 'IVA_10', ps.stock, ps.stock_minimo, 'UNIDAD', ps.codigo_barra, ps.codigo, true, NOW()
FROM productos_simples ps
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = ps.nombre);

-- Asociaciones Producto-Proveedor
INSERT INTO producto_proveedores (producto_id, proveedor_id, costo_compra, es_principal, fecha_asociacion, codigo_referencia)
SELECT p.id, 2, p.precio_costo, true, NOW(), 'REF-' || p.id
FROM productos p
WHERE p.tipo_articulo = 'VENTA_DIRECTA'
  AND NOT EXISTS (SELECT 1 FROM producto_proveedores WHERE producto_id = p.id AND proveedor_id = 2);

INSERT INTO producto_proveedores (producto_id, proveedor_id, costo_compra, es_principal, fecha_asociacion, codigo_referencia)
SELECT p.id, 1, p.precio_costo, true, NOW(), 'REF-' || p.id
FROM productos p
WHERE p.tipo_articulo = 'SABOR'
  AND NOT EXISTS (SELECT 1 FROM producto_proveedores WHERE producto_id = p.id AND proveedor_id = 1);

-- 10. COMPRA DE PRUEBA
INSERT INTO compras (id, proveedor_id, numero_factura, timbrado, fecha_emision, fecha_creacion, condicion_compra, total, total_iva10, estado, usuario, observacion)
VALUES (1, 1, '001-001-0004589', '14892034', CURRENT_DATE - INTERVAL '2 days', NOW(), 'CONTADO', 350000, 31818, 'REGISTRADA', 'admin', 'Reposición semanal de materia prima láctea')
ON CONFLICT (id) DO NOTHING;

INSERT INTO detalles_compra (id, compra_id, producto_id, cantidad, costo_unitario, subtotal, tipo_iva, monto_iva)
SELECT 1, 1, p.id, 10, 35000, 350000, 'IVA_10', 31818
FROM productos p
WHERE p.nombre = 'Dulce de Leche Granizado'
LIMIT 1
ON CONFLICT (id) DO NOTHING;

-- 11. VENTAS / PEDIDOS DE PRUEBA EN SESIÓN DE CAJA ACTUAL (id: 1)
-- Venta 1: TICKET - Efectivo (con vuelto calculado)
INSERT INTO pedidos (
  id, sesion_caja_id, punto_expedicion_id, fecha_creacion, cliente_nombre,
  tipo_comprobante, condicion_venta, cliente_ruc, cliente_direccion,
  metodo_pago, tipo_entrega, estado,
  total, total_gravada10, total_gravada5, total_exentas, total_iva10, total_iva5, total_iva,
  monto_recibido, vuelto, notas
) VALUES (
  1, 1, 1, NOW() - INTERVAL '3 hours', 'Consumidor Final',
  'TICKET', 'CONTADO', '4444444-1', 'Asunción',
  'EFECTIVO', 'MOSTRADOR', 'ENTREGADO',
  21000, 19091, 0, 0, 1909, 0, 1909,
  50000, 29000, 'Cliente pagó con Gs. 50.000 (Vuelto: Gs. 29.000)'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO detalles_pedido (id, pedido_id, tipo_item, presentacion_id, cantidad, precio_unitario, subtotal, tipo_iva)
VALUES (1, 1, 'HELADO', 2, 1, 18000, 18000, 'IVA_10')
ON CONFLICT (id) DO NOTHING;

INSERT INTO detalle_pedido_sabores (detalle_id, sabor_id)
VALUES (1, 1), (1, 3)
ON CONFLICT DO NOTHING;

INSERT INTO detalles_pedido (id, pedido_id, tipo_item, presentacion_id, cantidad, precio_unitario, subtotal, tipo_iva)
VALUES (2, 1, 'TOPPING', NULL, 1, 3000, 3000, 'IVA_10')
ON CONFLICT (id) DO NOTHING;

INSERT INTO detalle_pedido_toppings (detalle_id, topping_id)
VALUES (2, 1)
ON CONFLICT DO NOTHING;

-- Venta 2: FACTURA LEGAL - Efectivo (María Gómez)
INSERT INTO pedidos (
  id, sesion_caja_id, punto_expedicion_id, fecha_creacion, cliente_nombre,
  tipo_comprobante, condicion_venta, cliente_ruc, cliente_direccion, numero_factura, timbrado, timbrado_vencimiento,
  metodo_pago, tipo_entrega, estado,
  total, total_gravada10, total_gravada5, total_exentas, total_iva10, total_iva5, total_iva,
  monto_recibido, vuelto, notas
) VALUES (
  2, 1, 1, NOW() - INTERVAL '2 hours', 'María Elena Gómez Ortiz',
  'FACTURA', 'CONTADO', '3456789-5', 'Barrio Herrera, Asunción', '001-001-0000001', '17894561', CURRENT_DATE + INTERVAL '1 year',
  'EFECTIVO', 'MOSTRADOR', 'ENTREGADO',
  100000, 90909, 0, 0, 9091, 0, 9091,
  100000, 0, 'Pago exacto con billete de Gs. 100.000'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO detalles_pedido (id, pedido_id, tipo_item, presentacion_id, cantidad, precio_unitario, subtotal, tipo_iva)
VALUES (3, 2, 'HELADO', 7, 1, 80000, 80000, 'IVA_10')
ON CONFLICT (id) DO NOTHING;

INSERT INTO detalle_pedido_sabores (detalle_id, sabor_id)
VALUES (3, 9), (3, 5), (3, 4), (3, 2)
ON CONFLICT DO NOTHING;

INSERT INTO detalles_pedido (id, pedido_id, tipo_item, producto_simple_id, cantidad, precio_unitario, subtotal, tipo_iva)
VALUES (4, 2, 'PRODUCTO_SIMPLE', 1, 2, 10000, 20000, 'IVA_10')
ON CONFLICT (id) DO NOTHING;

-- Venta 3: TICKET - Tarjeta Débito con Voucher POS (Carlos Benítez)
INSERT INTO pedidos (
  id, sesion_caja_id, punto_expedicion_id, fecha_creacion, cliente_nombre,
  tipo_comprobante, condicion_venta, cliente_ruc, cliente_direccion, numero_voucher,
  metodo_pago, tipo_entrega, estado,
  total, total_gravada10, total_gravada5, total_exentas, total_iva10, total_iva5, total_iva,
  notas
) VALUES (
  3, 1, 1, NOW() - INTERVAL '1 hour', 'Carlos Alberto Benítez',
  'TICKET', 'CONTADO', '1234567-9', 'San Lorenzo', 'VOU-889123',
  'TARJETA_DEBITO', 'MOSTRADOR', 'ENTREGADO',
  60000, 54545, 0, 0, 5455, 0, 5455,
  'Cobro con tarjeta de débito POS Bancard'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO detalles_pedido (id, pedido_id, tipo_item, presentacion_id, cantidad, precio_unitario, subtotal, tipo_iva)
VALUES (5, 3, 'HELADO', 6, 1, 45000, 45000, 'IVA_10')
ON CONFLICT (id) DO NOTHING;

INSERT INTO detalle_pedido_sabores (detalle_id, sabor_id)
VALUES (5, 1), (5, 8), (5, 10)
ON CONFLICT DO NOTHING;

INSERT INTO detalles_pedido (id, pedido_id, tipo_item, producto_simple_id, cantidad, precio_unitario, subtotal, tipo_iva)
VALUES (6, 3, 'PRODUCTO_SIMPLE', 5, 1, 15000, 15000, 'IVA_10')
ON CONFLICT (id) DO NOTHING;

-- Venta 4: TICKET - Transferencia QR / Billetera
INSERT INTO pedidos (
  id, sesion_caja_id, punto_expedicion_id, fecha_creacion, cliente_nombre,
  tipo_comprobante, condicion_venta, cliente_ruc, cliente_direccion,
  metodo_pago, tipo_entrega, estado,
  total, total_gravada10, total_gravada5, total_exentas, total_iva10, total_iva5, total_iva,
  notas
) VALUES (
  4, 1, 1, NOW() - INTERVAL '30 minutes', 'Consumidor Final',
  'TICKET', 'CONTADO', '4444444-1', 'Asunción',
  'TRANSFERENCIA_QR', 'MOSTRADOR', 'ENTREGADO',
  24000, 21818, 0, 0, 2182, 0, 2182,
  'Pago con billetera electrónica QR SIPAP'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO detalles_pedido (id, pedido_id, tipo_item, presentacion_id, cantidad, precio_unitario, subtotal, tipo_iva)
VALUES (7, 4, 'HELADO', 1, 2, 12000, 24000, 'IVA_10')
ON CONFLICT (id) DO NOTHING;

INSERT INTO detalle_pedido_sabores (detalle_id, sabor_id)
VALUES (7, 11), (7, 10)
ON CONFLICT DO NOTHING;

-- 12. ACTUALIZAR TOTALES DE LA SESIÓN DE CAJA ABIERTA (id: 1)
UPDATE sesiones_caja
SET cantidad_pedidos = 4,
    total_ventas_efectivo = 121000,
    total_ventas_tarjeta_debito = 60000,
    total_ventas_tarjeta_credito = 0,
    total_ventas_transferencia_qr = 24000,
    total_ventas_general = 205000,
    total_ingresos_caja = 0,
    total_egresos_caja = 0,
    monto_esperado_efectivo = monto_inicial + 121000
WHERE id = 1;

-- 13. ACTUALIZAR SECUENCIAS A SUS VALORES MÁXIMOS
SELECT setval('categorias_id_seq', (SELECT COALESCE(MAX(id), 1) FROM categorias));
SELECT setval('clientes_id_seq', (SELECT COALESCE(MAX(id), 1) FROM clientes));
SELECT setval('proveedores_id_seq', (SELECT COALESCE(MAX(id), 1) FROM proveedores));
SELECT setval('presentaciones_id_seq', (SELECT COALESCE(MAX(id), 1) FROM presentaciones));
SELECT setval('sabores_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sabores));
SELECT setval('toppings_id_seq', (SELECT COALESCE(MAX(id), 1) FROM toppings));
SELECT setval('productos_simples_id_seq', (SELECT COALESCE(MAX(id), 1) FROM productos_simples));
SELECT setval('productos_id_seq', (SELECT COALESCE(MAX(id), 1) FROM productos));
SELECT setval('compras_id_seq', (SELECT COALESCE(MAX(id), 1) FROM compras));
SELECT setval('detalles_compra_id_seq', (SELECT COALESCE(MAX(id), 1) FROM detalles_compra));
SELECT setval('pedidos_id_seq', (SELECT COALESCE(MAX(id), 1) FROM pedidos));
SELECT setval('detalles_pedido_id_seq', (SELECT COALESCE(MAX(id), 1) FROM detalles_pedido));
SELECT setval('puntos_expedicion_id_seq', (SELECT COALESCE(MAX(id), 1) FROM puntos_expedicion));

COMMIT;
