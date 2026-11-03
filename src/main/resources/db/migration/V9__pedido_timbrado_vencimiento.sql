-- Vigencia del timbrado con el que se emitió cada factura, para reimprimir con los datos de la emisión
ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS timbrado_vencimiento date;

UPDATE pedidos p
   SET timbrado_vencimiento = pe.timbrado_vencimiento
  FROM puntos_expedicion pe
 WHERE p.punto_expedicion_id = pe.id
   AND p.numero_factura IS NOT NULL
   AND p.timbrado_vencimiento IS NULL;
