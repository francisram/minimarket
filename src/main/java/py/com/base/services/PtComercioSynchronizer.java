package py.com.base.services;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

import javax.sql.DataSource;

import jakarta.mail.MessagingException;
import py.com.base.dto.BusDatosTransaccionesDestinoDto;
import py.com.base.utils.AppConfig;
import py.com.base.utils.LoggerUtil;
import py.com.base.utils.ReflectionUtils;

/**
 * @author frlopez Clase que sincroniza los datos de transacciones entre dos
 *         bases de datos, ejecutando operaciones de actualizacion o insercion
 *         segun sea necesario. Implementa logica de reintentos en caso de fallo
 *         de conexion.
 */

public class PtComercioSynchronizer {

	private final DataSource postgresDataSource;
	private DataSource as400DataSource;
	private final int maxRetries = AppConfig.MAXRETRIES;
	private final long retryDelayMillis = AppConfig.RETRYDELAYMILLIS;
	private EmailService emailService;
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/**
	 * Constructor para inicializar el sincronizador con las fuentes de datos, el
	 * numero maximo de reintentos y el tiempo de espera entre reintentos.
	 *
	 * @param postgresDataSource Fuente de datos para la base de datos Postgres.
	 * @param as400DataSource    Fuente de datos para la base de datos AS400.
	 * @param maxRetries         Numero maximo de reintentos en caso de error.
	 * @param retryDelayMillis   Tiempo de espera entre reintentos en milisegundos.
	 */
	public PtComercioSynchronizer(DataSource postgresDataSource, DataSource as400DataSource) {
		this.postgresDataSource = postgresDataSource;
		this.as400DataSource = as400DataSource;
	}

