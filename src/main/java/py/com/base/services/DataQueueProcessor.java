package py.com.base.services;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;
import py.com.base.dto.TBDAUVDto;
import py.com.base.utils.AppConfig;
import py.com.base.utils.GeneradorDeSecuencia;
import py.com.base.utils.LogUtil;
import py.com.base.utils.LoggerUtil;

/**
 * @author frlopez Servicio que procesa los datos del DataQueue de AS400,
 *         realizando diversas operaciones de sincronizacion y actualizacion en
 *         bases de datos AS400 y PostgreSQL.
 * 
 *         Esta clase es gestionada por Spring mediante la anotacion
 *         {@code @Service}.
 */

@Service
public class DataQueueProcessor {

	private DataSource postgresDataSourceDestino;
	private EmailService emailService;
	private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private final int maxRetries = AppConfig.MAXRETRIES;
	int reintentos = 0;

	/**
	 * Constructor que inicializa los DataSources.
	 * 
	 * @param as400DataSource           DataSource para la base de datos AS400.
	 * @param postgresDataSource        DataSource para la base de datos PostgreSQL
	 *                                  (origen).
	 * @param postgresDataSourceDestino DataSource para la base de datos PostgreSQL
	 *                                  (destino).
	 */
	public DataQueueProcessor(DataSource postgresDataSourceDestino) {
		this.postgresDataSourceDestino = postgresDataSourceDestino;

	}

