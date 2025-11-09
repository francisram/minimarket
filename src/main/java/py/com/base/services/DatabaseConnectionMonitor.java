package py.com.base.services;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import py.com.base.utils.AppConfig;

/**
 * @author frlopez
 * Clase que se encarga de monitorear la conexion a las bases de datos de manera continua.
 * Si se pierde la conexion, intenta reconectarse automaticamente.
 */
public class DatabaseConnectionMonitor {

	/**
     * Inicia un hilo que verifica de manera continua la conexion a una base de datos.
     * Si la conexion se pierde, intenta reconectar.
     *
     * @param dataSource El {@link DataSource} de la base de datos a monitorear.
     * @param dbName El nombre de la base de datos que se esta monitoreando, usado solo para fines de logging.
     */
    public static void verifyConnectionAndMonitor(DataSource dataSource, String dbName) {
        new Thread(() -> {
            boolean connected = false;
            while (true) { 
                try {
                    if (!connected) {
                        connected = isConnectionValid(dataSource);
                    } 
                    TimeUnit.MINUTES.sleep(1); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    /**
     * Intenta establecer una conexion a la base de datos usando el {@link DataSource}.
     *
     * @param dataSource El {@link DataSource} que provee la conexion a la base de datos.
     * @param dbName El nombre de la base de datos, usado para propositos de logging.
     * @return {@code true} si la conexion fue exitosa, {@code false} en caso de error.
     */
    private static boolean tryToConnect(DataSource dataSource, String dbName) {
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null && !connection.isClosed()) {
            	//LogUtil.setLogINFO(AppConfig.APP_NAME, "Conexion exitosa a la base de datos " + dbName);
                return true;
            }
        } catch (Exception e) {
        	//LogUtil.setException(AppConfig.APP_NAME, "Error al conectar a la base de datos " + dbName, e.getMessage());
        }
        return false;
    }

    /**
     * Verifica si una conexion existente sigue siendo valida y activa.
     *
     * @param dataSource El {@link DataSource} para obtener la conexion de base de datos.
     * @return {@code true} si la conexion es valida, {@code false} si la conexion ha sido cerrada o no es valida.
     */
    private static boolean isConnectionValid(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
        	//LogUtil.setException(AppConfig.APP_NAME, "Conexion no valida", e.getMessage());
            return false;
        }
    }
}
