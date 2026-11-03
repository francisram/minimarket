-- Completa V2: el maestro de productos (componentes de combos) tampoco puede quedar con stock negativo.
-- NOT VALID: se exige en toda escritura nueva sin abortar el arranque si hubiera filas históricas negativas
-- (se pueden corregir y luego ejecutar: ALTER TABLE productos VALIDATE CONSTRAINT chk_productos_stock_no_negativo).
ALTER TABLE productos ADD CONSTRAINT chk_productos_stock_no_negativo CHECK (stock >= 0) NOT VALID;
