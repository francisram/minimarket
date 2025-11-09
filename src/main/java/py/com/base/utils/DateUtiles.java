package py.com.base.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtiles {

	public static final String fechaJson(Date fecha) {
		return new SimpleDateFormat("yyyy-MM-dd").format(fecha);
	}
	
	public static final String fechaHoraJson(Date fecha) {
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(fecha);
	} 
}
