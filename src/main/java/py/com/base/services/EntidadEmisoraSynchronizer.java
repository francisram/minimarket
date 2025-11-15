package py.com.base.services;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransientConnectionException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import py.com.base.dao.ConfiguracionEntidadEmiadqDao;
import py.com.base.dto.configuracion_entidad_emiadqDto;
import py.com.base.utils.AppConfig;
import py.com.base.utils.HttpsClientUtils;
import py.com.base.utils.LogUtil;
import py.com.base.utils.LoggerUtil;
import py.com.base.utils.ReflectionUtils;

/**
 * @author frlopez Esta clase es responsable de la sincronizacion de los datos
 *         de las entidades emisoras entre sistemas. Realiza la replicacion de
 *         los datos de la entidad emisora, administradora y propietaria,
 *         asegurando la consistencia de los datos entre los sistemas.
 * 
 *         La sincronizacion se realiza de manera asincronica usando
 *         {@link CompletableFuture}. Tambien incluye mecanismos de reintentos
 *         en caso de fallos de conexion.
 */

@Service
public class EntidadEmisoraSynchronizer {
	private DataSource as400DataSource;
	private final int maxRetries = AppConfig.MAXRETRIES;
	private final long retryDelayMillis = AppConfig.RETRYDELAYMILLIS;
	private EmailService emailService;
	private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private List<configuracion_entidad_emiadqDto> lceamq;
	
	
	
	
	private static final ExecutorService executorService = Executors.newFixedThreadPool(20);

	/**
	 * Constructor de la clase EntidadEmisoraSynchronizer.
	 *
	 * @param as400DataSource  la fuente de datos para la base de datos AS400.
	 * @param maxRetries       el numero maximo de intentos de reintento en caso de
	 *                         fallos.
	 * @param retryDelayMillis el tiempo de espera entre reintentos en milisegundos.
	 */
	public EntidadEmisoraSynchronizer(DataSource as400DataSource) {
		this.as400DataSource = as400DataSource;
	}

	/**
	 * Sincroniza los datos de la entidad emisora de manera asincronica. Verifica la
	 * configuracion para cada entidad (emisora, administradora y propietaria) y
	 * replica los datos segun corresponda.
	 * 
	 * @param dto el objeto de transferencia de datos que contiene los detalles de
	 *            la transaccion.
	 * @param dt  el objeto de transferencia de datos que contiene los detalles de
	 *            auditoria.
	 * @return un CompletableFuture que representa la finalizacion de la tarea de
	 *         sincronizacion.
	 */
	public CompletableFuture<Void> sincronizarEntidadEmisora(Object unTrimedDt, String paramQueue, String secuencia) {

		LocalDateTime fechaYHoraActual = LocalDateTime.now();
		String fechaYHoraFormateada = fechaYHoraActual.format(FORMATO_FECHA_HORA);
		final Object dt = trimFields(unTrimedDt);
		Class<?> dtoClass = dt.getClass();
		return CompletableFuture.runAsync(() -> {
		//	py.com.bepsa.logs.LogUtil.crearSeq(AppConfig.SEQ_URL);
			
			for (int attempt = 1; attempt <= maxRetries; attempt++) {
				try (Connection connection = as400DataSource.getConnection()) {
					connection.setAutoCommit(true);
					String rrn = ReflectionUtils.getStringValue(dtoClass, dt, "getRrnboleta").trim();
					if (!rrn.isEmpty()) {
						int entidad, entadmn, entprop;

						if (ReflectionUtils.getStringValue(dtoClass, dt, "getCodigoentidadrol") != null) {
							if (ReflectionUtils.getStringValue(dtoClass, dt, "getCodigoentidadrol").length() >= 4) {
								entidad = Integer
										.parseInt(ReflectionUtils.getStringValue(dtoClass, dt, "getCodigoentidadrol")
												.toString().substring(0, 4));
							} else {
								entidad = Integer.parseInt(
										ReflectionUtils.getStringValue(dtoClass, dt, "getCodigoentidadrol").toString());
							}
						} else {
							entidad = 0;
						}

						if (ReflectionUtils.getStringValue(dtoClass, dt, "getEntidadadministradoraatm") != null) {
							if (ReflectionUtils.getStringValue(dtoClass, dt, "getEntidadadministradoraatm")
									.length() >= 4) {
								entadmn = Integer.parseInt(
										ReflectionUtils.getStringValue(dtoClass, dt, "getEntidadadministradoraatm")
												.toString().substring(0, 4));
							} else {
								entadmn = Integer.parseInt(ReflectionUtils
										.getStringValue(dtoClass, dt, "getEntidadadministradoraatm").toString());
							}
						} else {
							entadmn = 0;
						}

						if (ReflectionUtils.getStringValue(dtoClass, dt, "getEntidadpropietariaatm") != null) {
							if (ReflectionUtils.getStringValue(dtoClass, dt, "getEntidadpropietariaatm")
									.length() >= 4) {
								entprop = Integer.parseInt(
										ReflectionUtils.getStringValue(dtoClass, dt, "getEntidadpropietariaatm")
												.toString().substring(0, 4));
							} else {
								entprop = Integer.parseInt(ReflectionUtils
										.getStringValue(dtoClass, dt, "getEntidadpropietariaatm").toString());
							}
						} else {
							entprop = 0;
						}

						if (hasConfiguracion(connection, entidad)) {
							Object t = this.ofuscarTarjeta(dtoClass, dt, false);
							this.ejecutar(connection, t, entidad, paramQueue, secuencia + "|" + fechaYHoraFormateada);

						}

						if (entadmn != entidad && hasConfiguracion(connection, entadmn)) {
							Object t = this.ofuscarTarjeta(dtoClass, dt, true);
							this.ejecutar(connection, t, entadmn, paramQueue, secuencia + "|" + fechaYHoraFormateada);

						}

						if (entprop != entidad && entprop != entadmn && hasConfiguracion(connection, entprop)) {
							Object t = this.ofuscarTarjeta(dtoClass, dt, true);
							this.ejecutar(connection, t, entprop, paramQueue, secuencia + "|" + fechaYHoraFormateada);

						}

						Object obj = this.ofuscarTarjeta(dtoClass, dt, false);
						this.ejecutarInterno(connection, obj, AppConfig.PTCOMERCIOQUEUE, paramQueue,
								secuencia + "|" + fechaYHoraFormateada);

					}

					return;
				} catch (SQLException e) {
						manejarErrorConexion(e, attempt, paramQueue, secuencia, dtoClass, dt);
				}
			}
	/*	}, CompletableFuture.delayedExecutor(retryDelayMillis, TimeUnit.MILLISECONDS)).exceptionally(ex -> {
			LogUtil.error("Excepcion en CompletableFuture de Entidad Emisora Syncronizzer: " + ex.getMessage());
			return null;
		});*/
		//	py.com.bepsa.logs.LogUtil.closeAndFlush();
	},executorService);
	}

