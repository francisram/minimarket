package com.sanmartin.minimarketbase.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.Connection;

@Service
public class DatabaseTestService {

    @Autowired
    private DataSource dataSource; // Spring inyecta el HikariDataSource aquí automáticamente

    public boolean testConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
