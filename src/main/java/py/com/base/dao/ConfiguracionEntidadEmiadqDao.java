package py.com.base.dao;

import java.util.List;

import javax.sql.DataSource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import py.com.base.dto.configuracion_entidad_emiadqDto;


@Component
public class ConfiguracionEntidadEmiadqDao {

	private final JdbcTemplate jdbcTemplate;


	  public ConfiguracionEntidadEmiadqDao(DataSource dataSource) {
	        this.jdbcTemplate = new JdbcTemplate(dataSource); 
	    }


	public List<configuracion_entidad_emiadqDto> findByConfiguracionEstado(String configuracionEstado) {
		String sql = "SELECT * FROM GXFINDTA.configuracion_entidad_emiadq WHERE configuracion_estado = ?";
		return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(configuracion_entidad_emiadqDto.class),
				configuracionEstado);
	}


	public List<configuracion_entidad_emiadqDto> findByConfiguracionEstadoModo(String configuracionEstado,
			String configuracionModo) {
		String sql = "SELECT * FROM GXFINDTA.configuracion_entidad_emiadq WHERE configuracion_estado = ? AND configuracion_modo = ?";
		return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(configuracion_entidad_emiadqDto.class),
				configuracionEstado, configuracionModo);
	}

}
