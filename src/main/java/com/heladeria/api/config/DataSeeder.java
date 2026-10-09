package com.heladeria.api.config;

import com.heladeria.api.entities.*;
import com.heladeria.api.repositories.*;
import com.heladeria.api.services.LicenciaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final PaginaRepository paginaRepository;
    private final PlanLicenciaRepository planLicenciaRepository;
    private final LicenciaRepository licenciaRepository;
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final LicenciaService licenciaService;
    private final InstitucionRepository institucionRepository;

    private final String defaultAdminPassword;
    private final String defaultOwnerPassword;

    public DataSeeder(PaginaRepository paginaRepository,
                      PlanLicenciaRepository planLicenciaRepository,
                      LicenciaRepository licenciaRepository,
                      RolRepository rolRepository,
                      UsuarioRepository usuarioRepository,
                      PasswordEncoder passwordEncoder,
                      LicenciaService licenciaService,
                      InstitucionRepository institucionRepository,
                      @Value("${app.security.default-admin-password:admin123}") String defaultAdminPassword,
                      @Value("${app.security.default-owner-password:owner123}") String defaultOwnerPassword) {
        this.paginaRepository = paginaRepository;
        this.planLicenciaRepository = planLicenciaRepository;
        this.licenciaRepository = licenciaRepository;
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.licenciaService = licenciaService;
        this.institucionRepository = institucionRepository;
        this.defaultAdminPassword = defaultAdminPassword;
        this.defaultOwnerPassword = defaultOwnerPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        sembrarPaginas();
        sembrarPlanesYLicencia();
        sembrarRolesYUsuarios();
        sembrarInstitucion();
    }

    private void sembrarPaginas() {
        List<Pagina> paginasCatalogo = List.of(
                new Pagina(null, "dashboard", "Dashboard", "/dashboard", null, "dashboard", 5),
                new Pagina(null, "sabores", "Sabores", "/sabores", null, "icecream", 10),
                new Pagina(null, "presentaciones", "Presentaciones", "/presentaciones", null, "format_shapes", 20),
                new Pagina(null, "toppings", "Toppings y Agregados", "/toppings", null, "cookie", 30),
                new Pagina(null, "productos", "Productos y Bebidas", "/productos", null, "inventory_2", 40),
                new Pagina(null, "caja", "Control de Caja", "/caja", null, "payments", 45),
                new Pagina(null, "pedidos", "Punto de Venta / Pedidos", "/pedidos", null, "point_of_sale", 50),
                new Pagina(null, "stock", "Control de Stock", "/stock", null, "warehouse", 55),
                new Pagina(null, "usuarios", "Usuarios", "/usuarios", null, "people", 60),
                new Pagina(null, "roles", "Roles y Permisos", "/roles", null, "security", 70),
                new Pagina(null, "impresoras", "Impresoras ESC/POS", "/impresoras", null, "printer", 75),
                new Pagina(null, "licencia", "Licenciamiento", "/licencia", null, "verified", 80)
        );

        for (Pagina p : paginasCatalogo) {
            Optional<Pagina> existente = paginaRepository.findByClave(p.getClave());
            if (existente.isEmpty()) {
                paginaRepository.save(p);
            } else {
                Pagina guardada = existente.get();
                guardada.setNombre(p.getNombre());
                guardada.setUrl(p.getUrl());
                guardada.setIcono(p.getIcono());
                guardada.setOrden(p.getOrden());
                paginaRepository.save(guardada);
            }
        }
    }

    private void sembrarPlanesYLicencia() {
        // Plan Basico
        PlanLicencia basico = planLicenciaRepository.findByNombre("Basico").orElseGet(() -> {
            PlanLicencia p = new PlanLicencia();
            p.setNombre("Basico");
            return planLicenciaRepository.save(p);
        });
        basico.setPaginas(obtenerPaginasPorClaves("dashboard", "sabores", "presentaciones", "pedidos", "caja"));
        planLicenciaRepository.save(basico);

        // Plan Estandar
        PlanLicencia estandar = planLicenciaRepository.findByNombre("Estandar").orElseGet(() -> {
            PlanLicencia p = new PlanLicencia();
            p.setNombre("Estandar");
            return planLicenciaRepository.save(p);
        });
        estandar.setPaginas(obtenerPaginasPorClaves("dashboard", "sabores", "presentaciones", "toppings", "productos", "stock", "pedidos", "impresoras", "caja"));
        planLicenciaRepository.save(estandar);

        // Plan Premium
        PlanLicencia premium = planLicenciaRepository.findByNombre("Premium").orElseGet(() -> {
            PlanLicencia p = new PlanLicencia();
            p.setNombre("Premium");
            return planLicenciaRepository.save(p);
        });
        premium.setPaginas(obtenerPaginasPorClaves("dashboard", "sabores", "presentaciones", "toppings", "productos", "stock", "pedidos", "usuarios", "roles", "impresoras", "licencia", "caja"));
        planLicenciaRepository.save(premium);

        // Licencia activa de esta instalacion (id=1)
        if (licenciaRepository.findById(1L).isEmpty()) {
            Licencia lic = new Licencia(1L, premium, LocalDate.now().plusYears(1));
            licenciaRepository.save(lic);
            log.info("[SEED] Licencia activa inicial sembrada con plan Premium.");
        }
    }

    private void sembrarRolesYUsuarios() {
        // ADMIN recibe todas las paginas hoy licenciadas de la instalacion
        Rol admin = rolRepository.findByNombreRol("ADMIN").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombreRol("ADMIN");
            return rolRepository.save(r);
        });
        admin.setPaginas(licenciaService.obtenerPaginasLicenciadas());
        rolRepository.save(admin);

        // OWNER recibe todas las paginas licenciadas
        Rol owner = rolRepository.findByNombreRol("OWNER").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombreRol("OWNER");
            return rolRepository.save(r);
        });
        owner.setPaginas(licenciaService.obtenerPaginasLicenciadas());
        rolRepository.save(owner);

        // CAJERO para operacion de mostrador
        Rol cajero = rolRepository.findByNombreRol("CAJERO").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombreRol("CAJERO");
            return rolRepository.save(r);
        });
        cajero.setPaginas(obtenerPaginasPorClaves("dashboard", "pedidos", "caja"));
        rolRepository.save(cajero);

        // Usuarios iniciales
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            Usuario u = new Usuario();
            u.setUsername("admin");
            u.setPasswordHash(passwordEncoder.encode(defaultAdminPassword));
            u.setRol(admin);
            u.setEstado(true);
            u.setPasswordNuncaExpira(true);
            u.setDebeCambiarPassword(false);
            usuarioRepository.save(u);
            log.info("[SEED] Usuario de desarrollo 'admin' creado. Recuerde cambiar credenciales para producción.");
        }

        if (usuarioRepository.findByUsername("owner").isEmpty()) {
            Usuario u = new Usuario();
            u.setUsername("owner");
            u.setPasswordHash(passwordEncoder.encode(defaultOwnerPassword));
            u.setRol(owner);
            u.setEstado(true);
            u.setPasswordNuncaExpira(true);
            u.setDebeCambiarPassword(false);
            usuarioRepository.save(u);
            log.info("[SEED] Usuario de desarrollo 'owner' creado. Recuerde cambiar credenciales para producción.");
        }
    }

    private Set<Pagina> obtenerPaginasPorClaves(String... claves) {
        Set<Pagina> set = new HashSet<>();
        for (String c : claves) {
            paginaRepository.findByClave(c).ifPresent(set::add);
        }
        return set;
    }

    private void sembrarInstitucion() {
        if (institucionRepository.findById(1L).isEmpty()) {
            Institucion inst = new Institucion(1L, "Heladería Artesanal", null);
            institucionRepository.save(inst);
            log.info("[SEED] Institución inicial sembrada.");
        }
    }
}
