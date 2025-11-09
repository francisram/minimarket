package py.com.base.rest.controllers;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import py.com.base.rest.PostgresHealthIndicator;




@RestController
public class CustomHealthController {
    private final HealthIndicator postgresHealthIndicator;


    public CustomHealthController(PostgresHealthIndicator postgresHealthIndicator) {
        this.postgresHealthIndicator = postgresHealthIndicator;
    }

    @GetMapping("/v1/health")
    public Health health() {
        Health.Builder status = Health.up();
        status.withDetail("Postgres", postgresHealthIndicator.health());
        return status.build();
    }
}
