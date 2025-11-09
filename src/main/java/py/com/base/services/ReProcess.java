package py.com.base.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import py.com.base.utils.LogUtil;
import py.com.base.utils.LoggerUtil;

public class ReProcess {
	
    /**
     * Envía un mensaje a la cola de reproceso en AMQ con los parámetros especificados.
     * 
     * @param entidadDestino Identificador de la entidad destino en la cola.
     * @param paramQ Parámetro en formato de cadena separado por "|", que debe contener al menos tres partes:
     *               <ul>
     *                   <li>rrn (identificador numérico)</li>
     *                   <li>fecha (cadena de texto)</li>
     *                   <li>hora (cadena de texto)</li>
     *               </ul>
     * @param secuencia Secuencia del mensaje (no se usa en la implementación actual).
     * @param punto Identificador del punto (por defecto "S" si es null).
     * @param lugar Identificador del lugar (por defecto "inicial" si es null).
     * @throws IllegalArgumentException Si el formato de paramQ es incorrecto.
     */
	public static void enviarAReproceso(String entidadDestino, String paramQ, String secuencia,String punto ,String lugar) {
        ClientQueServiceImpl camq = ClientQueServiceImpl.getInstance();
        ObjectMapper objectMapper = new ObjectMapper();

        try {

            if (paramQ == null || paramQ.isEmpty() || paramQ.split("\\|").length < 3) {
                throw new IllegalArgumentException("Formato incorrecto de paramQ: " + paramQ);
            }

            String[] parts = paramQ.split("\\|");

            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("rrn", Long.parseLong(parts[0])); // Convertir a Long
            jsonMap.put("fecha", parts[1]);
            jsonMap.put("hora", parts[2]);
            jsonMap.put("punto", punto != null ? punto : "S"); // Usar el parametro punto o "S" por defecto
            jsonMap.put("lugar", lugar != null ? lugar : "inicial"); // Usar el parametro punto o "S" por defecto

            String jsonOutput = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);

            Boolean enviado = false;
            	try {
            		camq.publicarMensaje(String.valueOf(entidadDestino), jsonOutput, paramQ , "AMQ");
            		LoggerUtil.detalle("AMQ Sender : " + paramQ);
            		enviado = true;            		
            	}catch (Exception e) {
					LogUtil.error(e.getMessage());
				}

            if (!enviado) {
                String mensajeError = "ERROR ENVIAR A REPROCESO AMQ - No se ha podido enviar a la cola de reproceso: " + paramQ;
                LogUtil.info(mensajeError);
            }
        } catch (Exception e) {
            String errorDetalles = "Se produjo un error: " + e.getMessage() + "\nDetalles:\n" + Arrays.toString(e.getStackTrace());
            LogUtil.error(String.format("ERROR ENVIAR A REPROCESO AMQ: %s", errorDetalles));
            e.printStackTrace();
        }
	}


}
