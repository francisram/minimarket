package py.com.base.rest;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class PostgresHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;
    
    @Value("${app.datasource.postgres2.url}")
    private String datasourceUrl;

    public PostgresHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
            	String dbIp = extractIpFromUrl(datasourceUrl);
            	return Health.up().withDetail("databaseIp", dbIp).build();
                //return Health.up().build();
            } else {
                return Health.down().withDetail("Error", "Connection is not valid").build();
            }
        } catch (SQLException e) {
            return Health.down(e).build();
        }
    }
    
    
    private String extractIpFromUrl(String url) {
        try {
            String[] parts = url.split("//")[1].split(":");
            return parts[0]; 
        } catch (Exception e) {
            return "Unknown";
        }
    }
}
