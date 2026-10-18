package com.heladeria.api.services;

import com.heladeria.api.dto.ProductoSimpleRequestDTO;
import com.heladeria.api.entities.ProductoSimple;
import com.heladeria.api.entities.Proveedor;
import com.heladeria.api.entities.enums.TipoIva;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.ProductoSimpleRepository;
import com.heladeria.api.repositories.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoSimpleService {

    private final ProductoSimpleRepository productoSimpleRepository;
    private final ProveedorRepository proveedorRepository;

    public ProductoSimpleService(ProductoSimpleRepository productoSimpleRepository,
                                 ProveedorRepository proveedorRepository) {
        this.productoSimpleRepository = productoSimpleRepository;
        this.proveedorRepository = proveedorRepository;
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

    @Transactional(readOnly = true)
    public List<ProductoSimple> listarPorProveedor(Long proveedorId) {
        if (proveedorId == null) {
            throw new ReglaDeNegocioException("El ID del proveedor no puede ser nulo");
        }
        return productoSimpleRepository.findByProveedorId(proveedorId);
    }

    @Transactional(readOnly = true)
    public ProductoSimple buscarPorCodigoBarra(String codigoBarra) {
        if (codigoBarra == null || codigoBarra.trim().isBlank()) {
            throw new ReglaDeNegocioException("El código de barra a buscar no puede estar vacío");
        }
        return productoSimpleRepository.findByCodigoBarraIgnoreCase(codigoBarra.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró ningún producto con el código de barra: " + codigoBarra.trim()));
    }

    @Transactional
    public ProductoSimple crear(ProductoSimpleRequestDTO dto) {
        if (productoSimpleRepository.existsByNombreIgnoreCase(dto.getNombre().trim())) {
            throw new ReglaDeNegocioException("Ya existe un producto con el nombre: " + dto.getNombre().trim());
        }

        String codigoBarraLimpio = (dto.getCodigoBarra() != null && !dto.getCodigoBarra().isBlank())
                ? dto.getCodigoBarra().trim()
                : null;

        if (codigoBarraLimpio != null && productoSimpleRepository.existsByCodigoBarraIgnoreCase(codigoBarraLimpio)) {
            throw new ReglaDeNegocioException("Ya existe un producto con el código de barra: " + codigoBarraLimpio);
        }

        Proveedor proveedor = null;
        if (dto.getProveedorId() != null) {
            proveedor = proveedorRepository.findById(dto.getProveedorId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor con id " + dto.getProveedorId() + " no encontrado"));
        }

        ProductoSimple producto = ProductoSimple.builder()
                .nombre(dto.getNombre().trim())
                .precio(dto.getPrecio())
                .categoria(dto.getCategoria())
                .stock(dto.getStock() != null ? dto.getStock() : 0)
                .stockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : 5)
                .activo(dto.getActivo() == null || dto.getActivo())
                .tipoIva(dto.getTipoIva() != null ? dto.getTipoIva() : TipoIva.IVA_10)
                .codigoBarra(codigoBarraLimpio)
                .proveedor(proveedor)
                .build();

        return productoSimpleRepository.save(producto);
    }

    @Transactional
    public ProductoSimple actualizar(Long id, ProductoSimpleRequestDTO dto) {
        ProductoSimple producto = obtenerPorId(id);

        if (!producto.getNombre().equalsIgnoreCase(dto.getNombre().trim()) &&
                productoSimpleRepository.existsByNombreIgnoreCase(dto.getNombre().trim())) {
            throw new ReglaDeNegocioException("Ya existe un producto con el nombre: " + dto.getNombre().trim());
        }

        String codigoBarraLimpio = (dto.getCodigoBarra() != null && !dto.getCodigoBarra().isBlank())
                ? dto.getCodigoBarra().trim()
                : null;

        if (codigoBarraLimpio != null && productoSimpleRepository.existsByCodigoBarraIgnoreCaseAndIdNot(codigoBarraLimpio, id)) {
            throw new ReglaDeNegocioException("Ya existe otro producto con el código de barra: " + codigoBarraLimpio);
        }

        Proveedor proveedor = null;
        if (dto.getProveedorId() != null) {
            proveedor = proveedorRepository.findById(dto.getProveedorId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor con id " + dto.getProveedorId() + " no encontrado"));
        }

        producto.setNombre(dto.getNombre().trim());
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
        producto.setCodigoBarra(codigoBarraLimpio);
        producto.setProveedor(proveedor);

        return productoSimpleRepository.save(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        ProductoSimple producto = obtenerPorId(id);
        productoSimpleRepository.delete(producto);
    }
}
