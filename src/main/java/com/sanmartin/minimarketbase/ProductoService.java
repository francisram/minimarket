package com.sanmartin.minimarketbase;

import com.sanmartin.minimarketbase.entities.*;
import com.sanmartin.minimarketbase.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    
    public List<Producto> buscarPorDescripcion(String descripcion) {
        List<Producto> todosProductos = repository.findAll();
        
        return todosProductos.stream()
            .filter(producto -> producto.getDescripcion() != null &&
                              producto.getDescripcion().toLowerCase()
                                    .contains(descripcion.toLowerCase()))
            .collect(Collectors.toList());
    }
}
