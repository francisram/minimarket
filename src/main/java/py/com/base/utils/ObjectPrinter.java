package py.com.base.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ObjectPrinter {

	public static void printAttributes(Object obj) {
		if (obj == null) {
			LogUtil.info("El objeto es nulo.");
			return;
		}

		Class<?> objClass = obj.getClass();
		LogUtil.info("Atributos del objeto " + objClass.getName() + ":");

		while (objClass != null) {
			Field[] fields = objClass.getDeclaredFields();

			for (Field field : fields) {
				if (Modifier.isStatic(field.getModifiers())) {
                    continue; // Omitimos atributos estáticos
                }
				field.setAccessible(true);
				try {
					Object value = field.get(obj);
					LogUtil.info(field.getName() + " = " + (value != null ? value : "null"));
				} catch (IllegalAccessException e) {
					LogUtil.error("No se pudo acceder al atributo: " + field.getName());
				}
			}
			 objClass = objClass.getSuperclass(); 
		}

	}
}
