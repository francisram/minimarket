package py.com.base.clases;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


/**
 * Esta clase maneja la logica para obtener la metadata de las columnas de tablas en las bases de datos AS400 y PostgreSQL.
 */


@Repository
public class TableMetadataRepository {

	@Autowired
	@Qualifier("as400JdbcTemplate")
	private  JdbcTemplate as400jdbcTemplate;

	@Autowired
	@Qualifier("postgresJdbcTemplate2")
	private  JdbcTemplate jdbcTemplatePostgres2;

	public TableMetadataRepository(
									@Qualifier("as400JdbcTemplate") JdbcTemplate jdbcTemplate,
									@Qualifier("postgresJdbcTemplate2") JdbcTemplate jdbcTemplatePostgres2
								  ) 
								{
                                this.as400jdbcTemplate = jdbcTemplate;
                                this.jdbcTemplatePostgres2 = jdbcTemplatePostgres2;
                	            }

	@SuppressWarnings("deprecation")
	public List<ColumnInfo> getTableColumnsOfas400(String schema, String tableName) {
		String sql = "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_SIZE, IS_NULLABLE " + "FROM SYSIBM.SQLCOLUMNS "
				+ "WHERE TABLE_SCHEM = ? AND TABLE_NAME = ?";

		return as400jdbcTemplate.query(sql, new Object[] { schema, tableName },
				(rs, rowNum) -> new ColumnInfo(rs.getString("COLUMN_NAME"), rs.getString("DATA_TYPE"),
						rs.getInt("COLUMN_SIZE"), rs.getString("IS_NULLABLE")));
	}
	
	@SuppressWarnings("deprecation")
	public List<ColumnInfo> getTableColumnsOfPostgresDestino(String schema, String tableName) {
		String sql = "SELECT COLUMN_NAME, DATA_TYPE, character_maximum_length AS COLUMN_SIZE,"
				+ " IS_NULLABLE FROM information_schema.columns " + "WHERE table_name = ?";
		
		return jdbcTemplatePostgres2.query(sql, new Object[] { tableName },
				(rs, rowNum) -> new ColumnInfo(rs.getString("COLUMN_NAME"), rs.getString("DATA_TYPE"),
						rs.getInt("COLUMN_SIZE"), rs.getString("IS_NULLABLE")));
	}
}