	/**
	 * Verifica si existe una configuracion para una entidad dada.
	 *
	 * @param connection la conexion a la base de datos.
	 * @param entidad    el codigo de la entidad para verificar la configuracion.
	 * @return true si la configuracion existe, false en caso contrario.
	 * @throws SQLException si ocurre un error de acceso a la base de datos.
	 */
	private boolean hasConfiguracion(Connection connection, int entidad) throws SQLException {
		boolean existe = false;
		String sql = "SELECT COUNT(1) FROM GXFINDTA.configuracion_entidad_emiadq WHERE configuracion_estado = 'A' AND configuracion_cod_entidad_destino = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, entidad);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					existe = true;
				}
			}
		}
		return existe;
	}

	/**
	 * Actualiza la tabla TBDAUD para marcar la transaccion como enviada
	 * exitosamente a la entidad emisora.
	 *
	 * @param audrnb     el numero de auditoria.
	 * @param audtrxfchc la fecha de la transaccion en la auditoria.
	 * @param audfcht    la marca de tiempo de la auditoria.
	 */
	private void setearTbdaudComoEnviadoEntidadEmisora(Connection connection, String audrnb, String audtrxfchc,
			String audfcht, String paramQueue, String secuencia) {
		String queryString = "UPDATE GXFINDTA.TBDAUD SET AUDENVIAENTIDAD = 'S' WHERE TRIM(AUDRNB) = ? AND AUDTRXFCHC = ? AND RTRIM(AUDFCHT) = ?";
		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
				String fecha = audtrxfchc.substring(0, 8);
				preparedStatement.setString(1, audrnb);
				preparedStatement.setString(2, fecha);
				preparedStatement.setString(3, audfcht);
				int rowsUpdated = preparedStatement.executeUpdate();
				if (rowsUpdated > 0) {
					LoggerUtil.detalle("Registro actualizado exitosamente a estado 'S' en Entidad QUEUE:  " + paramQueue	+ secuencia);
					LoggerUtil.publicarAlSeq(paramQueue,"Registro actualizado exitosamente a estado 'S' en Entidad QUEUE:  " + paramQueue	+ "| secuencia : " + secuencia,	false,"setearTbdaudComoEnviadoEntidadEmisora");
					return;
				} else {
					LoggerUtil.detalle("No se encontro ningun registro para actualizar a S : " + paramQueue	+ " en Entidad" + secuencia);
					LoggerUtil.publicarAlSeq(paramQueue, "No se encontro ningun registro para actualizar a S : "+ paramQueue + " en Entidad" + secuencia + " enviado a reproceso", true,"setearTbdaudComoEnviadoEntidadEmisora");
					ReProcess.enviarAReproceso(AppConfig.REPROCESAR, paramQueue, secuencia, "S", "amq");
					return;
				}

			} catch (SQLTransientConnectionException | SQLTimeoutException e) {
				if (attempt == maxRetries) {
					String body = "Se produjo un error: " + e.getMessage() + "\nDetalles:\n"
							+ Arrays.toString(e.getStackTrace());
					String header = "ERROR ENTIDAD EMISOR SINCRONIZER";
					LogUtil.error(String.format(header,
							"Conexion a la base de datos para actualizar a estado S no disponible QUEUE: " + audrnb
									+ "|" + audtrxfchc + "|" + audfcht + "|" + body));
					ReProcess.enviarAReproceso(AppConfig.REPROCESAR, paramQueue, secuencia, "S", "amq");
					LoggerUtil.publicarAlSeq(paramQueue,	"Conexion a la base de datos para actualizar a estado S no disponible QUEUE:  " + paramQueue+ "| secuencia : " + secuencia + "enviado a reproceso",	true,"setearTbdaudComoEnviadoEntidadEmisora");
					e.printStackTrace();
				}
				try {
					Thread.sleep(retryDelayMillis);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Pausado mientras se espera para reintento", ie);
				}

			} catch (SQLException e2) {
				throw new RuntimeException("Database error", e2);
			}
		}
	}

	/**
	 * Actualiza la tabla TBDAUD para marcar la transaccion como no enviada a la
	 * entidad emisora.
	 *
	 * @param audrnb     el numero de auditoria.
	 * @param audtrxfchc la fecha de la transaccion en la auditoria.
	 * @param audfcht    la marca de tiempo de la auditoria.
	 */
	private void setearTbdaudComoNoEnviadoEntidadEmisora(Connection connection, String audrnb, String audtrxfchc,
			String audfcht, String secuencia, String paramQueue) {
		String queryString = "UPDATE GXFINDTA.TBDAUD SET AUDENVIAENTIDAD = 'X' WHERE TRIM(AUDRNB) = ? AND AUDTRXFCHC = ? AND RTRIM(AUDFCHT) = ?";

		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
			String fecha = audtrxfchc.substring(0, 8);
			preparedStatement.setString(1, audrnb);
			preparedStatement.setString(2, fecha);
			preparedStatement.setString(3, audfcht);
			int rowsUpdated = preparedStatement.executeUpdate();
			if (rowsUpdated > 0) {
				LoggerUtil.detalle("Registro actualizado exitosamente a estado 'X' en Entidad QUEUE:  " + audrnb + "|"
						+ audtrxfchc + "|" + audfcht + secuencia);
				LoggerUtil.importante("Registro actualizado a estado 'X' en Entidad QUEUE");

				return;
			} else {
				LoggerUtil.detalle(	"No se encontro ningun registro para actualizar a X : " + audrnb + " en Entidad" + secuencia,	null);
				LoggerUtil.publicarAlSeq(paramQueue,	"No se encontro ningun registro para actualizar a X : " + paramQueue+ "| secuencia : " + secuencia + "enviado a reproceso",	true,"setearTbdaudComoNoEnviadoEntidadEmisora");
				ReProcess.enviarAReproceso(AppConfig.REPROCESAR, paramQueue, secuencia, "X", "amq");
				return;
			}

		} catch (SQLException e) {

			String body = "Se produjo un error: " + e.getMessage() + "\nDetalles:\n"
					+ Arrays.toString(e.getStackTrace());
			String header = "ERROR ENTIDAD EMISOR SINCRONIZER";
			LogUtil.error(
					String.format(header, "Conexion a la base de datos para actualizar a estado X no disponible QUEUE: "
							+ audrnb + "|" + audtrxfchc + "|" + audfcht + "|" + body));
			LoggerUtil.publicarAlSeq(paramQueue,	"Conexion a la base de datos para actualizar a estado X no disponible QUEUE:" + paramQueue+ "| secuencia : " + secuencia + "enviado a reproceso",	true,"setearTbdaudComoNoEnviadoEntidadEmisora");
			ReProcess.enviarAReproceso(AppConfig.REPROCESAR, paramQueue, secuencia, "X", "amq");
			e.getMessage();
		}
	}

	private void ejecutar(Connection connection, Object t, Integer entidadDestino, String paramQ, String secuencia) {
		Class<?> dto = t.getClass();
		ClientQueServiceImpl camq = ClientQueServiceImpl.getInstance();
		ObjectMapper objectMapper = new ObjectMapper();
		String trxJson;
		ConfiguracionEntidadEmiadqDao ce = new ConfiguracionEntidadEmiadqDao(as400DataSource);
		try {

			ObjectNode node = objectMapper.valueToTree(t);
			node.put("id", Integer.parseInt(node.get("id").asText()));
			node.put("montoorigen", Double.parseDouble(node.get("montoorigen").asText()));
			node.put("montodestino", Double.parseDouble(node.get("montodestino").asText()));
			node.put("saldoactual", Double.parseDouble(node.get("montodestino").asText()));
			node.put("saldodisponible", Double.parseDouble(node.get("montodestino").asText()));
			node.put("montocotizacion", Double.parseDouble(node.get("montodestino").asText()));

			DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

			LocalDate fechaProceso = LocalDate.parse(node.get("fechaproceso").asText(), inputFormatter);
			String fechaFormateada = fechaProceso.format(outputFormatter);

			node.put("fechaproceso", fechaFormateada);

			Iterator<String> fieldNames = node.fieldNames();
			while (fieldNames.hasNext()) {
				String fieldName = fieldNames.next();
				JsonNode value = node.get(fieldName);
				if (value.isNull()) {
					node.put(fieldName, ""); // Reemplaza valores `null` con cadena vacia
				}
			}

			trxJson = objectMapper.writeValueAsString(node);

			lceamq = ce.findByConfiguracionEstado("A");

			for (configuracion_entidad_emiadqDto ceamq : lceamq) {

				if (Integer.parseInt(ceamq.getConfiguracion_cod_entidad_destino()) == entidadDestino) {
					int intentos = 0;
					Boolean enviado = false;
					while (intentos < maxRetries && !enviado) {
						if (ceamq.getConfiguracion_modo().equals(AppConfig.AMQ)) {
							enviado = camq.publicarMensaje(String.valueOf(entidadDestino), trxJson, paramQ,ceamq.getConfiguracion_modo());
							LoggerUtil.detalle("AMQ Sender : " + paramQ);
						}
						if (ceamq.getConfiguracion_modo().equals(AppConfig.PUSHAPI)) {
							enviado = HttpsClientUtils.invocarServicio(ceamq.getConfiguracion_url(), trxJson, 10000,ceamq.getConfiguracion_modo());
							LoggerUtil.detalle("PUSH-API Sender : " + paramQ);
						}
						if (enviado) {
							LoggerUtil.importante("sincronizado en Entidad " + ceamq.getConfiguracion_modo() + " "
									+ paramQ + secuencia);
							LoggerUtil.publicarAlSeq(paramQ,
									"Sincronizado en Entidad " + ceamq.getConfiguracion_modo() + " " + secuencia,
									false ,ceamq.getConfiguracion_modo());
							setearTbdaudComoEnviadoEntidadEmisora(connection,
									ReflectionUtils.getStringValue(dto, t, "getRrnboleta"),
									ReflectionUtils.getStringValue(dto, t, "getFechaproceso"),
									ReflectionUtils.getStringValue(dto, t, "getOp_audfcht"), paramQ, secuencia);
							break;

						}
						intentos++;
						if (intentos < maxRetries) {
							try {
								LoggerUtil.publicarAlSeq(paramQ, "Reintentando envio " + " " + secuencia, true,ceamq.getConfiguracion_modo());
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}
						}
					}

					if (!enviado) {
						setearTbdaudComoNoEnviadoEntidadEmisora(connection,
								ReflectionUtils.getStringValue(dto, t, "getRrnboleta"),
								ReflectionUtils.getStringValue(dto, t, "getFechaproceso"),
								ReflectionUtils.getStringValue(dto, t, "getOp_audfcht"), secuencia, paramQ);
						LoggerUtil.publicarAlSeq(paramQ,
								"No sincronizado en Entidad " + ceamq.getConfiguracion_modo() + " " + secuencia, true,ceamq.getConfiguracion_modo());
					}

				}

			}

		} catch (Exception e) {
			String body = "Se produjo un error: " + e.getMessage() + "\nDetalles:\n"
					+ Arrays.toString(e.getStackTrace());
			LogUtil.error(String.format("ERROR ENTIDAD EMISOR SINCRONIZER",
					"error de mapeo/envio de trx a amq QUEUE: " + ReflectionUtils.getStringValue(dto, t, "getRrnboleta")
							+ "|" + ReflectionUtils.getStringValue(dto, t, "getFechatransaccion") + "|"
							+ ReflectionUtils.getStringValue(dto, t, "getOp_audfcht") + "|" + body));
			LoggerUtil.publicarAlSeq(paramQ, body, true,"AMQ/PUSH-API GENERAL de la trx");
			this.enviarCorreo("ERROR AMQ/ENTIDAD EMISOR SINCRONIZER",
					"error de mapeo/envio de trx a amq QUEUE: " + ReflectionUtils.getStringValue(dto, t, "getRrnboleta")
							+ "|" + ReflectionUtils.getStringValue(dto, t, "getFechatransaccion") + "|"
							+ ReflectionUtils.getStringValue(dto, t, "getOp_audfcht") + "|" + body);
			e.printStackTrace();
		}
	}

	private void ejecutarInterno(Connection connection, Object t, String entidadDestino, String paramQ,
			String secuencia) {
		Class<?> dto = t.getClass();
		ClientQueServiceImpl camq = ClientQueServiceImpl.getInstance();
		ObjectMapper objectMapper = new ObjectMapper();
		String trxJson;

		try {
			ObjectNode node = objectMapper.valueToTree(t);
			// Convertir los campos a numericos
			node.put("id", Integer.parseInt(node.get("id").asText()));
			node.put("montoorigen", Double.parseDouble(node.get("montoorigen").asText()));
			node.put("montodestino", Double.parseDouble(node.get("montodestino").asText()));
			node.put("saldoactual", Double.parseDouble(node.get("montodestino").asText()));
			node.put("saldodisponible", Double.parseDouble(node.get("montodestino").asText()));
			node.put("montocotizacion", Double.parseDouble(node.get("montodestino").asText()));

			DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

			LocalDate fechaProceso = LocalDate.parse(node.get("fechaproceso").asText(), inputFormatter);
			String fechaFormateada = fechaProceso.format(outputFormatter);

			node.put("fechaproceso", fechaFormateada);

			Iterator<String> fieldNames = node.fieldNames();
			while (fieldNames.hasNext()) {
				String fieldName = fieldNames.next();
				JsonNode value = node.get(fieldName);
				if (value.isNull()) {
					node.put(fieldName, "");
				}
			}

			trxJson = objectMapper.writeValueAsString(node);
			int intentos = 0;
			Boolean enviado = false;
			while (intentos < maxRetries && !enviado) {
				if (AppConfig.AMQ.equals(AppConfig.PTCOMERCIOMETHOD)) {
					enviado = camq.publicarMensaje(String.valueOf(entidadDestino), trxJson, paramQ,AppConfig.AMQ);
					LoggerUtil.detalle("AMQ Sender Portal Comercio : " + paramQ);

				}
				if (AppConfig.PUSHAPI.equals(AppConfig.PTCOMERCIOMETHOD)) {
					enviado = HttpsClientUtils.invocarServicio(AppConfig.PTCOMERCIOMETHODURL, trxJson, 10000 ,AppConfig.PUSHAPI);
					LoggerUtil.detalle("PUSH-API Sender Portal Comercio: " + paramQ);
				}
				if (enviado) {
					LoggerUtil.publicarAlSeq(paramQ, "Enviado Sender Portal Comercio AMQ " + paramQ + secuencia, false,"PTC"+ AppConfig.PTCOMERCIOMETHOD);
					setearTbdaudComoEnviadoPortalComercioAMQ(connection,
							ReflectionUtils.getStringValue(dto, t, "getRrnboleta"),
							ReflectionUtils.getStringValue(dto, t, "getFechaproceso"),
							ReflectionUtils.getStringValue(dto, t, "getOp_audfcht"), paramQ, secuencia);
					break;
				}
				intentos++;
				if (intentos < maxRetries) {
					try {
						LoggerUtil.publicarAlSeq(paramQ,
								"Reintentando envio Sender Portal Comercio " + paramQ + secuencia, true,"PTC"+ AppConfig.PTCOMERCIOMETHOD);
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}

			if (!enviado) {
				String header = "Error AMQ Portal Comercio";
				String body = "no se ha podido enviar a la cola de portal de comercio :" + paramQ;
				LogUtil.info(String.format(header + body));
				LoggerUtil.publicarAlSeq(paramQ, "No enviado a AMQ Portal de Comercio " + paramQ + secuencia, false,"PTC"+ AppConfig.PTCOMERCIOMETHOD);
				setearTbdaudComoNoEnviadoPortalComercioAMQ(connection,
						ReflectionUtils.getStringValue(dto, t, "getRrnboleta"),
						ReflectionUtils.getStringValue(dto, t, "getFechaproceso"),
						ReflectionUtils.getStringValue(dto, t, "getOp_audfcht"), paramQ, secuencia);
			}

		} catch (Exception e) {

			String body = "Se produjo un error: " + e.getMessage() + "\nDetalles:\n"
					+ Arrays.toString(e.getStackTrace());
			LogUtil.error(String.format("ERROR ENTIDAD EMISOR SINCRONIZER PORTALCOMERCIO",
					"error de mapeo/envio de trx a amq QUEUE: " + ReflectionUtils.getStringValue(dto, t, "getRrnboleta")
							+ "|" + ReflectionUtils.getStringValue(dto, t, "getFechatransaccion") + "|"
							+ ReflectionUtils.getStringValue(dto, t, "getOp_audfcht") + "|" + body));
			this.enviarCorreo("ERROR AMQ PORTAL DE COMERCIO SINCRONIZER",
					"error de mapeo/envio de trx a amq QUEUE: " + ReflectionUtils.getStringValue(dto, t, "getRrnboleta")
							+ "|" + ReflectionUtils.getStringValue(dto, t, "getFechatransaccion") + "|"
							+ ReflectionUtils.getStringValue(dto, t, "getOp_audfcht") + "|" + body);
			LoggerUtil.publicarAlSeq(paramQ, "Falla en enviado a AMQ Portal de Comercio " + paramQ + secuencia, true,"PTC"+ AppConfig.PTCOMERCIOMETHOD);
			e.printStackTrace();
		}
	}

	private Object ofuscarTarjeta(Class<?> dtoClass, Object dto, boolean ofuscado) {
		try {
			Method getNumeroTarjetaMethod = dtoClass.getMethod("getNumerotarjeta");
			Method setNumeroTarjetaMethod = dtoClass.getMethod("setNumerotarjeta", String.class);
			Method setNumeroTarjetaOfuscadoMethod = dtoClass.getMethod("setNumerotarjetaofuscado", String.class);
			Method setFechaInsercionMethod = dtoClass.getMethod("setFechainsercion", String.class);
			Method setHoraInsercionMethod = dtoClass.getMethod("setHorainsercion", String.class);

			Method getFechaTransaccion = dtoClass.getMethod("getFechatransaccion");
			Method setFechaTransaccion = dtoClass.getMethod("setFechatransaccion", String.class);

			String numeroTarjeta = (String) getNumeroTarjetaMethod.invoke(dto);
			String numeroTarjetaOfuscado = numeroTarjeta;
			String numeroTarjetaOfuscado2 = numeroTarjeta;

			if (numeroTarjeta != null && numeroTarjeta.length() >= 10) {
				int longitud = numeroTarjeta.length();
				numeroTarjetaOfuscado = numeroTarjeta.substring(0, 6) + "*".repeat(longitud - 10)
						+ numeroTarjeta.substring(longitud - 4);

				if (ofuscado) {
					setNumeroTarjetaMethod.invoke(dto, numeroTarjetaOfuscado);
					setNumeroTarjetaOfuscadoMethod.invoke(dto, numeroTarjetaOfuscado);
				} else {
					numeroTarjetaOfuscado = numeroTarjeta.substring(0, 6) + "*".repeat(longitud - 10)
							+ numeroTarjeta.substring(longitud - 4);
					setNumeroTarjetaOfuscadoMethod.invoke(dto, numeroTarjetaOfuscado);

				}

			}

			String fechaTransaccion = (String) getFechaTransaccion.invoke(dto);

			// DateTimeFormatter inputFormatter = new
			// DateTimeFormatterBuilder().appendPattern("yyyyMMdd HH:mm:ss").toFormatter();

			DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

			try {
				LocalDateTime fechaLocal = LocalDateTime.parse(fechaTransaccion.trim(), inputFormatter);
			//	ZoneId zonaAsuncion = ZoneId.of("America/Asuncion");
				ZoneId zonaBuenosAires = ZoneId.of("America/Argentina/Buenos_Aires");
				ZonedDateTime fechaAsuncion = fechaLocal.atZone(zonaBuenosAires);
				ZonedDateTime fechaMenosTres = fechaAsuncion.minusHours(3);
				ZonedDateTime fechaUtc = fechaMenosTres.withZoneSameInstant(ZoneOffset.UTC);

				// ZonedDateTime fechaUtc = fechaLocal.atZone(ZoneOffset.UTC);
				DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_INSTANT;

				String fechaFormateada = outputFormatter.format(fechaUtc.toInstant());
				setFechaTransaccion.invoke(dto, fechaFormateada);
			} catch (Exception e) {
				LoggerUtil.detalle(Arrays.toString(e.getStackTrace()));
			}
			setFechaInsercionMethod.invoke(dto, new Date(System.currentTimeMillis()).toString());
			/*
			 * String fechaHoraFormateada =
			 * DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
			 * .withZone(ZoneOffset.UTC).format(Instant.now());
			 */
			//ZoneId zonaHoraria = ZoneId.of("America/Asuncion");
			ZoneId zonaBuenosAires = ZoneId.of("America/Argentina/Buenos_Aires");
			ZonedDateTime fechaHoraAsuncion = ZonedDateTime.now(zonaBuenosAires);
			// ZonedDateTime fechaHoraUTC =
			// fechaHoraAsuncion.withZoneSameInstant(ZoneOffset.UTC);
			String fechaHoraFormateada = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
					.format(fechaHoraAsuncion);

			setHoraInsercionMethod.invoke(dto, fechaHoraFormateada);

		} catch (Exception e) {
			LogUtil.error(String.format("Error ofuscarTarjeta ", Arrays.toString(e.getStackTrace())));
			// enviarCorreo("Error ofuscarTarjeta ", Arrays.toString(e.getStackTrace()));
			e.printStackTrace();
		}
		return dto;
	}

	private void enviarCorreo(String header, String body) {
		try {
			if (AppConfig.CONFFROMPROP) {
				emailService = new EmailService();
			} else {
				emailService = new EmailService(as400DataSource);
			}
			emailService.sendEmail(header, body);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static <T> T trimFields(T obj) {
		Class<?> dtoClass = obj.getClass();

		for (Field field : dtoClass.getDeclaredFields()) {
			field.setAccessible(true);

			try {
				if (field.getType().equals(String.class)) {
					String value = (String) field.get(obj);
					if (value != null) {
						field.set(obj, value.trim());
					}
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		return obj;
	}

	private void setearTbdaudComoEnviadoPortalComercioAMQ(Connection connection, String audrnb, String audtrxfchc,
			String audfcht, String paramQueue, String secuencia) {
		String queryString = "UPDATE GXFINDTA.TBDAUD SET AUDENVIAPORTAL = 'S' WHERE TRIM(AUDRNB) = ? AND AUDTRXFCHC = ? AND RTRIM(AUDFCHT) = ?";
		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
				String fecha = audtrxfchc.substring(0, 8);
				preparedStatement.setString(1, audrnb);
				preparedStatement.setString(2, fecha);
				preparedStatement.setString(3, audfcht);
				int rowsUpdated = preparedStatement.executeUpdate();
				if (rowsUpdated > 0) {
					LoggerUtil.detalle("Registro actualizado exitosamente a estado 'S' en AUDENVIAPORTAL QUEUE:  "
							+ paramQueue + secuencia);
					LoggerUtil.publicarAlSeq(paramQueue,"Registro actualizado exitosamente a estado 'S' en AUDENVIAPORTAL QUEUE:  " + paramQueue+ "| secuencia : " + secuencia,	false,"setearTbdaudComoEnviadoPortalComercioAMQ");
					return;
				} else {
					LoggerUtil.detalle("No se encontro ningun registro para actualizar a S : " + paramQueue
							+ " en AUDENVIAPORTAL" + secuencia);
					LoggerUtil.publicarAlSeq(paramQueue, "No se encontro ningun registro para actualizar a S : "
							+ paramQueue + " en AUDENVIAPORTAL" + secuencia + " enviado a reproceso", false,"setearTbdaudComoEnviadoPortalComercioAMQ");
					ReProcess.enviarAReproceso(AppConfig.REPROCESAR, paramQueue, secuencia, "S", "ptc");
					return;
				}

			} catch (SQLTransientConnectionException | SQLTimeoutException e) {
				if (attempt == maxRetries) {
					String body = "Se produjo un error: " + e.getMessage() + "\nDetalles:\n"
							+ Arrays.toString(e.getStackTrace());
					String header = "ERROR PTC AMQ SINCRONIZER";
					LogUtil.error(String.format(header,
							"Conexion a la base de datos para actualizar a estado S en AUDENVIAPORTAL no disponible QUEUE: "
									+ audrnb + "|" + audtrxfchc + "|" + audfcht + "|" + body));
					ReProcess.enviarAReproceso(AppConfig.REPROCESAR, paramQueue, secuencia, "S", "ptc");
					LoggerUtil.importante("Conexion a la base de datos para actualizar a estado S en AUDENVIAPORTAL no disponible QUEUE:  "
									+ paramQueue + "| secuencia : " + secuencia + "enviado a reproceso");
					e.printStackTrace();
				}
				try {
					Thread.sleep(retryDelayMillis);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Pausado mientras se espera para reintento", ie);
				}

			} catch (SQLException e2) {
				throw new RuntimeException("Database error", e2);
			}
		}
	}

	private void setearTbdaudComoNoEnviadoPortalComercioAMQ(Connection connection, String audrnb, String audtrxfchc,
			String audfcht, String secuencia, String paramQueue) {
		String queryString = "UPDATE GXFINDTA.TBDAUD SET AUDENVIAPORTAL = 'X' WHERE TRIM(AUDRNB) = ? AND AUDTRXFCHC = ? AND RTRIM(AUDFCHT) = ?";

		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
			String fecha = audtrxfchc.substring(0, 8);
			preparedStatement.setString(1, audrnb);
			preparedStatement.setString(2, fecha);
			preparedStatement.setString(3, audfcht);
			int rowsUpdated = preparedStatement.executeUpdate();
			if (rowsUpdated > 0) {
				LoggerUtil.detalle("Registro actualizado exitosamente a estado 'X' en AUDENVIAPORTAL QUEUE:  " + audrnb	+ "|" + audtrxfchc + "|" + audfcht + secuencia);
				LoggerUtil.publicarAlSeq(paramQueue,"Registro actualizado exitosamente a estado 'X' en AUDENVIAPORTAL QUEUE:  " + paramQueue+ "| secuencia : " + secuencia,	false,"setearTbdaudComoEnviadoPortalComercioAMQ");
				return;
			} else {
				LoggerUtil.detalle("No se encontro ningun registro para actualizar a X  en PTC: " + audrnb
						+ " en AUDENVIAPORTAL " + secuencia, null);
				LoggerUtil.publicarAlSeq(paramQueue,"No se encontro ningun registro para actualizar a X  en PTC  QUEUE:  " + paramQueue+ "| secuencia : " + secuencia,	true,"setearTbdaudComoEnviadoPortalComercioAMQ");
				ReProcess.enviarAReproceso(AppConfig.REPROCESAR, paramQueue, secuencia, "X", "ptc");
				return;
			}

		} catch (SQLException e) {

			String body = "Se produjo un error: " + e.getMessage() + "\nDetalles:\n"
					+ Arrays.toString(e.getStackTrace());
			String header = "ERROR PTC AMQ SINCRONIZER";
			LogUtil.error(String.format(header,		"Conexion a la base de datos para actualizar a estado X en AUDENVIAPORTAL no disponible QUEUE: "	+ audrnb + "|" + audtrxfchc + "|" + audfcht + "|" + body));
			LoggerUtil.publicarAlSeq(paramQueue,"Conexion a la base de datos para actualizar a estado X en AUDENVIAPORTAL no disponible QUEUE: " + paramQueue+ "| secuencia : " + secuencia,	true,"setearTbdaudComoEnviadoPortalComercioAMQ");
			ReProcess.enviarAReproceso(AppConfig.REPROCESAR, paramQueue, secuencia, "X", "ptc");
		}
	}

	private void manejarErrorConexion(SQLException e, int attempt, String paramQueue, String secuencia , Class<?> dtoClass, Object dt) {

		if (attempt == maxRetries) {
			try (Connection connection = as400DataSource.getConnection()) {
				try {
					setearTbdaudComoNoEnviadoEntidadEmisora(connection,
					ReflectionUtils.getStringValue(dtoClass, dt, "getRrnboleta"),
					ReflectionUtils.getStringValue(dtoClass, dt, "getFechaproceso"),
					ReflectionUtils.getStringValue(dtoClass, dt, "getOp_audfcht"),
					secuencia , paramQueue);
			LogUtil.error(
					String.format("ERROR ENTIDAD EMISOR SINCRONIZER", "Numero maximo de intentos alcanzado QUEUE : "
							+ paramQueue + secuencia ));
			throw new RuntimeException("Error after maximum retry attempts", e);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
	
			} catch (SQLException e1) {
				LogUtil.error("Fallo al intentar cerrar la conexión en manejarErrorConexion: " + e1.getMessage());
				e1.printStackTrace();
			}
		}
		LoggerUtil.publicarAlSeq(paramQueue, "Reintentando... Intento " + attempt + " de " + maxRetries, true,"Entidad Emisora Syncronizzer General");
		LoggerUtil.detalle("Reintentando... Intento " + attempt + " de " + maxRetries
				+ ReflectionUtils.getStringValue(dtoClass, dt, "getRrnboleta"));

	}

}
