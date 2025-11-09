package py.com.base.interfaces;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import py.com.base.dto.configuracion_entidad_emiadqDto;
import py.com.base.entities.ConfiguracionEntidadEmiadq;



public interface IConfiguracionEntidadEmiadqRepository  {

    List<configuracion_entidad_emiadqDto> findByConfiguracionEstado(String configuracionEstado);
    
    List<configuracion_entidad_emiadqDto> findByConfiguracionEstadoModo(String configuracionEstado, String configuracionModo);
    
    
	
}
