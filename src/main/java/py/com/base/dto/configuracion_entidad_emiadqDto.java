package py.com.base.dto;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
public class configuracion_entidad_emiadqDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private String configuracion_id;
    private String configuracion_cod_entidad_destino;
    private String configuracion_destino;
    private String configuracion_tipo;
    private String configuracion_estado;
    private String configuracion_fecha_alta;
    private String configuracion_fecha_modi;
    private String configuracion_modo;
    private String configuracion_url;
    private String configuracion_header;

    public configuracion_entidad_emiadqDto() {
    }

    public String getConfiguracion_cod_entidad_destino() {
        return configuracion_cod_entidad_destino;
    }

    public void setConfiguracion_cod_entidad_destino(String configuracion_cod_entidad_destino) {
        this.configuracion_cod_entidad_destino = configuracion_cod_entidad_destino;
    }

    public String getConfiguracion_fecha_modi() {
        return configuracion_fecha_modi;
    }

    public void setConfiguracion_fecha_modi(String configuracion_fecha_modi) {
        this.configuracion_fecha_modi = configuracion_fecha_modi;
    }

    public String getConfiguracion_destino() {
        return configuracion_destino;
    }

    public void setConfiguracion_destino(String configuracion_destino) {
        this.configuracion_destino = configuracion_destino;
    }

    public String getConfiguracion_estado() {
        return configuracion_estado;
    }

    public void setConfiguracion_estado(String configuracion_estado) {
        this.configuracion_estado = configuracion_estado;
    }

    public String getConfiguracion_fecha_alta() {
        return configuracion_fecha_alta;
    }

    public void setConfiguracion_fecha_alta(String configuracion_fecha_alta) {
        this.configuracion_fecha_alta = configuracion_fecha_alta;
    }

    public String getConfiguracion_modo() {
        return configuracion_modo;
    }

    public void setConfiguracion_modo(String configuracion_modo) {
        this.configuracion_modo = configuracion_modo;
    }

    public String getConfiguracion_url() {
        return configuracion_url;
    }

    public void setConfiguracion_url(String configuracion_url) {
        this.configuracion_url = configuracion_url;
    }

    public String getConfiguracion_id() {
        return configuracion_id;
    }

    public void setConfiguracion_id(String configuracion_id) {
        this.configuracion_id = configuracion_id;
    }

    public String getConfiguracion_header() {
        return configuracion_header;
    }

    public void setConfiguracion_header(String configuracion_header) {
        this.configuracion_header = configuracion_header;
    }

    public String getConfiguracion_tipo() {
        return configuracion_tipo;
    }

    public void setConfiguracion_tipo(String configuracion_tipo) {
        this.configuracion_tipo = configuracion_tipo;
    }

    @Override
    public String toString() {
        return "configuracion_entidad_emiadqDto [" +
            "configuracion_cod_entidad_destino='" + configuracion_cod_entidad_destino + "'" +
            "configuracion_fecha_modi='" + configuracion_fecha_modi + "'" +
            "configuracion_destino='" + configuracion_destino + "'" +
            "configuracion_estado='" + configuracion_estado + "'" +
            "configuracion_fecha_alta='" + configuracion_fecha_alta + "'" +
            "configuracion_modo='" + configuracion_modo + "'" +
            "configuracion_url='" + configuracion_url + "'" +
            "configuracion_id='" + configuracion_id + "'" +
            "configuracion_header='" + configuracion_header + "'" +
            "configuracion_tipo='" + configuracion_tipo + "'" +
            ']';
    }
}