	/**
	 * Sincroniza los datos de la transaccion entre las dos bases de datos. Intenta
	 * actualizar o insertar un registro en la base de datos destino. Implementa
	 * logica de reintentos en caso de fallo en la conexion a la base de datos.
	 *
	 * @param dto DTO con los datos de la transaccion a sincronizar.
	 * @param dt  DTO con los datos de la tabla TBDAUV.
	 * @return Un {@link CompletableFuture} que contiene el DTO actualizado o
	 *         insertado.
	 */
	public CompletableFuture<BusDatosTransaccionesDestinoDto> sincronizarPtComercio(Object dt , String parmDataqueue, String secuencia) {
		LocalDateTime fechaYHoraActual = LocalDateTime.now();
		String fechaYHoraFormateada = fechaYHoraActual.format(FORMATO_FECHA_HORA);
		BusDatosTransaccionesDestinoDto dto = this.toDTO(dt);
		Class<?> dtoClass = dt.getClass(); 
		return CompletableFuture.supplyAsync(() -> {
			for (int attempt = 1; attempt <= maxRetries; attempt++) {
				try (Connection connection = postgresDataSource.getConnection()) {
					connection.setAutoCommit(false);
					if (registroExiste(connection, dto)) {
						LoggerUtil.detalle("Se actualiza Portal de comercio QUEUE : " +parmDataqueue);
						boolean logrado = actualizarRegistro(connection, dto);
						if (!logrado) {
							setearTbdaudComoNoEnviadoPortalComercio(ReflectionUtils.getStringValue(dtoClass, dt, "getRrnboleta"), ReflectionUtils.getStringValue(dtoClass, dt, "getFechaproceso"),
									ReflectionUtils.getStringValue(dtoClass, dt, "getOp_audfcht"),secuencia + "|"+ fechaYHoraFormateada);
							LoggerUtil.detalle( "Actualizacion a Portal Comercio No Logrado : " +parmDataqueue + secuencia + "|"+ fechaYHoraFormateada);
						} else {
							setearTbdaudComoEnviadoPortalComercio(ReflectionUtils.getStringValue(dtoClass, dt, "getRrnboleta"), ReflectionUtils.getStringValue(dtoClass, dt, "getFechaproceso"),
									ReflectionUtils.getStringValue(dtoClass, dt, "getOp_audfcht"),parmDataqueue,secuencia + "|"+ fechaYHoraFormateada);
							LoggerUtil.detalle("Actualizacion a Portal Comercio : " + parmDataqueue);
							LoggerUtil.importante( "Actualizacion a Portal Comercio: " +parmDataqueue + secuencia + "|"+ fechaYHoraFormateada);
						}
					} else {
						
						LoggerUtil.detalle( "Se Inserta a Portal de comercio QUEUE : " +parmDataqueue);
						boolean logrado = insertarRegistro(connection, dto);
						if (logrado) {
							setearTbdaudComoEnviadoPortalComercio(ReflectionUtils.getStringValue(dtoClass, dt, "getRrnboleta"), ReflectionUtils.getStringValue(dtoClass, dt, "getFechaproceso"),
									ReflectionUtils.getStringValue(dtoClass, dt, "getOp_audfcht"),parmDataqueue,secuencia + "|"+ fechaYHoraFormateada);
							LoggerUtil.detalle("Insercion a Portal Comercio: " + parmDataqueue);
							LoggerUtil.importante( "Insercion a Portal Comercio: " +parmDataqueue + secuencia + "|"+ fechaYHoraFormateada);

						} else {
							setearTbdaudComoNoEnviadoPortalComercio(ReflectionUtils.getStringValue(dtoClass, dt, "getRrnboleta"), ReflectionUtils.getStringValue(dtoClass, dt, "getFechaproceso"),
									ReflectionUtils.getStringValue(dtoClass, dt, "getOp_audfcht"),secuencia + "|"+ fechaYHoraFormateada);
							LoggerUtil.detalle("Insercion a Portal Comercio No Logrado: "+ 	parmDataqueue);
						}
					}
					connection.commit();
					return dto; 
				} catch (SQLException e) {
					if (attempt == maxRetries) {
						setearTbdaudComoNoEnviadoPortalComercio(ReflectionUtils.getStringValue(dtoClass, dt, "getRrnboleta"), ReflectionUtils.getStringValue(dtoClass, dt, "getFechaproceso"),
								ReflectionUtils.getStringValue(dtoClass, dt, "getOp_audfcht"),secuencia + "|"+ fechaYHoraFormateada);
						LoggerUtil.detalle("maximo numero de intentos realizado a Portal Comercio" + parmDataqueue);
						
						this.enviarCorreo("ERROR PTCOMERCIO SINCRONIZER",
									"maximo numero de intentos realizado QUEUE: " +parmDataqueue);
						throw new RuntimeException("Error : maximo numero de intentos realizado", e);
					}
					e.printStackTrace();
					try {
						Thread.sleep(retryDelayMillis);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}
				}
			}
			return null;
		});
	}

