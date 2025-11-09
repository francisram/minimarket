package py.com.base.utils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReflectionUtils {


    public static String getStringValue(Class<?> dtoClass, Object dto, String methodName) {
        try {
            Method method = dtoClass.getMethod(methodName); 
            return (String) method.invoke(dto); 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Integer getIntegerValue(Class<?> dtoClass, Object dto, String methodName) {
        try {
            Method method = dtoClass.getMethod(methodName);
            Object result = method.invoke(dto);
            
            if (result instanceof Integer) {
                return (Integer) result;  
            } else if (result instanceof String) {

                String strValue = (String) result;
                if (strValue != null && !strValue.isEmpty()) {
                    return Integer.valueOf(strValue); 
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;  
    }



    public static Short getShortValue(Class<?> dtoClass, Object dto, String methodName) {
        try {
            Method method = dtoClass.getMethod(methodName);
            Object result = method.invoke(dto);
            
            if (result instanceof Short) {
                return (Short) result;
            } else if (result instanceof String) {

                String strValue = (String) result;
                if (strValue != null && !strValue.isEmpty()) {
                    return Short.valueOf(strValue);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public static BigDecimal getBigDecimalValue(Class<?> dtoClass, Object dto, String methodName) {
        try {
            Method method = dtoClass.getMethod(methodName);
            Object result = method.invoke(dto);

            if (result instanceof BigDecimal) {
                return (BigDecimal) result;  
            } else if (result instanceof String) {

                String strValue = (String) result;
                if (strValue != null && !strValue.isEmpty()) {
                    return new BigDecimal(strValue);  
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; 
    }



    public static Timestamp getTimestampValue(Class<?> dtoClass, Object dto, String methodName) {
        try {
            Method method = dtoClass.getMethod(methodName);
            return (Timestamp) method.invoke(dto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Date parseSqlDate(Class<?> dtoClass, Object dto, String methodName, String format) {
        try {
            Method method = dtoClass.getMethod(methodName);
            String dateString = (String) method.invoke(dto);
            if (dateString != null && !dateString.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                return sdf.parse(dateString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Timestamp parseTimestamp(Class<?> dtoClass, Object dto, String methodName, String format) {
        try {
            Method method = dtoClass.getMethod(methodName);
            String dateString = (String) method.invoke(dto);
            if (dateString != null && !dateString.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                Date parsedDate = sdf.parse(dateString);
                return new Timestamp(parsedDate.getTime());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
