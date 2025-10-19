package com.sanmartin.minimarketbase;

import com.sanmartin.minimarketbase.dto.VentaRequest;
import com.sanmartin.minimarketbase.entities.*;
import com.sanmartin.minimarketbase.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;
    
    @Autowired
    private DetalleVentaRepository detalleVentaRepository;
    
    @Autowired
    private InventarioRepository inventarioRepository;
    
    @Autowired
    private InventarioMovimientoRepository inventarioMovimientoRepository;
    
    @Autowired
    private ProductoRepository productoRepository;

    @Transactional
    public Venta procesarVenta(VentaRequest ventaRequest) {
        // 1. Validar stock disponible
        validarStockDisponible(ventaRequest.getDetalles());
        
        // 2. Crear la venta
        Venta venta = new Venta();
        venta.setIdCliente(ventaRequest.getIdCliente());
        venta.setMetodoPago(ventaRequest.getMetodoPago());
        venta.setEstado("COMPLETADA");
        
        // 3. Procesar detalles y calcular total
        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal totalVenta = BigDecimal.ZERO;
        
        for (VentaRequest.DetalleVentaRequest detalleReq : ventaRequest.getDetalles()) {
            // Verificar que el producto existe
            Optional<Producto> productoOpt = productoRepository.findById(detalleReq.getIdProducto());
            if (productoOpt.isEmpty()) {
                throw new RuntimeException("Producto no encontrado: " + detalleReq.getIdProducto());
            }
            
            Producto producto = productoOpt.get();
            
            // Crear detalle de venta
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setIdProducto(detalleReq.getIdProducto());
            detalle.setCantidad(detalleReq.getCantidad());
            detalle.setPrecioUnitario(detalleReq.getPrecioUnitario());
            
            // Calcular subtotal
            BigDecimal subtotal = detalleReq.getPrecioUnitario()
                .multiply(BigDecimal.valueOf(detalleReq.getCantidad()));
            
            detalle.setSubtotal(subtotal);
            detalles.add(detalle);
            totalVenta = totalVenta.add(subtotal);
            
            // 4. Actualizar inventario
            actualizarInventario(detalleReq.getIdProducto(), detalleReq.getCantidad(), ventaRequest.getIdUsuario());
            
            // 5. Registrar movimiento de inventario
            registrarMovimientoInventario(
                detalleReq.getIdProducto(), 
                detalleReq.getCantidad(), 
                "VENTA", 
                "Venta #" + ventaRequest.getIdCliente(),
                ventaRequest.getIdUsuario()
            );
        }
        
        venta.setTotal(totalVenta);
        venta.setDetalles(detalles);
        
        // Guardar venta y detalles
        Venta ventaGuardada = ventaRepository.save(venta);
        
        // Guardar detalles con la venta ya persistida
        for (DetalleVenta detalle : detalles) {
            detalle.setVenta(ventaGuardada);
            detalleVentaRepository.save(detalle);
        }
        
        return ventaGuardada;
    }
    
    private void validarStockDisponible(List<VentaRequest.DetalleVentaRequest> detalles) {
        for (VentaRequest.DetalleVentaRequest detalle : detalles) {
            // ✅ Usar el método manual
            Optional<Inventario> inventarioOpt = buscarInventarioPorProducto(detalle.getIdProducto());
            
            if (inventarioOpt.isPresent()) {
                Inventario inventario = inventarioOpt.get();
                if (inventario.getStockDisponible() < detalle.getCantidad()) {
                    throw new RuntimeException(
                        "Stock insuficiente para producto ID: " + detalle.getIdProducto() + 
                        ". Stock disponible: " + inventario.getStockDisponible()
                    );
                }
            } else {
                throw new RuntimeException("Producto sin registro en inventario: " + detalle.getIdProducto());
            }
        }
    }

    private void actualizarInventario(Integer idProducto, Integer cantidadVendida, Integer idUsuario) {
        // ✅ Usar el método manual
        Optional<Inventario> inventarioOpt = buscarInventarioPorProducto(idProducto);
        
        if (inventarioOpt.isPresent()) {
            Inventario inventario = inventarioOpt.get();
            inventario.setStockActual(inventario.getStockActual() - cantidadVendida);
            inventarioRepository.save(inventario);
        } else {
            // Si no existe registro, crear uno
            Inventario inventario = new Inventario();
            inventario.setIdProducto(idProducto);
            inventario.setStockActual(-cantidadVendida); // Negativo porque es venta
            inventarioRepository.save(inventario);
        }
    }
    
    private Optional<Inventario> buscarInventarioPorProducto(Integer idProducto) {
        // Buscar manualmente en todos los registros de inventario
        List<Inventario> todosInventarios = inventarioRepository.findAll();
        
        return todosInventarios.stream()
                .filter(inv -> inv.getIdProducto().equals(idProducto))
                .findFirst();
    }
    
    private void registrarMovimientoInventario(Integer idProducto, Integer cantidad, String tipo, 
                                             String motivo, Integer idUsuario) {
        InventarioMovimiento movimiento = new InventarioMovimiento();
        movimiento.setIdProducto(idProducto);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(-cantidad); // Negativo porque es salida
        movimiento.setMotivo(motivo);
        movimiento.setIdUsuario(idUsuario);
        
        inventarioMovimientoRepository.save(movimiento);
    }

	public Venta cancelarVenta(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	public Venta obtenerVentaPorId(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Venta> obtenerTodasLasVentas() {
		// TODO Auto-generated method stub
		return null;
	}
}