	/**
	 * Verifica si un registro existe en la base de datos de Postgres.
	 *
	 * @param connection Conexion a la base de datos.
	 * @param dto        DTO con los datos de la transaccion.
	 * @return {@code true} si el registro existe, {@code false} en caso contrario.
	 * @throws SQLException Si ocurre un error de base de datos.
	 */
	private boolean registroExiste(Connection connection, BusDatosTransaccionesDestinoDto dto) throws SQLException {
		boolean existe = false;
		String selectSql = "SELECT COUNT(1) FROM public.busdatos_transacciones WHERE operacion_rrnbepsa = ?";
		try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
			ps.setString(1, dto.getOperacion_rrnbepsa());
			ResultSet rs = ps.executeQuery();
			if (rs.next() && rs.getInt(1) > 0) {
				existe = true;
			}
		} catch (Exception e) {
			e.getMessage();
		}
		return existe;
	}

	/**
	 * Actualiza un registro en la base de datos destino.
	 *
	 * @param connection Conexion a la base de datos.
	 * @param dto        DTO con los datos actualizados.
	 * @return {@code true} si la actualizacion fue exitosa, {@code false} en caso
	 *         contrario.
	 * @throws SQLException Si ocurre un error de base de datos.
	 */
	private boolean actualizarRegistro(Connection connection, BusDatosTransaccionesDestinoDto dto) throws SQLException {
		boolean exito = false;
		String tableName = "public.busdatos_transacciones";
		Field[] fields = dto.getClass().getDeclaredFields();

		StringJoiner setClause = new StringJoiner(", ");
		String pkField = "operacion_rrnbepsa"; 
		String pkValue = null;

		for (Field field : fields) {
			if (!"operacion_id".equals(field.getName()) && !field.getName().equals(pkField)) {
				setClause.add(field.getName() + " = ?");
			}
		}

		String updateSql = "UPDATE " + tableName + " SET " + setClause.toString() + " WHERE " + pkField + " = ?";

		int index = 1;
		try (PreparedStatement updatePs = connection.prepareStatement(updateSql)) {
			for (Field field : fields) {
				field.setAccessible(true);
				Object value = field.get(dto);
				
				 if (value instanceof String) {
					 String stringValue = (String) value;
				        value = stringValue.replace("\u0000", "").replace("\0", "");
				    }
				
				if (field.getName().equals(pkField)) {
					pkValue = (String) value;
				} else if (!"operacion_id".equals(field.getName())) {
					updatePs.setObject(index++, value);
				}
			}
			updatePs.setObject(index, pkValue); 
			updatePs.executeUpdate();
			exito = true;
			if (dto.getOperacion_id() == 0) {
				String selectSql = "SELECT operacion_id FROM " + tableName + " WHERE " + pkField + " = ?";
				try (PreparedStatement selectPs = connection.prepareStatement(selectSql)) {
					selectPs.setString(1, pkValue);
					ResultSet rs = selectPs.executeQuery();
					if (rs.next()) {
						dto.setOperacion_id(rs.getInt("operacion_id")); 
					}
				}
			}

		} catch (IllegalAccessException e) {
			exito = false;
			LoggerUtil.detalle("Error accessing field values", e);

			String body = "Se produjo un error: " + e.getMessage() + "\nDetalles:\n" + Arrays.toString(e.getStackTrace());
			this.enviarCorreo("Error accessing field values",dto.getOperacion_rrnbepsa() + "|" + body);
			throw new RuntimeException("Error accessing field values", e);
		}
		return exito;
	}

	/**
	 * Inserta un nuevo registro en la base de datos destino.
	 *
	 * @param connection Conexion a la base de datos.
	 * @param dto        DTO con los datos a insertar.
	 * @return {@code true} si la insercion fue exitosa, {@code false} en caso
	 *         contrario.
	 * @throws SQLException Si ocurre un error de base de datos.
	 */
	private boolean insertarRegistro(Connection connection, BusDatosTransaccionesDestinoDto dto) throws SQLException {

		boolean exito = false;

		String tableName = "public.busdatos_transacciones";
		Field[] fields = dto.getClass().getDeclaredFields();

		StringJoiner columns = new StringJoiner(", ");
		StringJoiner values = new StringJoiner(", ");

		for (Field field : fields) {
			if (!"operacion_id".equals(field.getName())) {
				columns.add(field.getName());
				values.add("?");
			}
		}

		String insertSql = "INSERT INTO " + tableName + " (" + columns.toString() + ") VALUES (" + values.toString()
				+ ")";


		String returningSql = insertSql + " RETURNING operacion_id";

		try (PreparedStatement insertPs = connection.prepareStatement(returningSql)) {
			int index = 1;
			for (Field field : fields) {
				if (!"operacion_id".equals(field.getName())) {
					field.setAccessible(true);
					Object value = field.get(dto);
					if (value instanceof String) {
					 String stringValue = (String) value;
				        value = stringValue.replace("\u0000", "").replace("\0", "");
					}
					insertPs.setObject(index++, value);
				}
			}

			ResultSet rs = insertPs.executeQuery();
			if (rs.next()) {
				dto.setOperacion_id(rs.getInt("operacion_id")); 
			}

			exito = true;
		} catch (IllegalAccessException e) {
			LoggerUtil.detalle("Error accessing field values", e);

			String body = "Se produjo un error: " + e.getMessage() + "\nDetalles:\n" + Arrays.toString(e.getStackTrace());
			this.enviarCorreo("Error accessing field values",dto.getOperacion_rrnbepsa() + "|" + body);
			throw new RuntimeException("Error accessing field values", e);
		}

		return exito;

	}

	/**
	 * Marca el registro en TBDAUD como enviado al portal de comercio.
	 *
	 * @param audrnb     Numero de referencia del registro.
	 * @param audtrxfchc Fecha de la transaccion.
	 * @param audfcht    Fecha de auditoria.
	 */
	private void setearTbdaudComoEnviadoPortalComercio(String audrnb, String audtrxfchc, String audfcht,String paramQueue ,String secuencia) {
		String queryString = "UPDATE GXFINDTA.TBDAUD SET AUDENVIAPORTAL = 'S' WHERE TRIM(AUDRNB) = ? AND AUDTRXFCHC = ? AND RTRIM(AUDFCHT) = ?";

		try (Connection connection = as400DataSource.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
			String fecha = audtrxfchc.substring(0, 8);
			preparedStatement.setString(1, audrnb);
			preparedStatement.setString(2, fecha);
			preparedStatement.setString(3, audfcht);
			int rowsUpdated = preparedStatement.executeUpdate();

			if (rowsUpdated > 0) {
				LoggerUtil.detalle("Registro actualizado exitosamente a estado 'S' en Portal de comercio QUEUE:  " + paramQueue + secuencia);
			} else {
				LoggerUtil.detalle("No se encontro ningun registro para actualizar a S : "+ paramQueue + " Portal de comercio" + secuencia);
			}

		} catch (SQLException e) {

			String body = "Se produjo un error: " + e.getMessage() + "\nDetalles:\n" + Arrays.toString(e.getStackTrace());
			this.enviarCorreo("ERROR PTCOMERCIO SINCRONIZER",
						"Conexion a la base de datos para actualizar a estado S no disponible QUEUE: " + audrnb + "|" +audtrxfchc + "|" +audfcht + "|" + body);
			e.getMessage();
		}
	}

	/**
	 * Marca el registro en TBDAUD como no enviado al portal de comercio.
	 *
	 * @param audrnb     Numero de referencia del registro.
	 * @param audtrxfchc Fecha de la transaccion.
	 * @param audfcht    Fecha de auditoria.
	 */
	private void setearTbdaudComoNoEnviadoPortalComercio(String audrnb, String audtrxfchc, String audfcht , String secuencia) {
		String queryString = "UPDATE GXFINDTA.TBDAUD SET AUDENVIAPORTAL = 'X' WHERE TRIM(AUDRNB) = ? AND AUDTRXFCHC = ? AND RTRIM(AUDFCHT) = ?";

		try (Connection connection = as400DataSource.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
			String fecha = audtrxfchc.substring(0, 8);
			preparedStatement.setString(1, audrnb);
			preparedStatement.setString(2, fecha);
			preparedStatement.setString(3, audfcht);
			int rowsUpdated = preparedStatement.executeUpdate();
			if (rowsUpdated > 0) {
				LoggerUtil.detalle( "Registro actualizado a estado 'X' QUEUE " + audrnb + "|" + audtrxfchc +"| " + audfcht + secuencia);
			} else {
				LoggerUtil.detalle("No se encontro ningun registro para actualizar a X QUEUE :" + audrnb + "|" + audtrxfchc +"| " + audfcht + secuencia);
			}

		} catch (SQLException e) {

			String body = "Se produjo un error: " + e.getMessage() + "\nDetalles:\n" + Arrays.toString(e.getStackTrace());
			this.enviarCorreo("ERROR PTCOMERCIO SINCRONIZER",
						"Conexion a la base de datos para actualizar a estado X no disponible QUEUE: " + audrnb + "|" +audtrxfchc + "|" +audfcht + "|" + body );
			e.getMessage();
		}
	}

	private BusDatosTransaccionesDestinoDto toDTO(Object dt) {
		BusDatosTransaccionesDestinoDto busDTO = new BusDatosTransaccionesDestinoDto();
		Class<?> dtoClass = dt.getClass(); 
		try {

			busDTO.setOperacion_ambito_transaccion(ReflectionUtils.getStringValue(dtoClass, dt, "getAmbitotransaccion"));
			busDTO.setOperacion_cant_cuotas(ReflectionUtils.getShortValue(dtoClass, dt, "getCantidadcuotas"));
			busDTO.setOperacion_cod_afinidad(ReflectionUtils.getShortValue(dtoClass, dt, "getCodigoafinidad"));
			busDTO.setOperacion_cod_autenticacion(ReflectionUtils.getStringValue(dtoClass, dt, "getCodigoautenticacion"));
			busDTO.setOperacion_cod_comercio(ReflectionUtils.getIntegerValue(dtoClass, dt, "getCodigocomercio"));
			busDTO.setOperacion_cod_entidad_rol(ReflectionUtils.getIntegerValue(dtoClass, dt, "getCodigoentidadrol"));
			busDTO.setOperacion_cod_marc_tarjeta(ReflectionUtils.getStringValue(dtoClass, dt, "getCodigomarcatarjeta"));
			busDTO.setOperacion_cod_moneda_destino(ReflectionUtils.getShortValue(dtoClass, dt, "getCodigomonedadestino"));
			busDTO.setOperacion_cod_moneda_origen(ReflectionUtils.getShortValue(dtoClass, dt, "getCodigomonedaorigen"));
			busDTO.setOperacion_cod_pais_destino(ReflectionUtils.getShortValue(dtoClass, dt, "getCodigopaisdestino"));
			busDTO.setOperacion_cod_pais_origen(ReflectionUtils.getShortValue(dtoClass, dt, "getCodigopaisorigen"));
			busDTO.setOperacion_cod_prestacion(ReflectionUtils.getStringValue(dtoClass, dt, "getCodigoprestacion"));
			busDTO.setOperacion_cod_prod_tarjeta(ReflectionUtils.getStringValue(dtoClass, dt, "getCodigoproductotarjeta"));
			busDTO.setOperacion_cod_retorno(ReflectionUtils.getStringValue(dtoClass, dt, "getCodigoretorno"));
			busDTO.setOperacion_cod_servicio(ReflectionUtils.getStringValue(dtoClass, dt, "getCodigoservicio"));
			busDTO.setOperacion_cod_sucursal(ReflectionUtils.getIntegerValue(dtoClass, dt, "getCodigosucursal"));
			busDTO.setOperacion_cod_transaccion(ReflectionUtils.getStringValue(dtoClass, dt, "getCodigotransaccion"));
			busDTO.setOperacion_cta_transfe_destino(ReflectionUtils.getStringValue(dtoClass, dt, "getCuentadestinotransferencia"));
			busDTO.setOperacion_cta_transfe_origen(ReflectionUtils.getStringValue(dtoClass, dt, "getCuentaorigentransferencia"));
			busDTO.setOperacion_cta_utda_operacion(ReflectionUtils.getStringValue(dtoClass, dt, "getCuentaoperacion"));
			busDTO.setOperacion_denom_comercial(ReflectionUtils.getStringValue(dtoClass, dt, "getNombrecomercio"));
			busDTO.setOperacion_desc_afinidad(ReflectionUtils.getStringValue(dtoClass, dt, "getNombreafinidad"));
			busDTO.setOperacion_desc_dispositivo(ReflectionUtils.getStringValue(dtoClass, dt, "getNombredispositivo"));
			busDTO.setOperacion_desc_enti_destino_tr(ReflectionUtils.getStringValue(dtoClass, dt, "getNombreentidaddestinotransferencia"));
			busDTO.setOperacion_desc_enti_origen_tr(ReflectionUtils.getStringValue(dtoClass, dt, "getNombreentidadorigentransferencia"));
			busDTO.setOperacion_desc_form_pago(ReflectionUtils.getStringValue(dtoClass, dt, "getNombreformapago"));
			busDTO.setOperacion_desc_marc_tarjeta(ReflectionUtils.getStringValue(dtoClass, dt, "getNombremarcatarjeta"));
			busDTO.setOperacion_desc_mcc(ReflectionUtils.getStringValue(dtoClass, dt, "getMccnombre"));
			busDTO.setOperacion_desc_mod_entrada(ReflectionUtils.getStringValue(dtoClass, dt, "getNombremodoentrada"));
			busDTO.setOperacion_desc_moneda_destino(ReflectionUtils.getStringValue(dtoClass, dt, "getNombremonedadestino"));
			busDTO.setOperacion_desc_moneda_origen(	ReflectionUtils.getStringValue(dtoClass, dt, "getNombremonedaorigen"));
			busDTO.setOperacion_desc_pais_destino(ReflectionUtils.getStringValue(dtoClass, dt, "getNombrepaisdestino"));
			busDTO.setOperacion_desc_pais_origen(ReflectionUtils.getStringValue(dtoClass, dt, "getNombrepaisorigen"));
			busDTO.setOperacion_desc_proce_adquirien(ReflectionUtils.getStringValue(dtoClass, dt, "getNombreprocesadoradquiriente"));
			busDTO.setOperacion_desc_proce_emisor(ReflectionUtils.getStringValue(dtoClass, dt, "getNombreprocesadoremisor"));
			busDTO.setOperacion_desc_prod_tarjeta(ReflectionUtils.getStringValue(dtoClass, dt, "getNombreproductotarjeta"));
			busDTO.setOperacion_desc_retorno(ReflectionUtils.getStringValue(dtoClass, dt, "getNombreretorno"));
			busDTO.setOperacion_desc_servicio(ReflectionUtils.getStringValue(dtoClass, dt, "getNombreservicio"));
			busDTO.setOperacion_desc_sucursal(ReflectionUtils.getStringValue(dtoClass, dt, "getNombresucursal"));
			busDTO.setOperacion_desc_transaccion(ReflectionUtils.getStringValue(dtoClass, dt, "getNombretransaccion"));
			busDTO.setOperacion_ent_propieta_atm(ReflectionUtils.getIntegerValue(dtoClass, dt, "getEntidadpropietariaatm"));
			busDTO.setOperacion_ent_transf_origen(ReflectionUtils.getShortValue(dtoClass, dt, "getEntidadorigentransferencia"));
			busDTO.setOperacion_ent_transfe_destino(ReflectionUtils.getShortValue(dtoClass, dt, "getEntidaddestinotransferencia"));
			busDTO.setOperacion_enti_admi_atm(ReflectionUtils.getIntegerValue(dtoClass, dt, "getEntidadadministradoraatm"));
			busDTO.setOperacion_est_fn_transaccion(	ReflectionUtils.getStringValue(dtoClass, dt, "getEstadofintransaccion"));
			busDTO.setOperacion_est_in_transaccion(	ReflectionUtils.getStringValue(dtoClass, dt, "getEstadoinittransaccion"));
			busDTO.setOperacion_est_reversa(ReflectionUtils.getStringValue(dtoClass, dt, "getEsreversa"));
			busDTO.setOperacion_estac_dest_ds(ReflectionUtils.getStringValue(dtoClass, dt, "getEstaciondestino"));
			busDTO.setOperacion_estac_origen_ss(ReflectionUtils.getStringValue(dtoClass, dt, "getEstacionorigen"));
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			java.util.Date utilDate = format.parse(ReflectionUtils.getStringValue(dtoClass, dt, "getFechaproceso"));
			busDTO.setOperacion_fech_proc_transaccio(new java.sql.Date(utilDate.getTime()));
			SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			java.util.Date parsedDate = inputFormat.parse(ReflectionUtils.getStringValue(dtoClass, dt, "getFechatransaccion"));
			Timestamp timestamp = new Timestamp(parsedDate.getTime());
			busDTO.setOperacion_fech_trans(timestamp);
			busDTO.setOperacion_form_pago(ReflectionUtils.getStringValue(dtoClass, dt, "getFormapago"));
			busDTO.setOperacion_id_promocion(ReflectionUtils.getStringValue(dtoClass, dt, "getIdpromocion"));
			busDTO.setOperacion_ident_cajero_atm(ReflectionUtils.getStringValue(dtoClass, dt, "getIdentificadorcajeroatm"));
			busDTO.setOperacion_mod_entrada(ReflectionUtils.getStringValue(dtoClass, dt, "getModoentrada"));
			busDTO.setOperacion_mont_cotizacion(ReflectionUtils.getBigDecimalValue(dtoClass, dt, "getMontocotizacion"));
			busDTO.setOperacion_mont_destino(ReflectionUtils.getBigDecimalValue(dtoClass, dt, "getMontodestino"));
			busDTO.setOperacion_mont_origen(ReflectionUtils.getBigDecimalValue(dtoClass, dt, "getMontoorigen"));
			busDTO.setOperacion_num_adherente(ReflectionUtils.getShortValue(dtoClass, dt, "getNumeroadherente"));
			busDTO.setOperacion_num_tarjeta(ReflectionUtils.getStringValue(dtoClass, dt, "getNumerotarjeta"));
			busDTO.setOperacion_num_tarjeta_enmascar(ReflectionUtils.getStringValue(dtoClass, dt, "getNumerotarjetaofuscado"));
			busDTO.setOperacion_oper_destino(ReflectionUtils.getIntegerValue(dtoClass, dt, "getOperadortelefonicodestino"));
			busDTO.setOperacion_opera_origen(ReflectionUtils.getIntegerValue(dtoClass, dt, "getOperadortelefonicoorigen"));
			busDTO.setOperacion_proce_adquiriente(ReflectionUtils.getShortValue(dtoClass, dt, "getProcesadoradquiriente"));
			busDTO.setOperacion_proce_emisor(ReflectionUtils.getShortValue(dtoClass, dt, "getProcesadoremisor"));
			busDTO.setOperacion_ptos_transaccion(ReflectionUtils.getIntegerValue(dtoClass, dt, "getPuntostransaccion"));
			busDTO.setOperacion_rrnbepsa(ReflectionUtils.getStringValue(dtoClass, dt, "getRrnboleta"));
			busDTO.setOperacion_sald_actual(ReflectionUtils.getBigDecimalValue(dtoClass, dt, "getSaldoactual"));
			busDTO.setOperacion_sald_disp(ReflectionUtils.getBigDecimalValue(dtoClass, dt, "getSaldodisponible"));
			busDTO.setOperacion_telef_destino(ReflectionUtils.getStringValue(dtoClass, dt, "getNrotelefonodestino"));
			busDTO.setOperacion_telef_origen(ReflectionUtils.getStringValue(dtoClass, dt, "getNrotelefonoorigen"));
			busDTO.setOperacion_tip_dispositivo(ReflectionUtils.getStringValue(dtoClass, dt, "getTipodispositivo"));
			busDTO.setOperacion_tip_uso_tarjeta(ReflectionUtils.getStringValue(dtoClass, dt, "getTipotarjeta"));
			busDTO.setOperacion_venc_tarjeta(ReflectionUtils.getIntegerValue(dtoClass, dt, "getVencimientotarjeta"));
			Date currentDate = new Date(System.currentTimeMillis());
			busDTO.setOperacion_fech_insercion(currentDate); // no tiene
			busDTO.setOperacion_hor_insercion(new java.sql.Timestamp(System.currentTimeMillis())); //

		} catch (Exception e) {
			e.printStackTrace();
		}
		return busDTO;
	}
	
	
	private void enviarCorreo(String header ,String body) {
		try {
			if(AppConfig.CONFFROMPROP) {
				emailService = new EmailService();
			}else {
				emailService = new EmailService(as400DataSource);				
			}
			emailService.sendEmail(header,body);
		} catch (Exception e) {
			e.printStackTrace();
			LoggerUtil.detalle("Correo BackDatos",e);
		}
	}

}
