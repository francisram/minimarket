package com.heladeria.api.services;

import com.heladeria.api.entities.IntentoFallido;
import com.heladeria.api.repositories.IntentoFallidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Persistencia del limitador. Cada operación corre en su propia transacción (REQUIRES_NEW): un fallo debe
 * quedar registrado aunque la operación que lo provocó haga rollback al lanzar su error.
 */
@Service
class IntentosFallidosStore {

    private final IntentoFallidoRepository repository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager em;

    IntentosFallidosStore(IntentoFallidoRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<Instant> bloqueadoHasta(String clave) {
        return repository.findById(clave).map(IntentoFallido::getBloqueadoHasta);
    }

    /** Devuelve true si con este fallo la clave quedó bloqueada. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean registrarFallo(String clave, Instant ahora, int maxFallos, Duration ventana, Duration bloqueo) {
        IntentoFallido actual = repository.findByClaveForUpdate(clave).orElse(null);
        Instant inicio = ahora;
        int fallos = 1;
        if (actual != null && actual.getPrimerFallo() != null && ahora.isBefore(actual.getPrimerFallo().plus(ventana))) {
            inicio = actual.getPrimerFallo();
            fallos = actual.getFallos() + 1;
        }
        IntentoFallido r = actual != null ? actual : new IntentoFallido(clave);
        boolean bloqueada = fallos >= maxFallos;
        if (bloqueada) {
            r.setFallos(0);
            r.setPrimerFallo(null);
            r.setBloqueadoHasta(ahora.plus(bloqueo));
        } else {
            r.setFallos(fallos);
            r.setPrimerFallo(inicio);
            r.setBloqueadoHasta(null);
        }
        if (actual == null) {
            // persist (y no save/merge): si otro hilo insertó la misma clave a la vez debe fallar por clave duplicada
            // —LimitadorIntentos reintenta— y no convertirse en un UPDATE que pise el conteo del otro
            em.persist(r);
        }
        em.flush();
        return bloqueada;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void borrar(String clave) {
        repository.deleteById(clave);
    }
}
