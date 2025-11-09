package py.com.base.services;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import ch.qos.logback.classic.Logger;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import py.com.base.clases.EmailConfig;
import py.com.base.impl.EmailConfigRepositoryImpl;
import py.com.base.interfaces.EmailConfigRepository;
import py.com.base.utils.AppConfig;
import py.com.base.utils.LogUtil;
import py.com.base.utils.LoggerUtil;

/**
 * Servicio para el envío de correos electrónicos.
 * 
 * <p>Permite enviar correos electrónicos utilizando una configuración obtenida
 * desde una base de datos o desde propiedades de la aplicación.</p>
 */
public class EmailService {

    private String host;
    private String port;
    private String userName;
    private String password;
    private boolean isConfigured = false;

    /**
     * Constructor que inicializa el servicio de correo electrónico con la configuración obtenida desde AS400.
     * 
     * @param as400DataSource Fuente de datos para conectarse a la base de datos AS400.
     * @throws Exception Si ocurre un error al obtener la configuración del correo.
     */
    public EmailService(DataSource as400DataSource) throws Exception {
    	EmailConfigRepository emailConfigRepository = new EmailConfigRepositoryImpl(as400DataSource);
        EmailConfig config = emailConfigRepository.findByConnectionId("MAILAS");
        if (config != null) {
            this.host = config.getHost().trim();
            this.port = config.getPort().trim();
            this.userName = config.getUsername();
            this.password = config.getPassword().trim()   ;
            this.isConfigured = true;
        }
    }
    
    /**
     * Constructor que inicializa el servicio de correo electrónico con la configuración obtenida desde las propiedades de la aplicación.
     * 
     * @throws Exception Si ocurre un error al obtener la configuración del correo.
     */
    public EmailService() throws Exception {
    	EmailConfigRepository emailConfigRepository = new EmailConfigRepositoryImpl();
    	EmailConfig config = emailConfigRepository.findFromProperties();
    	if (config != null) {
    		this.host = config.getHost().trim();
    		this.port = config.getPort().trim();
    		this.userName = config.getUsername().trim();
    		this.password = config.getPassword().trim();
    		this.isConfigured = true;
    	}
    }


    /**
     * Envía un correo electrónico a una lista de destinatarios definida en {@link AppConfig#CORREOS}.
     * 
     * @param subject Asunto del correo electrónico.
     * @param body Cuerpo del correo en formato HTML.
     * @throws MessagingException Si ocurre un error al enviar el correo.
     * @throws IllegalStateException Si el servicio no está configurado correctamente.
     */
    public void sendEmail(String subject, String body ) throws MessagingException {

    	List<String> correos = Arrays.asList(AppConfig.CORREOS.split(","));
    	
    	
        if (!isConfigured) {
            throw new IllegalStateException("EmailService is not configured.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        });

        for (String toEmail : correos) {
        	
        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(AppConfig.EMAILFROM, "Notificacion Bus de Datos"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(body, "text/html");

            Transport.send(message);
            LoggerUtil.detalle("Correo enviado exitosamente.");
        } catch (MessagingException e) {
            throw new MessagingException("Error al enviar correo", e);
        } catch (UnsupportedEncodingException e) {
        	LogUtil.error(e.getMessage(), e);
		}
	}
    }
}

