-- Última barrera contra sobreventa: el stock nunca puede quedar negativo.
-- (productos, el maestro de combos, queda fuera hasta que su descuento sea atómico.)
ALTER TABLE sabores ADD CONSTRAINT chk_sabores_stock_kilos_no_negativo CHECK (stock_kilos IS NULL OR stock_kilos >= 0);
ALTER TABLE presentaciones ADD CONSTRAINT chk_presentaciones_stock_no_negativo CHECK (stock IS NULL OR stock >= 0);
ALTER TABLE productos_simples ADD CONSTRAINT chk_productos_simples_stock_no_negativo CHECK (stock >= 0);
