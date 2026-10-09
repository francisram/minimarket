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
                new Pagina(null, "stock", "Control de Stock", "/stock", null, "warehouse", 45),
                new Pagina(null, "pedidos", "Punto de Venta / Pedidos", "/pedidos", null, "point_of_sale", 50),
                new Pagina(null, "usuarios", "Usuarios", "/usuarios", null, "people", 60),
                new Pagina(null, "roles", "Roles y Permisos", "/roles", null, "security", 70),
                new Pagina(null, "licencia", "Licenciamiento", "/licencia", null, "verified", 80),
                new Pagina(null, "impresoras", "Impresoras", "/impresoras", null, "print", 90)
        );

        for (Pagina p : paginasCatalogo) {
            if (paginaRepository.findByClave(p.getClave()).isEmpty()) {
                paginaRepository.save(p);
            }
        }
    }

    private void sembrarPlanesYLicencia() {
        // Plan Basico
        PlanLicencia basico = planLicenciaRepository.findByNombre("Basico").orElseGet(() -> {
            PlanLicencia p = new PlanLicencia();
            p.setNombre("Basico");
            p.setPaginas(obtenerPaginasPorClaves("dashboard", "sabores", "presentaciones", "pedidos"));
            return planLicenciaRepository.save(p);
        });

        // Plan Estandar
        planLicenciaRepository.findByNombre("Estandar").orElseGet(() -> {
            PlanLicencia p = new PlanLicencia();
            p.setNombre("Estandar");
            p.setPaginas(obtenerPaginasPorClaves("dashboard", "sabores", "presentaciones", "toppings", "productos", "stock", "pedidos", "impresoras"));
            return planLicenciaRepository.save(p);
        });

        // Plan Premium
        PlanLicencia premium = planLicenciaRepository.findByNombre("Premium").orElseGet(() -> {
            PlanLicencia p = new PlanLicencia();
            p.setNombre("Premium");
            p.setPaginas(obtenerPaginasPorClaves("dashboard", "sabores", "presentaciones", "toppings", "productos", "stock", "pedidos", "usuarios", "roles", "licencia", "impresoras"));
            return planLicenciaRepository.save(p);
        });

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
            r.setPaginas(licenciaService.obtenerPaginasLicenciadas());
            return rolRepository.save(r);
        });

        // OWNER recibe todas las paginas licenciadas
        Rol owner = rolRepository.findByNombreRol("OWNER").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombreRol("OWNER");
            r.setPaginas(licenciaService.obtenerPaginasLicenciadas());
            return rolRepository.save(r);
        });

        // CAJERO para operacion de mostrador
        rolRepository.findByNombreRol("CAJERO").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombreRol("CAJERO");
            r.setPaginas(obtenerPaginasPorClaves("dashboard", "pedidos"));
            return rolRepository.save(r);
        });

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
