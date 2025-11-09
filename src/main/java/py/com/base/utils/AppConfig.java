package py.com.base.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
//import py.com.bepsa.logs.LogUtil;

/**
 * La clase {@code AppConfig} se encarga de cargar y almacenar las configuraciones de la
 * aplicacion desde un archivo `application.properties` localizado en el classpath.
 * 
 * <p>Las propiedades se cargan al iniciar la clase a traves de un bloque estatico, 
 * donde se extraen las propiedades especificas de configuracion que la aplicacion requiere.
 * 
 * <p>Si el archivo de configuracion no se encuentra o no se puede cargar, se lanza un 
 * {@code ExceptionInInitializerError}.
 * 
 * <p>Las propiedades cargadas son:
 * <ul>
 *   <li>{@code app.name}: El nombre de la aplicacion.</li>
 *   <li>{@code confi.cantidad_registros}: La cantidad de registros a procesar.</li>
 *   <li>{@code confi.sleep}: Tiempo de espera entre operaciones.</li>
 * </ul>
 * 
 * <p>Esta clase es inmutable ya que todas sus propiedades son finales y estaticas.
 * 
 * @author frlopez
 * @version 1.0
 */
public class AppConfig {
	 /**
     * El nombre de la aplicacion, cargado desde la propiedad {@code app.name}.
     */
	public static final String APP_NAME;
    /**
     * La cantidad de registros a procesar, cargado desde la propiedad {@code confi.cantidad_registros}.
     */
	public static final String CANTIDAD_REGISTROS;
	  /**
     * El tiempo de espera entre operaciones, cargado desde la propiedad {@code confi.sleep}.
     */
	public static final String SLEEP;
	public static final String CORREOS;
	public static final String EMAILFROM;
	public static final String ENCRYPTEDUSERNAMEPOSTGRES2;
	public static final String ENCRYPTEDPASSWORDPOSTGRES2;
	public static final int MAXRETRIES;
	public static final Long RETRYDELAYMILLIS;
	public static final String CONSUMERDESTINATION;
	public static final String PUSHAPI;
	public static final int MAXTHREADS;
	public static final int MAXPULLSIZEPOSTGRES;
	public static final Long TIMEOUTPOSTGRES;
	public static final Long REQUEST_TIMEOUT_POSTGRES;
	public static final Long MAX_LIFE_POSTGRES;
	public static final boolean PTCOMERCIO;
	public static final String EMAILUSERNAME;
	public static final String EMAILUSERPASS;
	public static final String EMAILDOMAIN;
	public static final String EMAILPORT;
	public static final boolean CONFFROMPROP;
	public static final String REPROCESAR;
	public static final String CLIENTEREPROCESO;
	public static final Long TIEMPODEESPERAREPROCESO;
	public static final Long TIEMPODEESPERAREPROCESOFALLIDO;
	public static final String NIVEL_LOG;
	

	static {

		Properties properties = new Properties();
		
		String userDir = System.getProperty("user.dir");
        String propertiesFilePath = userDir + "/application.properties";
		try (InputStream input = new FileInputStream(propertiesFilePath)) {
		//	try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
			properties.load(input);
			APP_NAME = properties.getProperty("app.name");
			CANTIDAD_REGISTROS = properties.getProperty("confi.cantidad_registros");
			SLEEP = properties.getProperty("confi.sleep");
			CORREOS = properties.getProperty("email");;
			EMAILFROM = properties.getProperty("emailFrom");
			ENCRYPTEDUSERNAMEPOSTGRES2 = properties.getProperty("app.datasource.postgres2.username");
			ENCRYPTEDPASSWORDPOSTGRES2 = properties.getProperty("app.datasource.postgres2.password");
			MAXRETRIES = Integer.parseInt(properties.getProperty("maxRetries")) ;
			RETRYDELAYMILLIS = Long.parseLong(properties.getProperty("retryDelayMillis"))   ;
			CONSUMERDESTINATION = properties.getProperty("consumerdestination");
			PUSHAPI = properties.getProperty("envio.modo2");
			MAXTHREADS = Integer.parseInt(properties.getProperty("maxThreads")) ;
			MAXPULLSIZEPOSTGRES = Integer.parseInt(properties.getProperty("app.datasource.postgres2.hikari.maximum-pool-size")) ;
			TIMEOUTPOSTGRES = Long.parseLong(properties.getProperty("app.datasource.postgres2.hikari.idle-timeout")) ;
			REQUEST_TIMEOUT_POSTGRES = Long.parseLong(properties.getProperty("app.datasource.postgres2.hikari.connection-timeout")) ;
			MAX_LIFE_POSTGRES = Long.parseLong(properties.getProperty("app.datasource.postgres2.hikari.max-lifetime")) ;
			PTCOMERCIO = Boolean.parseBoolean(properties.getProperty("ptcomerciosincronizzer"));
			EMAILUSERNAME = properties.getProperty("email.username");
			EMAILUSERPASS = properties.getProperty("email.password");
			EMAILDOMAIN = properties.getProperty("email.host");
			EMAILPORT = properties.getProperty("email.port");
			REPROCESAR = properties.getProperty("reprocess.queue");
			CLIENTEREPROCESO = properties.getProperty("client.queue.name");
			CONFFROMPROP = Boolean.parseBoolean(properties.getProperty("email.conf.from.file"));
			TIEMPODEESPERAREPROCESO = Long.parseLong(properties.getProperty("reprocess.waittime"));
			TIEMPODEESPERAREPROCESOFALLIDO = Long.parseLong(properties.getProperty("reprocess.failedreprocess.waittime"));
			NIVEL_LOG = properties.getProperty("seq.level");
			LoggerUtil.setDetallesHabilitados(Boolean.parseBoolean(properties.getProperty("log.level.detailed")));
			LoggerUtil.habilitarSeq(Boolean.parseBoolean(properties.getProperty("seq_loggin")));
			if(Boolean.parseBoolean(properties.getProperty("seq_loggin"))) {
				LoggerUtil.importante("Registro en SEQ HABILITADO");
			}else {
				LoggerUtil.importante("Registro en SEQ NO HABILITADO");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new ExceptionInInitializerError("Error I/O al cargar las propiedades: " + ex.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new ExceptionInInitializerError("Error al cargar las propiedades: " + e.getMessage());
		}
	}

}