	/**
	 * Metodo principal que procesa los datos del queue de AS400 en un hilo
	 * separado. Realiza diversas operaciones como la recuperacion de datos,
	 * conversion y sincronizacion con sistemas externos.
	 */
	public void procesarDatosDelQueue() throws InterruptedException {
		while (true) {
			try (Connection connection = postgresDataSourceDestino.getConnection()) {
				String parmDataqueue = obtenerParmDataqueue();
				if (Objects.nonNull(parmDataqueue) && !parmDataqueue.isEmpty()) {
					LocalDateTime fechaYHoraActual = LocalDateTime.now();
					String fechaYHoraFormateada = fechaYHoraActual.format(FORMATO_FECHA_HORA);
					int secuencia = GeneradorDeSecuencia.obtenerSiguiente();
					LoggerUtil.importante("queue a procesar: " + parmDataqueue + "| secuencia : " + secuencia + "|"	+ fechaYHoraFormateada);
					
					String[] dividir = parmDataqueue.split("\\|");
					String audrnbParm = dividir[0].trim();
					String audrfcParm = dividir[1].trim();
					String audfchtParm = dividir[2].trim();

					String pattern = "^[0-9]{1,}$";
					if (audrnbParm.matches(pattern)) {
						try {
							String currentDir = System.getProperty("user.dir");
							File directory = new File(currentDir, "busdatosv2Folder/dto/");
							URL[] urls = { directory.toURI().toURL() };

							try (URLClassLoader classLoader = new URLClassLoader(urls, null)) {

								List<Object> results = recuperaRegistros(audrnbParm, audrfcParm, audfchtParm);
								if (Objects.nonNull(results)) {

									this.actualizarTbdaudConEstadoE(connection, audrnbParm, audrfcParm, audfchtParm,
											parmDataqueue, "| secuencia : " + secuencia + "|" + fechaYHoraFormateada);
									for (Object dt : results) {

										if (AppConfig.PTCOMERCIO) {
											PtComercioSynchronizer ptc = new PtComercioSynchronizer(postgresDataSourceDestino);

											CompletableFuture<Void> ptcFuture = ptc
													.sincronizarPtComercio(dt, parmDataqueue,"| secuencia : " + secuencia)
													.thenAccept(dto -> LoggerUtil.detalle("Sincronizacion Portal Comercio Completada para operacion id: " + dto.getOperacion_id()))
													.exceptionally(ex -> {
														String body = "Se produjo un error: " + ex.getMessage()
																+ "\nDetalles:\n" + Arrays.toString(ex.getStackTrace());
														ex.printStackTrace();
														return null;
													});

										}

										EntidadEmisoraSynchronizer ees = new EntidadEmisoraSynchronizer(
												postgresDataSourceDestino);
										CompletableFuture<Void> eesFuture = ees
												.sincronizarEntidadEmisora(dt, parmDataqueue,"| secuencia : " + secuencia)
												.thenRun(() -> LoggerUtil.detalle("Sincronizacion Entidad Emisora completada con exito"))
												.exceptionally(ex -> {
													String body = "Se produjo un error: " + ex.getMessage()
															+ "\nDetalles:\n" + Arrays.toString(ex.getStackTrace());
													LogUtil.info(String.format(	"Error sincronizarEntidadEmisora DataQueueProcessor  QUEUE: "+ parmDataqueue,	body));
													ex.printStackTrace();
													return null;
												});

									}

								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					} else {
						actualizarTbdaudConEstadoC(connection, audrnbParm, audrfcParm, audfchtParm, parmDataqueue,
								"| secuencia : " + secuencia + "|" + fechaYHoraFormateada);
					}
				} else {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}

			} catch (SQLException e) {
				LogUtil.error("Error de conexion con la base de datos AS400. Reintento " + reintentos + " de " + maxRetries,e);
				reintentos++;
				if (reintentos == maxRetries) {
					String head = "Error al intentar conectar con db2";
					String body = "Se alcanzo el numero maximo de reintentos. Esperando 30s antes de continuar...";
					LogUtil.error(body);
				//	py.com.bepsa.logs.LogUtil.crearSeq(AppConfig.SEQ_URL);
					LoggerUtil.detalle(body);
					this.enviarCorreo(head, body);
			//		py.com.bepsa.logs.LogUtil.closeAndFlush();
					Thread.sleep(30000);
					reintentos = 0;
					e.printStackTrace();
				} else {
					Thread.sleep(30000);
				}
			} catch (Exception e) {
				LogUtil.error("Error inesperado: " + e.getMessage());
			}

		}

	}

	/**
	 * Obtiene los datos desde la cola (queue) de AS400.
	 * 
	 * @return String con los datos de la cola.
	 */
	private String obtenerParmDataqueue() {
		String parmDataqueue = "";
		try (Connection connection = postgresDataSourceDestino.getConnection();
				CallableStatement callableStatement = connection.prepareCall("{CALL GXFINPGM.SPFIN002(?)}")) {
			callableStatement.setString(1, parmDataqueue);
			callableStatement.registerOutParameter(1, java.sql.Types.VARCHAR);
			callableStatement.setQueryTimeout(30);
			callableStatement.execute();
			parmDataqueue = callableStatement.getString(1).trim();
		} catch (SQLException e) {
			String body = "Se produjo un error: " + e.getMessage() + "\nDetalles:\n"
					+ Arrays.toString(e.getStackTrace());
			LogUtil.error(String.format("conexion para obtener queue no disponible " + parmDataqueue, body));
			e.printStackTrace();
		} catch (Exception e) {
		}
		return parmDataqueue;
	}

	/**
	 * Recupera los registros de la vista TBDauv para ser procesados.
	 * 
	 * @param rrnb        Numero de referencia.
	 * @param audrfcParm  Fecha de transaccion.
	 * @param audfchtParm Fecha adicional.
	 * @return Lista de objetos {@link TBDAUVDto}.
	 */
	private List<Object> recuperaRegistros(String rrnb, String audrfcParm, String audfchtParm) {
		try {
			String currentDir = System.getProperty("user.dir");
			File directory = new File(currentDir, "busdatosv2Folder/dto/");
			URL[] urls = { directory.toURI().toURL() };
			try (URLClassLoader classLoader = new URLClassLoader(urls, null)) {
				Class<?> tbdauvDtoClass = classLoader.loadClass("py.com.base.dto.TBDAUVDtoDynamic");
				List<Object> tbdaResults = findByFields(rrnb, audrfcParm, audfchtParm, tbdauvDtoClass);
				return tbdaResults;
			}
		} catch (Exception e) {
			String body = "Se produjo un error: " + e.getMessage() + "\nDetalles:\n"
					+ Arrays.toString(e.getStackTrace());
			LogUtil.error(String.format("Error al recuperar registros de la vista de manera dinamica", body));
			return Collections.emptyList();
		}
	}

	/**
	 * Actualiza el estado del registro en la tabla TBDAUD a 'C' indicando que el
	 * proceso ha concluido.
	 * 
	 * @param audrnb     Numero de referencia.
	 * @param audtrxfchc Fecha de transaccion.
	 * @param audfcht    Fecha adicional.
	 */
	private void actualizarTbdaudConEstadoC(Connection connection, String audrnb, String audtrxfchc, String audfcht,
			String queueParam, String secuencia) {

		String queryString = "UPDATE GXFINDTA.TBDAUD SET AUDESTPOG = 'C' WHERE TRIM(AUDRNB) = ? AND AUDTRXFCHF = ? AND RTRIM(AUDFCHT) = ?";

		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
			String fecha = audtrxfchc.trim().substring(0, 8);
			preparedStatement.setString(1, audrnb);
			preparedStatement.setString(2, fecha);
			preparedStatement.setString(3, audfcht);
			int rowsUpdated = preparedStatement.executeUpdate();

			if (rowsUpdated > 0) {
				LoggerUtil.importante("Registro actualizado exitosamente a  estado 'C' ");
				return;
			} else {
				LoggerUtil.importante( "No se encontro ningun registro para actualizar ");
				ReProcess.enviarAReproceso(AppConfig.REPROCESAR, queueParam, secuencia, "C","inicial");
				return;
			}

		} catch (SQLException e) {
			String body = "Se produjo un error: " + e.getMessage() + "\nDetalles:\n"+ Arrays.toString(e.getStackTrace());
			LoggerUtil.detalle(body);
			ReProcess.enviarAReproceso(AppConfig.REPROCESAR, queueParam, secuencia, "C","inicial");
			LogUtil.error(String.format("Error QUEUE PROCESSOR  QUEUE: " + audrnb + "|" + audtrxfchc + "|" + audfcht,
					"Conexion a la base de datos para actualizar a estado C no disponible. " + queueParam + " |"
							+ body));
		}
	}

	/**
	 * Actualiza el estado del registro en la tabla TBDAUD a 'E' indicando que el
	 * registro ha sido procesado exitosamente.
	 * 
	 * @param audrnb     Numero de referencia.
	 * @param audtrxfchc Fecha de transaccion.
	 * @param audfcht    Fecha adicional.
	 */
	private void actualizarTbdaudConEstadoE(Connection connection, String audrnb, String audtrxfchc, String audfcht,
			String queueParam, String secuencia) {
		String queryString = "UPDATE GXFINDTA.TBDAUD SET AUDESTPOG = 'E' WHERE TRIM(AUDRNB) = ? AND AUDTRXFCHF = ? AND RTRIM(AUDFCHT) = ?";
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
			String fecha = audtrxfchc.trim().substring(0, 8);
			preparedStatement.setString(1, audrnb);
			preparedStatement.setString(2, fecha);
			preparedStatement.setString(3, audfcht);
			int rowsUpdated = preparedStatement.executeUpdate();

			if (rowsUpdated > 0) {
				LoggerUtil.importante("Registro actualizado exitosamente a  estado 'E' " + queueParam + secuencia);
				return;
			} else {
				LogUtil.info(String.format("Error QUEUE PROCESSOR  QUEUE: " + audrnb + "|" + audtrxfchc + "|" + audfcht,"No se encontro ningun registro para actualizar a E en inicial: " + queueParam));
				ReProcess.enviarAReproceso(AppConfig.REPROCESAR, queueParam, secuencia, "E","inicial");
				return;
			}

		} catch (SQLException e) {
			LoggerUtil.importante("Conexion a la base de datos para actualizar a estado E no disponible."	+ Arrays.toString(e.getStackTrace()));
			String body = "Se produjo un error: " + e.getMessage() + "\nDetalles:\n" + Arrays.toString(e.getStackTrace());
			ReProcess.enviarAReproceso(AppConfig.REPROCESAR, queueParam, secuencia, "E","inicial");
			LogUtil.error(String.format("Error QUEUE PROCESSOR  QUEUE: " + audrnb + "|" + audtrxfchc + "|" + audfcht,"Conexion a la base de datos para actualizar a estado E no disponible. " + body));

		}
	}

	/**
	 * Busca registros en la vista TBDauv por campos especificos.
	 * 
	 * @param rrnb        Numero de referencia.
	 * @param audrfcParm  Fecha de transaccion.
	 * @param audfchtParm Fecha adicional.
	 * @return Lista de objetos {@link TBDAUVDto}.
	 */
	private List<Object> findByFields(String rrnb, String audrfcParm, String audfchtParm, Class<?> dtoClass) {
		List<Object> results = new ArrayList<>();
		String queryString = "SELECT * FROM GXFINDTA.view_bus_datos e WHERE e.RRNBOLETA = ? AND SUBSTR(e.FECHATRANSACCION, 1, 8) = ? AND RTRIM(e.op_audfcht) = ?";

		try (Connection connection = postgresDataSourceDestino.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			preparedStatement.setString(1, rrnb);
			preparedStatement.setString(2, audrfcParm);
			preparedStatement.setString(3, audfchtParm);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				results = mapResultSetToDto(resultSet, dtoClass);
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}

		return results;
	}

	/**
	 * Mapeo campos dinamicamente
	 * 
	 * @param <T>
	 * @param resultSet
	 * @param dtoClass
	 * @return
	 * @throws SQLException
	 */
	private List<Object> mapResultSetToDto(ResultSet resultSet, Class<?> dtoClass) throws SQLException {
		List<Object> dtoList = new ArrayList<>();

		try {
			while (resultSet.next()) {

				Object dtoInstance = dtoClass.getDeclaredConstructor().newInstance();

				ResultSetMetaData metaData = resultSet.getMetaData();
				int columnCount = metaData.getColumnCount();

				for (int i = 1; i <= columnCount; i++) {
					String columnName = metaData.getColumnName(i);
					Object columnValue = resultSet.getObject(i);

					Field field;
					try {
						field = dtoClass.getDeclaredField(columnName.toLowerCase());
					} catch (NoSuchFieldException e) {
						continue;
					}
					field.setAccessible(true);

					if (columnValue != null && !field.getType().isAssignableFrom(columnValue.getClass())) {

						columnValue = convertValue(columnValue, field.getType());
					}

					field.set(dtoInstance, columnValue);
				}
				dtoList.add(dtoInstance);
			}
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException
				| NoSuchMethodException e) {
			String body = "Se produjo un error: " + e.getMessage() + "\nDetalles:\n"
					+ Arrays.toString(e.getStackTrace());
			// this.enviarCorreo("Error al mapear ResultSet al DTO ", body);
			LogUtil.error(String.format("Error al mapear ResultSet al DTO ", body));
			e.printStackTrace();
			throw new SQLException("Error al mapear ResultSet al DTO: " + e.getMessage());
		}

		return dtoList;
	}

	/**
	 * Metodo auxiliar para convertir valores de tipo `columnValue` al tipo esperado
	 * en `targetType`.
	 */
	private Object convertValue(Object columnValue, Class<?> targetType) {

		if (columnValue instanceof Integer && targetType == String.class) {
			return columnValue.toString();
		}

		else if (columnValue instanceof String && targetType == Integer.class) {
			return Integer.parseInt((String) columnValue);
		}

		else if (columnValue instanceof Double && targetType == String.class) {
			return columnValue.toString();
		}

		else if (columnValue instanceof String && targetType == Double.class) {
			return Double.parseDouble((String) columnValue);
		}

		else if (columnValue instanceof Integer && targetType == BigDecimal.class) {
			return new BigDecimal((Integer) columnValue);
		}

		else if (columnValue instanceof String && targetType == BigDecimal.class) {
			return new BigDecimal((String) columnValue);
		}

		else if (columnValue instanceof Double && targetType == BigDecimal.class) {
			return BigDecimal.valueOf((Double) columnValue);
		}

		else if (columnValue instanceof BigDecimal && targetType == String.class) {
			return columnValue.toString();
		}

		else if (columnValue instanceof BigDecimal && targetType == Integer.class) {
			return ((BigDecimal) columnValue).intValue();
		}

		else if (columnValue instanceof BigDecimal && targetType == Double.class) {
			return ((BigDecimal) columnValue).doubleValue();
		}

		return columnValue;
	}

	private void enviarCorreo(String header, String body) {
		try {
			if (AppConfig.CONFFROMPROP) {
				emailService = new EmailService();
			} else {
				emailService = new EmailService(postgresDataSourceDestino);
			}
			emailService.sendEmail(header, body);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
