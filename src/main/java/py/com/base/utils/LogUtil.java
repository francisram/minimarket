package py.com.base.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {
    private static final Logger logger = LoggerFactory.getLogger(LogUtil.class);

    private LogUtil() {
        // Evitar instanciacion
    }

    public static void info(String mensaje) {
        logger.info(mensaje);
    }

    public static void error(String mensaje) {
        logger.error(mensaje);
    }

    public static void debug(String mensaje) {
        logger.debug(mensaje);
    }

    public static void warn(String mensaje) {
        logger.warn(mensaje);
    }

    public static void error(String mensaje, Throwable excepcion) {
        logger.error(mensaje, excepcion);
    }
}
