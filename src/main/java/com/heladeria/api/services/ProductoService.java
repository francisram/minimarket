package com.heladeria.api.services;

import com.heladeria.api.dto.AsignarProveedorRequestDTO;
import com.heladeria.api.dto.ProductoDTO;
import com.heladeria.api.dto.ProductoRequestDTO;
import com.heladeria.api.dto.ProveedorVinculadoDTO;
import com.heladeria.api.entities.Producto;
import com.heladeria.api.entities.ProductoProveedor;
import com.heladeria.api.entities.Proveedor;
import com.heladeria.api.entities.enums.TipoArticulo;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.ProductoProveedorRepository;
import com.heladeria.api.repositories.ProductoRepository;
import com.heladeria.api.repositories.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoProveedorRepository productoProveedorRepository;
    private final ProveedorRepository proveedorRepository;
    private final AuditoriaService auditoriaService;

    public ProductoService(ProductoRepository productoRepository,
                           ProductoProveedorRepository productoProveedorRepository,
                           ProveedorRepository proveedorRepository,
                           AuditoriaService auditoriaService) {
        this.productoRepository = productoRepository;
        this.productoProveedorRepository = productoProveedorRepository;
        this.proveedorRepository = proveedorRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listar(TipoArticulo tipo, Long proveedorId, Boolean soloActivos, String buscar) {
        List<Producto> productos;

        if (buscar != null && !buscar.trim().isBlank()) {
            productos = productoRepository.buscarPorFiltro(buscar.trim());
        } else if (proveedorId != null) {
            productos = productoRepository.findByProveedorId(proveedorId);
        } else if (tipo != null) {
            productos = Boolean.TRUE.equals(soloActivos)
                    ? productoRepository.findByTipoArticuloAndActivoTrue(tipo)
                    : productoRepository.findByTipoArticulo(tipo);
        } else if (Boolean.TRUE.equals(soloActivos)) {
            productos = productoRepository.findByActivoTrue();
        } else {
            productos = productoRepository.findAll();
        }

        // Si se especificó tanto tipo como proveedor, filtramos adicionalmente en memoria
        if (tipo != null && proveedorId != null) {
            productos = productos.stream()
                    .filter(p -> p.getTipoArticulo() == tipo)
                    .collect(Collectors.toList());
        }

        if (Boolean.TRUE.equals(soloActivos)) {
            productos = productos.stream()
                    .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                    .collect(Collectors.toList());
        }

        return productos.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con ID " + id + " no encontrado"));
        return mapToDTO(producto);
    }

    @Transactional(readOnly = true)
    public ProductoDTO buscarPorCodigoBarra(String codigoBarra) {
        if (codigoBarra == null || codigoBarra.trim().isBlank()) {
            throw new ReglaDeNegocioException("El código de barra no puede estar vacío");
        }
        Producto producto = productoRepository.findByCodigoBarraIgnoreCase(codigoBarra.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró ningún artículo con el código de barra: " + codigoBarra.trim()));
        return mapToDTO(producto);
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarPorProveedor(Long proveedorId) {
        return productoRepository.findByProveedorId(proveedorId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductoDTO crear(ProductoRequestDTO dto) {
        validarUnicidad(dto.getNombre(), dto.getCodigoBarra(), null);

        Producto producto = new Producto();
        mapearDatos(producto, dto);
        Producto guardado = productoRepository.save(producto);
        if (dto.getProveedores() != null && !dto.getProveedores().isEmpty()) {
            for (AsignarProveedorRequestDTO provDto : dto.getProveedores()) {
                Proveedor prov = proveedorRepository.findById(provDto.getProveedorId())
                        .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor ID " + provDto.getProveedorId() + " no encontrado"));
                guardado.agregarProveedor(prov, provDto.getCodigoReferencia(), provDto.getCostoCompra(), provDto.getEsPrincipal());
            }
            guardado = productoRepository.save(guardado);
        }

        auditoriaService.registrar(
                "CATALOGO_MAESTRO",
                "CREACION",
                guardado.getId(),
                guardado.getNombre(),
                "Creado producto maestro '" + guardado.getNombre() + "', Tipo: " + guardado.getTipoArticulo() +
                ", Precio Venta: Gs. " + guardado.getPrecioVenta() + ", Costo: Gs. " + guardado.getPrecioCosto()
        );

        return mapToDTO(guardado);
    }

    @Transactional
    public ProductoDTO actualizar(Long id, ProductoRequestDTO dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con ID " + id + " no encontrado"));

        validarUnicidad(dto.getNombre(), dto.getCodigoBarra(), id);

        StringBuilder cambios = new StringBuilder();
        if (dto.getPrecioVenta() != null && (producto.getPrecioVenta() == null || dto.getPrecioVenta().compareTo(producto.getPrecioVenta()) != 0)) {
            cambios.append("Precio venta: Gs. ").append(producto.getPrecioVenta()).append(" ➔ Gs. ").append(dto.getPrecioVenta()).append("; ");
        }
        if (dto.getPrecioCosto() != null && (producto.getPrecioCosto() == null || dto.getPrecioCosto().compareTo(producto.getPrecioCosto()) != 0)) {
            cambios.append("Costo: Gs. ").append(producto.getPrecioCosto()).append(" ➔ Gs. ").append(dto.getPrecioCosto()).append("; ");
        }
        if (dto.getStock() != null && (producto.getStock() == null || dto.getStock().compareTo(producto.getStock()) != 0)) {
            cambios.append("Stock: ").append(producto.getStock()).append(" ➔ ").append(dto.getStock()).append("; ");
        }
        if (!producto.getNombre().equalsIgnoreCase(dto.getNombre().trim())) {
            cambios.append("Nombre: ").append(producto.getNombre()).append(" ➔ ").append(dto.getNombre().trim()).append("; ");
        }
        if (cambios.length() == 0) {
            cambios.append("Modificación de datos generales del artículo maestro.");
        }

        mapearDatos(producto, dto);

        // Sincronizar proveedores si la lista no es nula
        if (dto.getProveedores() != null) {
            producto.getProveedores().clear();
            for (AsignarProveedorRequestDTO provDto : dto.getProveedores()) {
                Proveedor prov = proveedorRepository.findById(provDto.getProveedorId())
                        .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor ID " + provDto.getProveedorId() + " no encontrado"));
                producto.agregarProveedor(prov, provDto.getCodigoReferencia(), provDto.getCostoCompra(), provDto.getEsPrincipal());
            }
        }

        producto = productoRepository.save(producto);

        auditoriaService.registrar(
                "CATALOGO_MAESTRO",
                "MODIFICACION",
                producto.getId(),
                producto.getNombre(),
                cambios.toString().trim()
        );

        return mapToDTO(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con ID " + id + " no encontrado"));
        productoRepository.delete(producto);
        auditoriaService.registrar(
                "CATALOGO_MAESTRO",
                "ELIMINACION",
                id,
                producto.getNombre(),
                "Eliminado producto maestro: '" + producto.getNombre() + "'"
        );
    }

    @Transactional
    public ProductoDTO cambiarEstado(Long id, boolean activo) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con ID " + id + " no encontrado"));
        producto.setActivo(activo);
        Producto guardado = productoRepository.save(producto);
        auditoriaService.registrar(
                "CATALOGO_MAESTRO",
                "MODIFICACION",
                guardado.getId(),
                guardado.getNombre(),
                "Estado cambiado a: " + (activo ? "Activo" : "Inactivo")
        );
        return mapToDTO(guardado);
    }

    @Transactional
    public ProductoDTO asociarProveedor(Long productoId, AsignarProveedorRequestDTO dto) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con ID " + productoId + " no encontrado"));
        Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor con ID " + dto.getProveedorId() + " no encontrado"));

        // Verificar si ya está asociado
        Optional<ProductoProveedor> existente = producto.getProveedores().stream()
                .filter(pp -> pp.getProveedor().getId().equals(proveedor.getId()))
                .findFirst();

        if (existente.isPresent()) {
            ProductoProveedor pp = existente.get();
            pp.setCodigoReferencia(dto.getCodigoReferencia());
            pp.setCostoCompra(dto.getCostoCompra());
            if (Boolean.TRUE.equals(dto.getEsPrincipal())) {
                producto.getProveedores().forEach(p -> p.setEsPrincipal(false));
                pp.setEsPrincipal(true);
            }
        } else {
            producto.agregarProveedor(proveedor, dto.getCodigoReferencia(), dto.getCostoCompra(), dto.getEsPrincipal());
        }

        return mapToDTO(productoRepository.save(producto));
    }

    @Transactional
    public ProductoDTO desasociarProveedor(Long productoId, Long proveedorId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto con ID " + productoId + " no encontrado"));
        producto.removerProveedor(proveedorId);
        return mapToDTO(productoRepository.save(producto));
    }

    private void validarUnicidad(String nombre, String codigoBarra, Long idActual) {
        if (idActual == null) {
            if (productoRepository.existsByNombreIgnoreCase(nombre.trim())) {
                throw new ReglaDeNegocioException("Ya existe un producto con el nombre: " + nombre.trim());
            }
            if (codigoBarra != null && !codigoBarra.trim().isBlank()) {
                if (productoRepository.existsByCodigoBarraIgnoreCase(codigoBarra.trim())) {
                    throw new ReglaDeNegocioException("Ya existe un producto con el código de barra: " + codigoBarra.trim());
                }
            }
        } else {
            if (productoRepository.existsByNombreIgnoreCaseAndIdNot(nombre.trim(), idActual)) {
                throw new ReglaDeNegocioException("Ya existe otro producto con el nombre: " + nombre.trim());
            }
            if (codigoBarra != null && !codigoBarra.trim().isBlank()) {
                if (productoRepository.existsByCodigoBarraIgnoreCaseAndIdNot(codigoBarra.trim(), idActual)) {
                    throw new ReglaDeNegocioException("Ya existe otro producto con el código de barra: " + codigoBarra.trim());
                }
            }
        }
    }

    private void mapearDatos(Producto p, ProductoRequestDTO dto) {
        p.setNombre(dto.getNombre().trim());
        p.setCodigoBarra(dto.getCodigoBarra() != null && !dto.getCodigoBarra().isBlank() ? dto.getCodigoBarra().trim() : null);
        p.setSku(dto.getSku() != null && !dto.getSku().isBlank() ? dto.getSku().trim() : null);
        p.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);
        p.setTipoArticulo(dto.getTipoArticulo());
        p.setPrecioVenta(dto.getPrecioVenta());
        p.setPrecioCosto(dto.getPrecioCosto());
        p.setTipoIva(dto.getTipoIva());
        p.setStock(dto.getStock());
        p.setStockMinimo(dto.getStockMinimo());
        p.setUnidadMedida(dto.getUnidadMedida());
        p.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        // Atributos condicionales
        if (dto.getTipoArticulo() == TipoArticulo.ENVASE) {
            p.setMaxSabores(dto.getMaxSabores());
            p.setPesoGramosAprox(dto.getPesoGramosAprox());
        } else if (dto.getTipoArticulo() == TipoArticulo.SABOR) {
            p.setCategoriaSabor(dto.getCategoriaSabor());
            p.setAptoCeliaco(dto.getAptoCeliaco() != null ? dto.getAptoCeliaco() : true);
            p.setEsVegano(dto.getEsVegano() != null ? dto.getEsVegano() : false);
            p.setSinAzucar(dto.getSinAzucar() != null ? dto.getSinAzucar() : false);
        }
    }

    public ProductoDTO mapToDTO(Producto p) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(p.getId());
        dto.setCodigoBarra(p.getCodigoBarra());
        dto.setSku(p.getSku());
        dto.setNombre(p.getNombre());
        dto.setDescripcion(p.getDescripcion());
        dto.setTipoArticulo(p.getTipoArticulo());
        dto.setPrecioVenta(p.getPrecioVenta());
        dto.setPrecioCosto(p.getPrecioCosto());
        dto.setTipoIva(p.getTipoIva());
        dto.setStock(p.getStock());
        dto.setStockMinimo(p.getStockMinimo());
        dto.setUnidadMedida(p.getUnidadMedida());
        dto.setActivo(p.getActivo());
        dto.setMaxSabores(p.getMaxSabores());
        dto.setPesoGramosAprox(p.getPesoGramosAprox());
        dto.setCategoriaSabor(p.getCategoriaSabor());
        dto.setAptoCeliaco(p.getAptoCeliaco());
        dto.setEsVegano(p.getEsVegano());
        dto.setSinAzucar(p.getSinAzucar());
        dto.setFechaCreacion(p.getFechaCreacion());
        dto.setFechaActualizacion(p.getFechaActualizacion());

        List<ProveedorVinculadoDTO> provDtos = new ArrayList<>();
        if (p.getProveedores() != null) {
            for (ProductoProveedor pp : p.getProveedores()) {
                Proveedor prov = pp.getProveedor();
                ProveedorVinculadoDTO pvd = new ProveedorVinculadoDTO(
                        prov.getId(),
                        prov.getRuc(),
                        prov.getRazonSocial(),
                        prov.getContacto(),
                        prov.getTelefono(),
                        pp.getCodigoReferencia(),
                        pp.getCostoCompra(),
                        pp.getEsPrincipal()
                );
                provDtos.add(pvd);
                if (Boolean.TRUE.equals(pp.getEsPrincipal())) {
                    dto.setProveedorPrincipal(pvd);
                }
            }
        }
        dto.setProveedores(provDtos);
        if (dto.getProveedorPrincipal() == null && !provDtos.isEmpty()) {
            dto.setProveedorPrincipal(provDtos.get(0));
        }

        return dto;
    }
}
