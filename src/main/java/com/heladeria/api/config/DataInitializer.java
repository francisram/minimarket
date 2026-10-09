package com.heladeria.api.config;

import com.heladeria.api.entities.*;
import com.heladeria.api.entities.enums.CategoriaSabor;
import com.heladeria.api.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final SaborRepository saborRepository;
    private final PresentacionRepository presentacionRepository;
    private final ToppingRepository toppingRepository;
    private final ProductoSimpleRepository productoSimpleRepository;

    public DataInitializer(SaborRepository saborRepository,
                           PresentacionRepository presentacionRepository,
                           ToppingRepository toppingRepository,
                           ProductoSimpleRepository productoSimpleRepository) {
        this.saborRepository = saborRepository;
        this.presentacionRepository = presentacionRepository;
        this.toppingRepository = toppingRepository;
        this.productoSimpleRepository = productoSimpleRepository;
    }

    @Override
    public void run(String... args) {
        if (saborRepository.count() == 0) {
            log.info("Precargando catálogo inicial de sabores de heladería...");
            saborRepository.saveAll(List.of(
                    Sabor.builder().nombre("Dulce de Leche Granizado").descripcion("Dulce de leche tradicional con finas escamas de chocolate amargo").categoria(CategoriaSabor.DULCE_DE_LECHE).aptoCeliaco(true).esVegano(false).sinAzucar(false).disponible(true).build(),
                    Sabor.builder().nombre("Dulce de Leche Clásico").descripcion("El auténtico sabor tradicional argentino").categoria(CategoriaSabor.DULCE_DE_LECHE).aptoCeliaco(true).esVegano(false).sinAzucar(false).disponible(true).build(),
                    Sabor.builder().nombre("Chocolate Suizo").descripcion("Chocolate con dulce de leche natural y nueces").categoria(CategoriaSabor.CHOCOLATE).aptoCeliaco(false).esVegano(false).sinAzucar(false).disponible(true).build(),
                    Sabor.builder().nombre("Chocolate Amargo 70%").descripcion("Puro cacao amargo al agua, apto vegano").categoria(CategoriaSabor.CHOCOLATE).aptoCeliaco(true).esVegano(true).sinAzucar(false).disponible(true).build(),
                    Sabor.builder().nombre("Frutilla al Agua").descripcion("Frutillas frescas seleccionadas, base agua").categoria(CategoriaSabor.FRUTAL).aptoCeliaco(true).esVegano(true).sinAzucar(false).disponible(true).build(),
                    Sabor.builder().nombre("Limón al Agua").descripcion("Zumo natural de limones frescos").categoria(CategoriaSabor.FRUTAL).aptoCeliaco(true).esVegano(true).sinAzucar(false).disponible(true).build(),
                    Sabor.builder().nombre("Sambayón Italiano").descripcion("Crema con yemas de huevo y vino oporto").categoria(CategoriaSabor.CREMA).aptoCeliaco(true).esVegano(false).sinAzucar(false).disponible(true).build(),
                    Sabor.builder().nombre("Tramontana").descripcion("Crema chantilly, dulce de leche natural y galletitas crocantes").categoria(CategoriaSabor.CREMA).aptoCeliaco(false).esVegano(false).sinAzucar(false).disponible(true).build(),
                    Sabor.builder().nombre("Frutos del Bosque").descripcion("Crema americana veteada con salsa de frambuesas y moras").categoria(CategoriaSabor.CREMA).aptoCeliaco(true).esVegano(false).sinAzucar(false).disponible(true).build(),
                    Sabor.builder().nombre("Vainilla Diet (Stevia)").descripcion("Vainilla natural sin azúcar agregada endulzada con stevia").categoria(CategoriaSabor.CREMA).aptoCeliaco(true).esVegano(false).sinAzucar(true).disponible(true).build()
            ));
        }

        if (presentacionRepository.count() == 0) {
            log.info("Precargando formatos y presentaciones de helado...");
            presentacionRepository.saveAll(List.of(
                    Presentacion.builder().nombre("Cucurucho 1 Bocha").precio(new BigDecimal("12000.00")).maxSabores(1).pesoGramosAprox(100).activo(true).build(),
                    Presentacion.builder().nombre("Cucurucho 2 Bochas").precio(new BigDecimal("18000.00")).maxSabores(2).pesoGramosAprox(180).activo(true).build(),
                    Presentacion.builder().nombre("Vaso Mediano").precio(new BigDecimal("16000.00")).maxSabores(2).pesoGramosAprox(150).activo(true).build(),
                    Presentacion.builder().nombre("Pote 1/4 Kg").precio(new BigDecimal("22000.00")).maxSabores(3).pesoGramosAprox(250).activo(true).build(),
                    Presentacion.builder().nombre("Pote 1/2 Kg").precio(new BigDecimal("38000.00")).maxSabores(3).pesoGramosAprox(500).activo(true).build(),
                    Presentacion.builder().nombre("Pote 1 Kg").precio(new BigDecimal("68000.00")).maxSabores(4).pesoGramosAprox(1000).activo(true).build()
            ));
        }

        if (toppingRepository.count() == 0) {
            log.info("Precargando toppings y salsas...");
            toppingRepository.saveAll(List.of(
                    Topping.builder().nombre("Baño de Chocolate").precioExtra(new BigDecimal("3000.00")).disponible(true).build(),
                    Topping.builder().nombre("Salsa de Caramelo").precioExtra(new BigDecimal("2500.00")).disponible(true).build(),
                    Topping.builder().nombre("Almendras Tostadas").precioExtra(new BigDecimal("4000.00")).disponible(true).build(),
                    Topping.builder().nombre("Cucurucho Extra").precioExtra(new BigDecimal("2000.00")).disponible(true).build()
            ));
        }

        if (productoSimpleRepository.count() == 0) {
            log.info("Precargando productos simples (bebidas y envasados)...");
            productoSimpleRepository.saveAll(List.of(
                    ProductoSimple.builder().nombre("Agua Mineral con gas 500ml").precio(new BigDecimal("5000.00")).categoria("BEBIDAS").stock(40).activo(true).build(),
                    ProductoSimple.builder().nombre("Agua Mineral sin gas 500ml").precio(new BigDecimal("5000.00")).categoria("BEBIDAS").stock(50).activo(true).build(),
                    ProductoSimple.builder().nombre("Alfajor Helado").precio(new BigDecimal("15000.00")).categoria("ENVASADOS").stock(25).activo(true).build(),
                    ProductoSimple.builder().nombre("Paleta Bombón Artesanal").precio(new BigDecimal("14000.00")).categoria("ENVASADOS").stock(30).activo(true).build()
            ));
        }
        log.info("Catálogo inicial precargado exitosamente.");
    }
}
