package com.heladeria.api.services;

import com.heladeria.api.dto.stock.AjusteStockDTO;
import com.heladeria.api.dto.stock.AlertaStockItemDTO;
import com.heladeria.api.dto.stock.StockAlertasResponseDTO;
import com.heladeria.api.entities.Presentacion;
import com.heladeria.api.entities.ProductoSimple;
import com.heladeria.api.entities.Sabor;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.repositories.PresentacionRepository;
import com.heladeria.api.repositories.ProductoSimpleRepository;
import com.heladeria.api.repositories.SaborRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class StockService {

    private final ProductoSimpleRepository productoSimpleRepository;
    private final SaborRepository saborRepository;
    private final PresentacionRepository presentacionRepository;

    public StockService(ProductoSimpleRepository productoSimpleRepository,
                        SaborRepository saborRepository,
                        PresentacionRepository presentacionRepository) {
        this.productoSimpleRepository = productoSimpleRepository;
        this.saborRepository = saborRepository;
        this.presentacionRepository = presentacionRepository;
    }

    @Transactional(readOnly = true)
    public StockAlertasResponseDTO obtenerAlertasStock() {
        StockAlertasResponseDTO response = new StockAlertasResponseDTO();

        int totalAgotados = 0;
        int totalBajo = 0;

        // 1. Productos simples
        List<AlertaStockItemDTO> productosAlertas = new ArrayList<>();
        List<ProductoSimple> productos = productoSimpleRepository.findAll();
        for (ProductoSimple prod : productos) {
            int stockActual = prod.getStock() != null ? prod.getStock() : 0;
            int stockMin = prod.getStockMinimo() != null ? prod.getStockMinimo() : 5;

            if (stockActual <= stockMin) {
                String estado = stockActual <= 0 ? "AGOTADO" : "BAJO";
                if ("AGOTADO".equals(estado)) {
                    totalAgotados++;
                } else {
                    totalBajo++;
                }

                productosAlertas.add(new AlertaStockItemDTO(
                        prod.getId(),
                        prod.getNombre(),
                        "PRODUCTO",
                        (double) stockActual,
                        (double) stockMin,
                        "UNIDADES",
                        estado,
                        prod.getActivo()
                ));
            }
        }
        response.setProductosBajoStock(productosAlertas);

        // 2. Sabores de helado
        List<AlertaStockItemDTO> saboresAlertas = new ArrayList<>();
        List<Sabor> sabores = saborRepository.findAll();
        for (Sabor sab : sabores) {
            double stockKilos = sab.getStockKilos() != null ? sab.getStockKilos() : 0.0;
            double stockMin = sab.getStockMinimoKilos() != null ? sab.getStockMinimoKilos() : 2.0;
            boolean noDisponible = !Boolean.TRUE.equals(sab.getDisponible());

            if (stockKilos <= stockMin || noDisponible) {
                String estado = (stockKilos <= 0.001 || noDisponible) ? "AGOTADO" : "BAJO";
                if ("AGOTADO".equals(estado)) {
                    totalAgotados++;
                } else {
                    totalBajo++;
                }

                saboresAlertas.add(new AlertaStockItemDTO(
                        sab.getId(),
                        sab.getNombre(),
                        "SABOR",
                        stockKilos,
                        stockMin,
                        "KG",
                        estado,
                        sab.getDisponible()
                ));
            }
        }
        response.setSaboresBajoStock(saboresAlertas);

        // 3. Presentaciones / Envases / Cucuruchos
        List<AlertaStockItemDTO> presentacionesAlertas = new ArrayList<>();
        List<Presentacion> presentaciones = presentacionRepository.findAll();
        for (Presentacion pres : presentaciones) {
            if (pres.getStock() != null) {
                int stockActual = pres.getStock();
                int stockMin = pres.getStockMinimo() != null ? pres.getStockMinimo() : 10;

                if (stockActual <= stockMin) {
                    String estado = stockActual <= 0 ? "AGOTADO" : "BAJO";
                    if ("AGOTADO".equals(estado)) {
                        totalAgotados++;
                    } else {
                        totalBajo++;
                    }

                    presentacionesAlertas.add(new AlertaStockItemDTO(
                            pres.getId(),
                            pres.getNombre(),
                            "ENVASE",
                            (double) stockActual,
                            (double) stockMin,
                            "UNIDADES",
                            estado,
                            pres.getActivo()
                    ));
                }
            }
        }
        response.setPresentacionesBajoStock(presentacionesAlertas);

        response.setTotalAgotados(totalAgotados);
        response.setTotalBajoStock(totalBajo);
        response.setTotalAlertas(totalAgotados + totalBajo);

        return response;
    }

    @Transactional
    public ProductoSimple actualizarStockProducto(Long id, AjusteStockDTO dto) {
        ProductoSimple prod = productoSimpleRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con id " + id + " no encontrado"));

        if (dto.getStock() != null) {
            prod.setStock(dto.getStock());
        }
        if (dto.getStockMinimo() != null) {
            prod.setStockMinimo(dto.getStockMinimo());
        }

        return productoSimpleRepository.save(prod);
    }

    @Transactional
    public Sabor actualizarStockSabor(Long id, AjusteStockDTO dto) {
        Sabor sabor = saborRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sabor con id " + id + " no encontrado"));

        if (dto.getStockKilos() != null) {
            sabor.setStockKilos(dto.getStockKilos());
            if (dto.getStockKilos() > 0.001) {
                sabor.setDisponible(true);
            } else {
                sabor.setDisponible(false);
            }
        }
        if (dto.getStockMinimoKilos() != null) {
            sabor.setStockMinimoKilos(dto.getStockMinimoKilos());
        }

        return saborRepository.save(sabor);
    }

    @Transactional
    public Presentacion actualizarStockPresentacion(Long id, AjusteStockDTO dto) {
        Presentacion pres = presentacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Presentación con id " + id + " no encontrada"));

        if (dto.getStock() != null) {
            pres.setStock(dto.getStock());
        }
        if (dto.getStockMinimo() != null) {
            pres.setStockMinimo(dto.getStockMinimo());
        }

        return presentacionRepository.save(pres);
    }
}
