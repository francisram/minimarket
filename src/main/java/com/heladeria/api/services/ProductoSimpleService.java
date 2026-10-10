package com.heladeria.api.services;

import com.heladeria.api.dto.ProductoSimpleRequestDTO;
import com.heladeria.api.entities.ProductoSimple;
import com.heladeria.api.entities.enums.TipoIva;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.ProductoSimpleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoSimpleService {

    private final ProductoSimpleRepository productoSimpleRepository;

    public ProductoSimpleService(ProductoSimpleRepository productoSimpleRepository) {
        this.productoSimpleRepository = productoSimpleRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoSimple> listar(boolean soloActivos, String categoria) {
        if (categoria != null && !categoria.isBlank()) {
            return productoSimpleRepository.findByCategoriaIgnoreCase(categoria);
        }
        if (soloActivos) {
            return productoSimpleRepository.findByActivoTrue();
        }
        return productoSimpleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ProductoSimple obtenerPorId(Long id) {
        return productoSimpleRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con id " + id + " no encontrado"));
    }

    @Transactional
    public ProductoSimple crear(ProductoSimpleRequestDTO dto) {
        if (productoSimpleRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ReglaDeNegocioException("Ya existe un producto con el nombre: " + dto.getNombre());
        }

        ProductoSimple producto = ProductoSimple.builder()
                .nombre(dto.getNombre())
                .precio(dto.getPrecio())
                .categoria(dto.getCategoria())
                .stock(dto.getStock() != null ? dto.getStock() : 0)
                .stockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : 5)
                .activo(dto.getActivo() == null || dto.getActivo())
                .tipoIva(dto.getTipoIva() != null ? dto.getTipoIva() : TipoIva.IVA_10)
                .build();

        return productoSimpleRepository.save(producto);
    }

    @Transactional
    public ProductoSimple actualizar(Long id, ProductoSimpleRequestDTO dto) {
        ProductoSimple producto = obtenerPorId(id);

        if (!producto.getNombre().equalsIgnoreCase(dto.getNombre()) &&
                productoSimpleRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ReglaDeNegocioException("Ya existe un producto con el nombre: " + dto.getNombre());
        }

        producto.setNombre(dto.getNombre());
        producto.setPrecio(dto.getPrecio());
        producto.setCategoria(dto.getCategoria());
        if (dto.getStock() != null) {
            producto.setStock(dto.getStock());
        }
        if (dto.getStockMinimo() != null) {
            producto.setStockMinimo(dto.getStockMinimo());
        }
        if (dto.getActivo() != null) {
            producto.setActivo(dto.getActivo());
        }
        if (dto.getTipoIva() != null) {
            producto.setTipoIva(dto.getTipoIva());
        }

        return productoSimpleRepository.save(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        ProductoSimple producto = obtenerPorId(id);
        productoSimpleRepository.delete(producto);
    }
}
