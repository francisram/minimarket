package py.com.base.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LoggerUtil {

	private static boolean detallesHabilitados = false;
	private static boolean seqHabilitado = false;

	public static void setDetallesHabilitados(boolean habilitar) {
		detallesHabilitados = habilitar;
	}

	public static void habilitarSeq(boolean habilitar) {
		seqHabilitado = habilitar;
	}

	public static void detalle(String mensaje) {
		if (detallesHabilitados) {
			LogUtil.info("[DETALLE] " + mensaje);
		}
	}

	public static void detalle(String mensaje, Throwable exception) {
		if (detallesHabilitados) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			exception.printStackTrace(pw);
			LogUtil.info("[DETALLE] " + mensaje);

		}
	}

	public static void importante(String mensaje) {
		LogUtil.info("[IMPORTANTE] " + mensaje);
	}
	



}
