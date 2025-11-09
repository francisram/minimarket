package py.com.base.clases;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import py.com.base.utils.AppConfig;
import py.com.base.utils.LoggerUtil;

/**
 * Genera dinamicamente los DTOs basados en la metadata de las columnas
 * proporcionadas por el TableMetadataRepository
 */
public class DtoGenerator {

	public void generateDto(String className, List<ColumnInfo> columns,boolean serializable) {
		StringBuilder classContent = new StringBuilder();
		Set<String> uniqueColumns = new HashSet<>();

		classContent.append("package py.com.base.dto;\n");
		classContent.append("import java.io.Serializable;\n");
		classContent.append("import java.math.BigDecimal;\n");
		classContent.append("import java.sql.Date;\n");
		classContent.append("import java.sql.Timestamp;\n");
		if(serializable) {
			classContent.append("public class ").append(className).append("DtoDynamic implements Serializable {\n");
			classContent.append("    private static final long serialVersionUID = 1L;\n");			
		}else {
			classContent.append("public class ").append(className).append("DtoDynamic {\n");
		}
		
		for (ColumnInfo column : columns) {
			String columnName = column.getColumnName().toLowerCase();
			if (uniqueColumns.add(columnName)) { // Solo se agrega si no esta presente en el Set
				classContent.append("    private ");
				classContent.append(getJavaDataType(column.getDataType(), column.getColumnSize()));
				String capitalizedColumnName = columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
				classContent.append(" ").append(columnName).append(";\n");
				
			}
		}


		classContent.append("\n    public ").append(className).append("DtoDynamic() {\n");
		classContent.append("    }\n");


		for (String columnName : uniqueColumns) {
			ColumnInfo column = columns.stream().filter(c -> c.getColumnName().equalsIgnoreCase(columnName)).findFirst()
					.orElse(null);

			if (column != null) {
				String javaType = getJavaDataType(column.getDataType(), column.getColumnSize());
				String capitalizedColumnName = columnName.substring(0, 1).toUpperCase() + columnName.substring(1);

				classContent.append("\n    public ").append(javaType).append(" get").append(capitalizedColumnName)
						.append("() {\n");
				classContent.append("        return ").append(columnName).append(";\n");
				classContent.append("    }\n");

				classContent.append("\n    public void set").append(capitalizedColumnName).append("(").append(javaType)
						.append(" ").append(columnName).append(") {\n");
				classContent.append("        this.").append(columnName).append(" = ").append(columnName).append(";\n");
				classContent.append("    }\n");
			}
		}


		
		classContent.append("\n    @Override\n");
		classContent.append("    public String toString() {\n");
		classContent.append("        return \"").append(className).append("DtoDynamic [\" +\n");
		for (String columnName : uniqueColumns) {
		    String capitalizedColumnName = columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
		    classContent.append("            \"").append(columnName).append("='\" + ").append(columnName)
		            .append(" + \"'\" +\n");
		}
		classContent.append("            ']';\n");
		classContent.append("    }\n");
		
		
		classContent.append("}\n");


        String currentDir = System.getProperty("user.dir");
        File directory = new File(currentDir, "busdatosv2Folder/dto/py/com/bepsa/dto/");
        if (!directory.exists()) {
            directory.mkdirs(); 
        }


        File file = new File(directory, className + "DtoDynamic.java");
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(classContent.toString());
        } catch (IOException e) {
        	//LogUtil.setException(AppConfig.APP_NAME, "Error al generar Dto", e.getMessage());
        	LoggerUtil.detalle("Error al generar Dto", e);
        }
		

        boolean isCompiled = compileJavaFile(file.getPath());
	}
	
	  private boolean compileJavaFile(String javaFilePath) {
	        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	        int result = compiler.run(null, null, null, javaFilePath);
	        return result == 0; 
	    }

	private String getJavaDataType(String sqlDataType, int size) {

		
		switch (sqlDataType.toUpperCase()) {
		case "INTEGER":
			return "Integer";
		case "BIGINT":
			return "long";
		case "SMALLINT":
			return "short";
		case "NUMERIC":
		case "DECIMAL":
			return "java.math.BigDecimal";
		case "VARCHAR":
		case "CHAR":
		case "CHARACTER":
		case "CHARACTER VARYING":
			return "String";
		case "DATE":
			return "java.sql.Date";
		case "TIMESTAMP":
		case "TIMESTAMP WITHOUT TIME ZONE":
			return "java.sql.Timestamp";
		case "TIMESTAMP WITH TIME ZONE": 
			return "java.time.Instant"; 
		default:
			return "String";
		}
		

		
	}
}
