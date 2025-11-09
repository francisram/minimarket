package py.com.base.clases;


/**
 * Es una clase de datos (DTO) que encapsula la informacion de las columnas de una tabla: nombre de la columna, tipo de dato, tamaño, y si es nulo o no.
 */
public class ColumnInfo {

    private String columnName;
    public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public int getColumnSize() {
		return columnSize;
	}

	public void setColumnSize(int columnSize) {
		this.columnSize = columnSize;
	}

	public String getIsNullable() {
		return isNullable;
	}

	public void setIsNullable(String isNullable) {
		this.isNullable = isNullable;
	}

	private String dataType;
    private int columnSize;
    private String isNullable;

    // Constructor, getters, and setters
    public ColumnInfo(String columnName, String dataType, int columnSize, String isNullable) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.columnSize = columnSize;
        this.isNullable = isNullable;
    }


    
    

    
}
