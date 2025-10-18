package com.sanmartin.minimarketbase.interfaces;



import com.sanmartin.minimarketbase.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
}
