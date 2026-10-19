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
    private final AuditoriaService auditoriaService;

    public ProductoSimpleService(ProductoSimpleRepository productoSimpleRepository,
                                 ProveedorRepository proveedorRepository,
                                 AuditoriaService auditoriaService) {
        this.productoSimpleRepository = productoSimpleRepository;
        this.proveedorRepository = proveedorRepository;
        this.auditoriaService = auditoriaService;
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

        ProductoSimple guardado = productoSimpleRepository.save(producto);
        auditoriaService.registrar(
                "PRODUCTOS_CAFETERIA",
                "CREACION",
                guardado.getId(),
                guardado.getNombre(),
                "Creado producto simple '" + guardado.getNombre() + "', Categoría: " + guardado.getCategoria() +
                ", Precio: Gs. " + guardado.getPrecio() + ", Stock inicial: " + guardado.getStock()
        );
        return guardado;
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

        StringBuilder cambios = new StringBuilder();
        if (dto.getPrecio() != null && (producto.getPrecio() == null || dto.getPrecio().compareTo(producto.getPrecio()) != 0)) {
            cambios.append("Precio: Gs. ").append(producto.getPrecio()).append(" ➔ Gs. ").append(dto.getPrecio()).append("; ");
        }
        if (dto.getStock() != null && !dto.getStock().equals(producto.getStock())) {
            cambios.append("Stock: ").append(producto.getStock()).append(" ➔ ").append(dto.getStock()).append("; ");
        }
        if (!producto.getNombre().equalsIgnoreCase(dto.getNombre().trim())) {
            cambios.append("Nombre: ").append(producto.getNombre()).append(" ➔ ").append(dto.getNombre().trim()).append("; ");
        }
        if (dto.getActivo() != null && !dto.getActivo().equals(producto.getActivo())) {
            cambios.append("Estado: ").append(Boolean.TRUE.equals(producto.getActivo()) ? "Activo" : "Inactivo")
                   .append(" ➔ ").append(Boolean.TRUE.equals(dto.getActivo()) ? "Activo" : "Inactivo").append("; ");
        }
        if (cambios.length() == 0) {
            cambios.append("Modificación de datos generales del producto.");
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

        ProductoSimple actualizado = productoSimpleRepository.save(producto);
        auditoriaService.registrar(
                "PRODUCTOS_CAFETERIA",
                "MODIFICACION",
                actualizado.getId(),
                actualizado.getNombre(),
                cambios.toString().trim()
        );
        return actualizado;
    }

    @Transactional
    public void eliminar(Long id) {
        ProductoSimple producto = obtenerPorId(id);
        productoSimpleRepository.delete(producto);
        auditoriaService.registrar(
                "PRODUCTOS_CAFETERIA",
                "ELIMINACION",
                id,
                producto.getNombre(),
                "Eliminado producto simple: '" + producto.getNombre() + "'"
        );
    }
}
