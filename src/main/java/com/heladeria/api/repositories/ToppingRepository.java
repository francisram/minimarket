package com.heladeria.api.repositories;

import com.heladeria.api.entities.Topping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToppingRepository extends JpaRepository<Topping, Long> {
    List<Topping> findByDisponibleTrue();
    boolean existsByNombreIgnoreCase(String nombre);
}
