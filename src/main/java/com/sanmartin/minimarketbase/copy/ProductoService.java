package com.sanmartin.minimarketbase;

package com.sanmartin.minimarketbase.copy;

import com.supermercado.model.Producto;
import com.supermercado.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository repository;

    public Page<Producto> listarPaginado(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("idProducto").descending());
        return repository.findAll(pageable);
    }

    public Optional<Producto> obtenerPorId(Integer id) {
        return repository.findById(id);
    }

    public Producto crear(Producto producto) {
        return repository.save(producto);
    }

    public Producto actualizar(Integer id, Producto producto) {
        producto.setIdProducto(id);
        return repository.save(producto);
    }

    public void eliminar(Integer id) {
        repository.deleteById(id);
    }
}
