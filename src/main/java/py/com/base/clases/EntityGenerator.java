package py.com.base.clases;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Genera dinamicamente las entidades Java basadas en la metadata de las
 * columnas proporcionadas por el TableMetadataRepository
 */
public class EntityGenerator {

    public void generateEntity(String className, List<ColumnInfo> columns, String tableName) {
        StringBuilder classContent = new StringBuilder();
        Set<String> uniqueColumns = new HashSet<>();

        classContent.append("package py.com.base.entities;\n");
        classContent.append("import jakarta.persistence.Entity;\n ");
        classContent.append("import jakarta.persistence.Table;\n");
        classContent.append("import jakarta.persistence.Column;\n");
        classContent.append("import java.math.BigDecimal;\n");
        classContent.append("import java.sql.Date;\n");
        classContent.append("import java.sql.Timestamp;\n");
        classContent.append("@Entity\n");
        classContent.append("@Table(name = \"").append(tableName).append("\")\n");
        classContent.append("public class ").append(className).append(" {\n");


        for (ColumnInfo column : columns) {
            String columnName = column.getColumnName().toLowerCase();
            if (uniqueColumns.add(columnName)) {  
                classContent.append("    @Column(name = \"").append(column.getColumnName()).append("\"");
                if ("NO".equalsIgnoreCase(column.getIsNullable())) {
                    classContent.append(", nullable = false");
                }
                if (column.getColumnSize() > 0) {
                    classContent.append(", length = ").append(column.getColumnSize());
                }
                classContent.append(")\n");
                classContent.append("    private ");
                classContent.append(getJavaDataType(column.getDataType(), column.getColumnSize()));
                classContent.append(" ").append(columnName).append(";\n");
            }
        }
        

        classContent.append("\n    public ").append(className).append("() {\n");
        classContent.append("    }\n");


        for (String columnName : uniqueColumns) {
            ColumnInfo column = columns.stream()
                    .filter(c -> c.getColumnName().equalsIgnoreCase(columnName))
                    .findFirst()
                    .orElse(null);
            
            if (column != null) {
                String javaType = getJavaDataType(column.getDataType(), column.getColumnSize());
                String capitalizedColumnName = columnName.substring(0, 1).toUpperCase() + columnName.substring(1);

                classContent.append("\n    public ").append(javaType).append(" get").append(capitalizedColumnName).append("() {\n");
                classContent.append("        return ").append(columnName).append(";\n");
                classContent.append("    }\n");

                classContent.append("\n    public void set").append(capitalizedColumnName).append("(").append(javaType).append(" ").append(columnName).append(") {\n");
                classContent.append("        this.").append(columnName).append(" = ").append(columnName).append(";\n");
                classContent.append("    }\n");
            }
        }

        classContent.append("}\n");


        String entityCode = classContent.toString();

        File file = new File("src/main/java/py/com/bepsa/entities/" + className + ".java");
        try (FileWriter fileWriter = new FileWriter(file) ) {
            fileWriter.write(classContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                return "java.math.BigDecimal";
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
            case "1": case "12": case "15": case "80": case "30": case "50":
            case "100": case "19": case "6": case "2": case "4": case "10000":
                return "String";
            case "3":
                return "java.math.BigDecimal";
            default:
                return "String";
        }
    }
}
