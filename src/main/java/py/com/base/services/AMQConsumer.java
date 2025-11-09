package py.com.base.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.DataSource;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import py.com.base.utils.AppConfig;
import py.com.base.utils.LogUtil;
import py.com.base.utils.LoggerUtil;

/**
 * @author frlopez Consumidor de mensajes de una cola AMQ.
 *         <p>
 *         Esta clase escucha mensajes de una cola ActiveMQ, los procesa y
 *         maneja errores utilizando un enfoque asincrono. Ademas, envia correos
 *         electronicos en caso de fallos.
 *         </p>
 */

@Component
@EnableScheduling
public class AMQConsumer {
	private EmailService emailService;
	private final DataSource as400DataSource;
	private final AtomicLong messageCounter = new AtomicLong(0);

	/**
	 * Metodo programado que se conecta a AMQ cada 30 minutos y procesa mensajes.
	 */

	// @Scheduled(cron = "0 0/5 * * * ?") // Se ejecuta cada 30 minutos
	
	@Value("${reprocess.waittime}")
	private int tiempoEsperaReproceso;
	
	@Scheduled(fixedRateString = "#{${reprocess.waittime} * 60 * 1000}")
	public void scheduledAMQConnection() {
		LogUtil.info("Ejecutando AMQConnection para Reproceso...");
		LogUtil.info("Ejecutando reproceso fallidos cada " + (tiempoEsperaReproceso * 60 * 1000) + " ms");
		LocalDateTime fechaHoraActual = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(AppConfig.AMQ_USER,	AppConfig.AMQ_PASSWORD, AppConfig.BROKER_URL);

		try (jakarta.jms.Connection connection = connectionFactory.createConnection();
				Connection con = as400DataSource.getConnection();
				Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);) {
			LogUtil.info("Conectandose a AMQ :" + fechaHoraActual.format(formatter));
			connection.start();
			jakarta.jms.Queue queue = session.createQueue(AppConfig.CLIENTEREPROCESO);
			MessageConsumer consumer = session.createConsumer(queue);
			TextMessage message;
			while ((message = (TextMessage) consumer.receive(5000)) != null) {
				long currentCount = messageCounter.incrementAndGet();
				LogUtil.info("Mensaje recibido numero " + currentCount + ": " + message.getText());
				//guardarMensajeFallido(message.getText());
				processMessage(con, message.getText());
			}
			consumer.close();
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Procesa un mensaje recibido de la cola.
	 * <p>
	 * Este metodo realiza una llamada a una funcion en la base de datos utilizando
	 * el mensaje recibido como parametro. Segun el resultado de la funcion:
	 * <ul>
	 * <li>Si la funcion retorna un numero, se considera un resultado esperado.</li>
	 * <li>Si la funcion retorna un error (como texto), se envia un correo con los
	 * detalles.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param message el mensaje recibido que se procesa.
	 */
	public void processMessage(Connection connection, String message) {

		String fechabruta;
		String punto;
		String hora;
		String rrn;
		String lugar;
		String campo = "";
		
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(message);

			fechabruta = jsonNode.get("fecha").asText().trim();
			punto = jsonNode.get("punto").asText().trim();
			hora = jsonNode.get("hora").asText().trim();
			rrn = jsonNode.get("rrn").asText().trim();
			lugar = jsonNode.get("lugar").asText().trim();
			
			if("amq".equals(lugar.toLowerCase())) {campo = "AUDENVIAENTIDAD";}
			if("ptc".equals(lugar.toLowerCase())) {campo = "AUDENVIAPORTAL";}
			if("inicial".equals(lugar.toLowerCase())) {campo = "AUDESTPOG";}

			String queryStringPuntoS = String.format("UPDATE GXFINDTA.TBDAUD SET %s = 'S' WHERE TRIM(AUDRNB) = ? AND AUDTRXFCHC = ? AND RTRIM(AUDFCHT) = ?",campo);
			String queryStringPuntoX = String.format("UPDATE GXFINDTA.TBDAUD SET %s = 'X' WHERE TRIM(AUDRNB) = ? AND AUDTRXFCHC = ? AND RTRIM(AUDFCHT) = ?",campo);
			String queryStringPuntoE = String.format("UPDATE GXFINDTA.TBDAUD SET %s = 'E' WHERE TRIM(AUDRNB) = ? AND AUDTRXFCHF = ? AND RTRIM(AUDFCHT) = ?",campo);
			String queryStringPuntoC = String.format("UPDATE GXFINDTA.TBDAUD SET %s = 'C' WHERE TRIM(AUDRNB) = ? AND AUDTRXFCHF = ? AND RTRIM(AUDFCHT) = ?",campo);

			String queryString = "";
			switch (punto.toLowerCase()) {
			case "e":
				queryString = queryStringPuntoE;
				break;
			case "s":
				queryString = queryStringPuntoS;
				break;
			case "c":
				queryString = queryStringPuntoC;
				break;
			case "x":
				queryString = queryStringPuntoX;
				break;
			default:
				LogUtil.info("Opcion no valida en reprocesado");
				return;
			}

			int maxReintentos = AppConfig.MAXRETRIES;
			int intento = 0;
			boolean exito = false;

			while (intento < maxReintentos && !exito) {
				intento++;
				try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

					String fecha = fechabruta.trim().substring(0, 8);
					preparedStatement.setString(1, rrn);
					preparedStatement.setString(2, fecha);
					preparedStatement.setString(3, hora);
					int rowsUpdated = preparedStatement.executeUpdate();

					if (rowsUpdated > 0) {
						LoggerUtil.importante("Registro reprocesado exitosamente a estado " + punto + " | " + message);
						exito = true;
					} else {
						LoggerUtil.importante(
								"No se encontro ningun registro para reprocesar a estado " + punto + " | " + message);
						exito = true;
					}

				} catch (SQLException e) {
					LogUtil.error("Intento " + intento	+ " - Error de conexion a la base de datos. Reintentando en 15 minutos. " + "Error: "	+ e.getMessage());

					if (intento < maxReintentos) {
						try {
							Thread.sleep(30000); // Espera 15 minutos antes de reintentar (900000 ms)
						} catch (InterruptedException ie) {
							Thread.currentThread().interrupt();
							LogUtil.error("Reintento interrumpido: " + ie.getMessage());
							return;
						}
					} else {
						LogUtil.error("Todos los intentos de conexion a la base de datos han fallado.");
						JSONObject jsonObject = new JSONObject(message);
						String mensajeCompacto = jsonObject.toString();
						guardarMensajeFallido(mensajeCompacto);
					}
				}
			}

		} catch (Exception e) {
			LogUtil.error("Error al parsear el JSON: " + e.getMessage());
			return;
		}

	}



	/**
	 * Constructor que inicializa el consumidor AMQ.
	 * 
	 * @param postgresDataSource la fuente de datos PostgreSQL utilizada para
	 *                           realizar consultas.
	 */
	public AMQConsumer(DataSource as400DataSource) {
		this.as400DataSource = as400DataSource;
	}

	private void guardarMensajeFallido(String message) {
		LogUtil.info("Intentando guardar mensaje fallido...");
		String filePath = System.getProperty("user.dir") + File.separator + "mensajes_fallidos.txt";

		int intentos = AppConfig.MAXRETRIES;
		while (intentos > 0) {
			try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw"); FileChannel channel = raf.getChannel()) {

				FileLock lock = channel.tryLock();
				if (lock == null) {
					LogUtil.warn("Archivo en uso, reintentando en 1 segundo...");
					Thread.sleep(2000);
					intentos--;
					continue;
				}

				try {
					raf.seek(raf.length()); 
					JSONObject jsonObject = new JSONObject(message);
					raf.writeBytes(jsonObject.toString() + System.lineSeparator());
					LogUtil.info("Mensaje guardado correctamente.");
				} finally {
					lock.release();
				}
				return;
			} catch (IOException | InterruptedException e) {
				LogUtil.error("Error al guardar mensaje fallido: " + e.getMessage());
				e.printStackTrace();
			}
		}
		LogUtil.error("No se pudo guardar el mensaje tras varios intentos.");
	}

	@Value("${reprocess.failedreprocess.waittime}")
	private int tiempoEsperaFallido;

	@Scheduled(fixedRateString = "#{${reprocess.failedreprocess.waittime} * 60 * 1000}")
	public void reprocesarMensajesFallidos() {
		LogUtil.info("Ejecutando reproceso de reprocesos fallidos cada " + (tiempoEsperaFallido * 60 * 1000) + " ms");
	    String filePath = System.getProperty("user.dir") + File.separator + "mensajes_fallidos.txt";
	    File file = new File(filePath);

	    if (!file.exists()) {
	        LogUtil.info("No hay mensajes fallidos para reprocesar.");
	        return;
	    }

	    List<String> mensajesNoProcesados = new ArrayList<>();

	    // Leer los mensajes antes de vaciar el archivo
	    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
	        String linea;
	        while ((linea = reader.readLine()) != null) {
	            mensajesNoProcesados.add(linea);
	        }
	    } catch (IOException e) {
	        LogUtil.error("Error al leer el archivo de mensajes fallidos: " + e);
	        return;
	    }

	    // Ahora intentamos vaciar el archivo
	    boolean archivoVaciado = false;
	    int intentos = AppConfig.MAXRETRIES;

	    while (intentos > 0) {
	        try (RandomAccessFile raf = new RandomAccessFile(file, "rw"); FileChannel channel = raf.getChannel()) {
	            FileLock lock = channel.tryLock();

	            if (lock != null) {
	                LogUtil.info("Archivo desbloqueado y listo para vaciar.");
	                raf.setLength(0);  // Vaciar archivo
	                lock.release();
	                archivoVaciado = true;
	                break;
	            } else {
	                LogUtil.warn("El archivo está en uso, reintentando en 2 segundos...");
	                Thread.sleep(2000);
	            }
	        } catch (IOException | InterruptedException e) {
	            LogUtil.error("Error al vaciar el archivo de mensajes fallidos: " + e.getMessage());
	        }
	        intentos--;
	    }

	    if (!archivoVaciado) {
	        LogUtil.error("No se pudo obtener el acceso exclusivo al archivo.");
	        return;
	    }

	    // Procesar los mensajes en memoria
	    for (String mensaje : mensajesNoProcesados) {
	        try (Connection connection = as400DataSource.getConnection()) {
	            LogUtil.info("Reintentando mensaje fallido: " + mensaje);
	            processMessage(connection, mensaje);
	        } catch (SQLException e) {
	            LogUtil.error("Error reprocesando mensaje: " + mensaje + " - " + e.getMessage());
	        }
	    }
	}


	


	
}
