package py.com.base.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import py.com.base.interfaces.ClientQueService;
import py.com.base.utils.LogUtil;
import py.com.base.utils.LoggerUtil;
import jakarta.annotation.PreDestroy;

@Service
public class ClientQueServiceImpl implements ClientQueService {

    private static final String COLA_DESTINO = "BEPSA.BUS.TRX.";
    
    private static ClientQueServiceImpl instance;

    private final JmsTemplate jmsTemplate;

    @Autowired
    public ClientQueServiceImpl(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
        instance = this;
    }
    
    public static ClientQueServiceImpl getInstance() {
        if (instance == null) {
            throw new IllegalStateException("El bean ClientQueServiceImpl no ha sido inicializado por Spring.");
        }
        return instance;
    }

    public void responde() {
        LoggerUtil.importante("responde");
    }

    public boolean publicarMensaje(String entidad, String mensaje, String paramQ, String lugar) {
        boolean exito = false;
        int intentos = 0;
        int maxIntentos = 3;

        while (intentos < maxIntentos) {
            try {
                jmsTemplate.setPubSubDomain(false); // true = topic, false = queue
                jmsTemplate.convertAndSend(COLA_DESTINO + entidad, mensaje);
                exito = true;
                break;
            } catch (Exception e) {
                intentos++;
                LogUtil.error("Intento " + intentos + " fallido en " + lugar + 
                              " QUEUE: " + paramQ + " | " + e.getMessage());
                if (intentos < maxIntentos) {
                    try {
                        Thread.sleep(1000); // Esperar 1 segundo antes del próximo intento
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        return exito;
    }

    @PreDestroy
    public void cerrarRecursos() {
        LogUtil.info("Cerrando recursos de JMS...");
        // Aquí puedes cerrar conexiones si es necesario
    }

    @Override
    public void addOrder(String entidad, String msg) {
        // Implementación futura
    }
}
