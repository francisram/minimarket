package com.heladeria.api.config;

import com.heladeria.api.entities.*;
import com.heladeria.api.entities.enums.CategoriaSabor;
import com.heladeria.api.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Order(2)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final SaborRepository saborRepository;
    private final PresentacionRepository presentacionRepository;
    private final ToppingRepository toppingRepository;
    private final ProductoSimpleRepository productoSimpleRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final CompraRepository compraRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public DataInitializer(SaborRepository saborRepository,
                           PresentacionRepository presentacionRepository,
                           ToppingRepository toppingRepository,
                           ProductoSimpleRepository productoSimpleRepository,
                           ProductoRepository productoRepository,
                           ProveedorRepository proveedorRepository,
                           CompraRepository compraRepository,
                           org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.saborRepository = saborRepository;
        this.presentacionRepository = presentacionRepository;
        this.toppingRepository = toppingRepository;
        this.productoSimpleRepository = productoSimpleRepository;
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
        this.compraRepository = compraRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("ALTER TABLE detalles_pedido DROP CONSTRAINT IF EXISTS detalles_pedido_tipo_item_check");
            jdbcTemplate.execute("ALTER TABLE detalles_pedido ADD CONSTRAINT detalles_pedido_tipo_item_check CHECK (tipo_item IN ('HELADO', 'PRODUCTO_SIMPLE', 'TOPPING'))");
        } catch (Exception e) {
            log.warn("Verificación de constraint detalles_pedido_tipo_item_check: {}", e.getMessage());
        }

        if (saborRepository.count() == 0) {
            log.info("Precargando catálogo inicial de sabores de heladería...");
            saborRepository.saveAll(List.of(
                    Sabor.builder().nombre("Dulce de Leche Granizado").descripcion("Dulce de leche tradicional con finas escamas de chocolate amargo").categoria(CategoriaSabor.DULCE_DE_LECHE).aptoCeliaco(true).esVegano(false).sinAzucar(false).disponible(true).stockKilos(10.0).stockMinimoKilos(2.0).build(),
                    Sabor.builder().nombre("Dulce de Leche Clásico").descripcion("El auténtico sabor tradicional argentino").categoria(CategoriaSabor.DULCE_DE_LECHE).aptoCeliaco(true).esVegano(false).sinAzucar(false).disponible(true).stockKilos(10.0).stockMinimoKilos(2.0).build(),
                    Sabor.builder().nombre("Chocolate Suizo").descripcion("Chocolate con dulce de leche natural y nueces").categoria(CategoriaSabor.CHOCOLATE).aptoCeliaco(false).esVegano(false).sinAzucar(false).disponible(true).stockKilos(10.0).stockMinimoKilos(2.0).build(),
                    Sabor.builder().nombre("Chocolate Amargo 70%").descripcion("Puro cacao amargo al agua, apto vegano").categoria(CategoriaSabor.CHOCOLATE).aptoCeliaco(true).esVegano(true).sinAzucar(false).disponible(true).stockKilos(10.0).stockMinimoKilos(2.0).build(),
                    Sabor.builder().nombre("Frutilla al Agua").descripcion("Frutillas frescas seleccionadas, base agua").categoria(CategoriaSabor.FRUTAL).aptoCeliaco(true).esVegano(true).sinAzucar(false).disponible(true).stockKilos(10.0).stockMinimoKilos(2.0).build(),
                    Sabor.builder().nombre("Limón al Agua").descripcion("Zumo natural de limones frescos").categoria(CategoriaSabor.FRUTAL).aptoCeliaco(true).esVegano(true).sinAzucar(false).disponible(true).stockKilos(10.0).stockMinimoKilos(2.0).build(),
                    Sabor.builder().nombre("Sambayón Italiano").descripcion("Crema con yemas de huevo y vino oporto").categoria(CategoriaSabor.CREMA).aptoCeliaco(true).esVegano(false).sinAzucar(false).disponible(true).stockKilos(10.0).stockMinimoKilos(2.0).build(),
                    Sabor.builder().nombre("Tramontana").descripcion("Crema chantilly, dulce de leche natural y galletitas crocantes").categoria(CategoriaSabor.CREMA).aptoCeliaco(false).esVegano(false).sinAzucar(false).disponible(true).stockKilos(10.0).stockMinimoKilos(2.0).build(),
                    Sabor.builder().nombre("Frutos del Bosque").descripcion("Crema americana veteada con salsa de frambuesas y moras").categoria(CategoriaSabor.CREMA).aptoCeliaco(true).esVegano(false).sinAzucar(false).disponible(true).stockKilos(10.0).stockMinimoKilos(2.0).build(),
                    Sabor.builder().nombre("Vainilla Diet (Stevia)").descripcion("Vainilla natural sin azúcar agregada endulzada con stevia").categoria(CategoriaSabor.CREMA).aptoCeliaco(true).esVegano(false).sinAzucar(true).disponible(true).stockKilos(10.0).stockMinimoKilos(2.0).build()
            ));
        }

        if (presentacionRepository.count() == 0) {
            log.info("Precargando formatos y presentaciones de helado...");
            presentacionRepository.saveAll(List.of(
                    Presentacion.builder().nombre("Cucurucho 1 Bocha").precio(new BigDecimal("12000")).maxSabores(1).pesoGramosAprox(100).stock(50).stockMinimo(10).activo(true).build(),
                    Presentacion.builder().nombre("Cucurucho 2 Bochas").precio(new BigDecimal("18000")).maxSabores(2).pesoGramosAprox(180).stock(50).stockMinimo(10).activo(true).build(),
                    Presentacion.builder().nombre("Vaso Mediano").precio(new BigDecimal("18000")).maxSabores(2).pesoGramosAprox(150).stock(40).stockMinimo(10).activo(true).build(),
                    Presentacion.builder().nombre("Pote 1/4 Kg").precio(new BigDecimal("25000")).maxSabores(3).pesoGramosAprox(250).stock(30).stockMinimo(10).activo(true).build(),
                    Presentacion.builder().nombre("Pote 1/2 Kg").precio(new BigDecimal("45000")).maxSabores(3).pesoGramosAprox(500).stock(30).stockMinimo(10).activo(true).build(),
                    Presentacion.builder().nombre("Pote 1 Kg").precio(new BigDecimal("80000")).maxSabores(4).pesoGramosAprox(1000).stock(30).stockMinimo(10).activo(true).build()
            ));
        }

        if (toppingRepository.count() == 0) {
            log.info("Precargando toppings y salsas...");
            toppingRepository.saveAll(List.of(
                    Topping.builder().nombre("Baño de Chocolate").precioExtra(new BigDecimal("3000")).disponible(true).build(),
                    Topping.builder().nombre("Salsa de Caramelo").precioExtra(new BigDecimal("3000")).disponible(true).build(),
                    Topping.builder().nombre("Almendras Tostadas").precioExtra(new BigDecimal("3000")).disponible(true).build(),
                    Topping.builder().nombre("Cucurucho Extra").precioExtra(new BigDecimal("3000")).disponible(true).build()
            ));
        }

        if (productoSimpleRepository.count() == 0) {
            log.info("Precargando productos simples (bebidas y envasados)...");
            productoSimpleRepository.saveAll(List.of(
                    ProductoSimple.builder().nombre("Agua Mineral con gas 500ml").precio(new BigDecimal("10000")).categoria("BEBIDAS").stock(40).stockMinimo(5).activo(true).codigoBarra("7840001001").build(),
                    ProductoSimple.builder().nombre("Agua Mineral sin gas 500ml").precio(new BigDecimal("10000")).categoria("BEBIDAS").stock(50).stockMinimo(5).activo(true).codigoBarra("7840001002").build(),
                    ProductoSimple.builder().nombre("Alfajor Helado").precio(new BigDecimal("15000")).categoria("ENVASADOS").stock(25).stockMinimo(5).activo(true).codigoBarra("7840001003").build(),
                    ProductoSimple.builder().nombre("Paleta Bombón Artesanal").precio(new BigDecimal("12000")).categoria("ENVASADOS").stock(30).stockMinimo(5).activo(true).codigoBarra("7840001004").build()
            ));
        }

        if (productoRepository.count() == 0) {
            log.info("Precargando Catálogo Maestro Unificado de Productos (Retail ERP)...");
            List<Proveedor> proveedores = proveedorRepository.findAll();
            Proveedor provSur = proveedores.stream().filter(p -> p.getRazonSocial().contains("Bebidas")).findFirst().orElse(null);
            Proveedor provValle = proveedores.stream().filter(p -> p.getRazonSocial().contains("Valle")).findFirst().orElse(null);

            // 1. Envases (con múltiples proveedores demostrativos)
            Producto pote1kg = new Producto();
            pote1kg.setNombre("Pote Térmico 1 Kg");
            pote1kg.setTipoArticulo(com.heladeria.api.entities.enums.TipoArticulo.ENVASE);
            pote1kg.setPrecioVenta(new BigDecimal("80000"));
            pote1kg.setPrecioCosto(new BigDecimal("1500"));
            pote1kg.setStock(new BigDecimal("50"));
            pote1kg.setMaxSabores(4);
            pote1kg.setPesoGramosAprox(1000);
            pote1kg.setUnidadMedida(com.heladeria.api.entities.enums.UnidadMedida.UNIDAD);
            if (provSur != null) pote1kg.agregarProveedor(provSur, "POT-1KG-SUR", new BigDecimal("1400"), true);
            if (provValle != null) pote1kg.agregarProveedor(provValle, "POT-1KG-VALLE", new BigDecimal("1550"), false);
            productoRepository.save(pote1kg);

            Producto poteMedio = new Producto();
            poteMedio.setNombre("Pote Térmico 1/2 Kg");
            poteMedio.setTipoArticulo(com.heladeria.api.entities.enums.TipoArticulo.ENVASE);
            poteMedio.setPrecioVenta(new BigDecimal("45000"));
            poteMedio.setPrecioCosto(new BigDecimal("1000"));
            poteMedio.setStock(new BigDecimal("60"));
            poteMedio.setMaxSabores(3);
            poteMedio.setPesoGramosAprox(500);
            poteMedio.setUnidadMedida(com.heladeria.api.entities.enums.UnidadMedida.UNIDAD);
            if (provSur != null) poteMedio.agregarProveedor(provSur, "POT-500G", new BigDecimal("980"), true);
            productoRepository.save(poteMedio);

            Producto cucuruchoDoble = new Producto();
            cucuruchoDoble.setNombre("Cucurucho 2 Bochas");
            cucuruchoDoble.setTipoArticulo(com.heladeria.api.entities.enums.TipoArticulo.ENVASE);
            cucuruchoDoble.setPrecioVenta(new BigDecimal("18000"));
            cucuruchoDoble.setPrecioCosto(new BigDecimal("400"));
            cucuruchoDoble.setStock(new BigDecimal("100"));
            cucuruchoDoble.setMaxSabores(2);
            cucuruchoDoble.setPesoGramosAprox(180);
            cucuruchoDoble.setUnidadMedida(com.heladeria.api.entities.enums.UnidadMedida.UNIDAD);
            if (provValle != null) cucuruchoDoble.agregarProveedor(provValle, "CUC-DOB", new BigDecimal("400"), true);
            productoRepository.save(cucuruchoDoble);

            // 2. Sabores
            Producto ddl = new Producto();
            ddl.setNombre("Dulce de Leche Granizado");
            ddl.setTipoArticulo(com.heladeria.api.entities.enums.TipoArticulo.SABOR);
            ddl.setPrecioVenta(BigDecimal.ZERO);
            ddl.setPrecioCosto(new BigDecimal("35000"));
            ddl.setStock(new BigDecimal("10.0"));
            ddl.setStockMinimo(new BigDecimal("2.0"));
            ddl.setUnidadMedida(com.heladeria.api.entities.enums.UnidadMedida.KG);
            ddl.setCategoriaSabor(CategoriaSabor.DULCE_DE_LECHE);
            ddl.setAptoCeliaco(true);
            if (provValle != null) ddl.agregarProveedor(provValle, "BASE-DDL-01", new BigDecimal("35000"), true);
            productoRepository.save(ddl);

            Producto chocoSuizo = new Producto();
            chocoSuizo.setNombre("Chocolate Suizo");
            chocoSuizo.setTipoArticulo(com.heladeria.api.entities.enums.TipoArticulo.SABOR);
            chocoSuizo.setPrecioVenta(BigDecimal.ZERO);
            chocoSuizo.setPrecioCosto(new BigDecimal("42000"));
            chocoSuizo.setStock(new BigDecimal("10.0"));
            chocoSuizo.setStockMinimo(new BigDecimal("2.0"));
            chocoSuizo.setUnidadMedida(com.heladeria.api.entities.enums.UnidadMedida.KG);
            chocoSuizo.setCategoriaSabor(CategoriaSabor.CHOCOLATE);
            chocoSuizo.setAptoCeliaco(false);
            if (provValle != null) chocoSuizo.agregarProveedor(provValle, "BASE-CHOC-SUI", new BigDecimal("42000"), true);
            productoRepository.save(chocoSuizo);

            // 3. Toppings
            Producto banoChoco = new Producto();
            banoChoco.setNombre("Baño de Chocolate");
            banoChoco.setTipoArticulo(com.heladeria.api.entities.enums.TipoArticulo.TOPPING);
            banoChoco.setPrecioVenta(new BigDecimal("3000"));
            banoChoco.setPrecioCosto(new BigDecimal("600"));
            banoChoco.setStock(new BigDecimal("100"));
            banoChoco.setUnidadMedida(com.heladeria.api.entities.enums.UnidadMedida.UNIDAD);
            if (provValle != null) banoChoco.agregarProveedor(provValle, "TOP-CHOC", new BigDecimal("600"), true);
            productoRepository.save(banoChoco);

            // 4. Venta Directa (con códigos de barra)
            Producto aguaConGas = new Producto();
            aguaConGas.setNombre("Agua Mineral con gas 500ml");
            aguaConGas.setTipoArticulo(com.heladeria.api.entities.enums.TipoArticulo.VENTA_DIRECTA);
            aguaConGas.setCodigoBarra("7840001001");
            aguaConGas.setPrecioVenta(new BigDecimal("10000"));
            aguaConGas.setPrecioCosto(new BigDecimal("4500"));
            aguaConGas.setStock(new BigDecimal("40"));
            aguaConGas.setUnidadMedida(com.heladeria.api.entities.enums.UnidadMedida.UNIDAD);
            if (provSur != null) aguaConGas.agregarProveedor(provSur, "AGUA-GAS-500", new BigDecimal("4500"), true);
            productoRepository.save(aguaConGas);

            Producto alfajor = new Producto();
            alfajor.setNombre("Alfajor Helado");
            alfajor.setTipoArticulo(com.heladeria.api.entities.enums.TipoArticulo.VENTA_DIRECTA);
            alfajor.setCodigoBarra("7840001003");
            alfajor.setPrecioVenta(new BigDecimal("15000"));
            alfajor.setPrecioCosto(new BigDecimal("8000"));
            alfajor.setStock(new BigDecimal("25"));
            alfajor.setUnidadMedida(com.heladeria.api.entities.enums.UnidadMedida.UNIDAD);
            if (provValle != null) alfajor.agregarProveedor(provValle, "ALF-HEL-01", new BigDecimal("8000"), true);
            productoRepository.save(alfajor);

            // 5. Insumos
            Producto cucharas = new Producto();
            cucharas.setNombre("Cucharas Plásticas Reforzadas (Pack 100u)");
            cucharas.setTipoArticulo(com.heladeria.api.entities.enums.TipoArticulo.INSUMO);
            cucharas.setPrecioVenta(BigDecimal.ZERO);
            cucharas.setPrecioCosto(new BigDecimal("12000"));
            cucharas.setStock(new BigDecimal("50"));
            cucharas.setUnidadMedida(com.heladeria.api.entities.enums.UnidadMedida.UNIDAD);
            if (provSur != null) cucharas.agregarProveedor(provSur, "CUCH-100U", new BigDecimal("11500"), true);
            productoRepository.save(cucharas);
        }

        if (compraRepository.count() == 0) {
            log.info("Precargando facturas de compra de prueba...");
            Proveedor provValle = proveedorRepository.findByRucIgnoreCase("80012345-6").orElse(null);
            Producto pote1k = productoRepository.findByCodigoBarraIgnoreCase("7840001000012").orElse(null);
            if (provValle != null && pote1k != null) {
                Compra c1 = new Compra();
                c1.setProveedor(provValle);
                c1.setNumeroFactura("001-001-0004589");
                c1.setTimbrado("14892034");
                c1.setFechaEmision(java.time.LocalDate.now().minusDays(3));
                c1.setCondicionCompra(com.heladeria.api.entities.enums.CondicionCompra.CONTADO);
                c1.setObservacion("Factura de reposición de potes térmicos");
                c1.setUsuario("ADMIN");

                DetalleCompra d1 = new DetalleCompra();
                d1.setProducto(pote1k);
                d1.setCantidad(new BigDecimal("10"));
                d1.setCostoUnitario(new BigDecimal("18000"));
                d1.setSubtotal(new BigDecimal("180000"));
                d1.setTipoIva(com.heladeria.api.entities.enums.TipoIva.IVA_10);
                d1.setMontoIva(com.heladeria.api.util.MonedaPyUtils.calcularIva10(new BigDecimal("180000")));
                c1.agregarDetalle(d1);

                c1.setTotalIva10(d1.getMontoIva());
                c1.setTotal(new BigDecimal("180000"));
                compraRepository.save(c1);
            }
        }

        log.info("Catálogo inicial precargado exitosamente.");
    }
}
