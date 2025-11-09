package py.com.base.entities;


import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author dcolman
 */
@Entity
@Table(name = "configuracion_entidad_emiadq", schema = "GXFINDTA")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ConfiguracionEntidadEmiadq.findAll", query = "SELECT c FROM ConfiguracionEntidadEmiadq c"),
    @NamedQuery(name = "ConfiguracionEntidadEmiadq.findByConfiguracionId", query = "SELECT c FROM ConfiguracionEntidadEmiadq c WHERE c.configuracionId = :configuracionId"),
    @NamedQuery(name = "ConfiguracionEntidadEmiadq.findByConfiguracionCodEntidadDestino", query = "SELECT c FROM ConfiguracionEntidadEmiadq c WHERE c.configuracionCodEntidadDestino = :configuracionCodEntidadDestino"),
    @NamedQuery(name = "ConfiguracionEntidadEmiadq.findByConfiguracionDestino", query = "SELECT c FROM ConfiguracionEntidadEmiadq c WHERE c.configuracionDestino = :configuracionDestino"),
    @NamedQuery(name = "ConfiguracionEntidadEmiadq.findByConfiguracionTipo", query = "SELECT c FROM ConfiguracionEntidadEmiadq c WHERE c.configuracionTipo = :configuracionTipo"),
    @NamedQuery(name = "ConfiguracionEntidadEmiadq.findByConfiguracionEstado", query = "SELECT c FROM ConfiguracionEntidadEmiadq c WHERE c.configuracionEstado = :configuracionEstado"),
    @NamedQuery(name = "ConfiguracionEntidadEmiadq.findByConfiguracionEstadoModo", query = "SELECT c FROM ConfiguracionEntidadEmiadq c WHERE c.configuracionEstado = :configuracionEstado AND c.configuracionModo = :configuracionModo "),
    @NamedQuery(name = "ConfiguracionEntidadEmiadq.findByConfiguracionFechaAlta", query = "SELECT c FROM ConfiguracionEntidadEmiadq c WHERE c.configuracionFechaAlta = :configuracionFechaAlta"),
    @NamedQuery(name = "ConfiguracionEntidadEmiadq.findByConfiguracionFechaModi", query = "SELECT c FROM ConfiguracionEntidadEmiadq c WHERE c.configuracionFechaModi = :configuracionFechaModi")})
public class ConfiguracionEntidadEmiadq implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "configuracion_id")
    private Long configuracionId;
    @Column(name = "configuracion_cod_entidad_destino")
    private Integer configuracionCodEntidadDestino;
    @Column(name = "configuracion_destino")
    private String configuracionDestino;
    @Column(name = "configuracion_tipo")
    private String configuracionTipo;
    @Column(name = "configuracion_estado")
    private String configuracionEstado;
    @Column(name = "configuracion_fecha_alta")
    @Temporal(TemporalType.DATE)
    private Date configuracionFechaAlta;
    @Column(name = "configuracion_fecha_modi")
    @Temporal(TemporalType.DATE)
    private Date configuracionFechaModi;

    @Column(name = "configuracion_modo")
    private String configuracionModo;
    @Column(name = "configuracion_url")
    private String configuracionUrl;
    @Column(name = "configuracion_header")
    private String configuracionHeader;
    
    public ConfiguracionEntidadEmiadq() {
    }

    public ConfiguracionEntidadEmiadq(Long configuracionId) {
        this.configuracionId = configuracionId;
    }

    public Long getConfiguracionId() {
        return configuracionId;
    }

    public void setConfiguracionId(Long configuracionId) {
        this.configuracionId = configuracionId;
    }

    public Integer getConfiguracionCodEntidadDestino() {
        return configuracionCodEntidadDestino;
    }

    public void setConfiguracionCodEntidadDestino(Integer configuracionCodEntidadDestino) {
        this.configuracionCodEntidadDestino = configuracionCodEntidadDestino;
    }

    public String getConfiguracionDestino() {
        return configuracionDestino;
    }

    public void setConfiguracionDestino(String configuracionDestino) {
        this.configuracionDestino = configuracionDestino;
    }

    public String getConfiguracionTipo() {
        return configuracionTipo;
    }

    public void setConfiguracionTipo(String configuracionTipo) {
        this.configuracionTipo = configuracionTipo;
    }

    public String getConfiguracionEstado() {
        return configuracionEstado;
    }

    public void setConfiguracionEstado(String configuracionEstado) {
        this.configuracionEstado = configuracionEstado;
    }

    public Date getConfiguracionFechaAlta() {
        return configuracionFechaAlta;
    }

    public void setConfiguracionFechaAlta(Date configuracionFechaAlta) {
        this.configuracionFechaAlta = configuracionFechaAlta;
    }

    public Date getConfiguracionFechaModi() {
        return configuracionFechaModi;
    }

    public void setConfiguracionFechaModi(Date configuracionFechaModi) {
        this.configuracionFechaModi = configuracionFechaModi;
    }

	public String getConfiguracionModo() {
		return configuracionModo;
	}

	public void setConfiguracionModo(String configuracionModo) {
		this.configuracionModo = configuracionModo;
	}

	public String getConfiguracionUrl() {
		return configuracionUrl;
	}

	public void setConfiguracionUrl(String configuracionUrl) {
		this.configuracionUrl = configuracionUrl;
	}

	public String getConfiguracionHeader() {
		return configuracionHeader;
	}

	public void setConfiguracionHeader(String configuracionHeader) {
		this.configuracionHeader = configuracionHeader;
	}

	
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (configuracionId != null ? configuracionId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ConfiguracionEntidadEmiadq)) {
            return false;
        }
        ConfiguracionEntidadEmiadq other = (ConfiguracionEntidadEmiadq) object;
        if ((this.configuracionId == null && other.configuracionId != null) || (this.configuracionId != null && !this.configuracionId.equals(other.configuracionId))) {
            return false;
        }
        return true;
    }

	@Override
	public String toString() {
		return "ConfiguracionEntidadEmiadq [configuracionId=" + configuracionId + ", configuracionCodEntidadDestino="
				+ configuracionCodEntidadDestino + ", configuracionDestino=" + configuracionDestino
				+ ", configuracionTipo=" + configuracionTipo + ", configuracionEstado=" + configuracionEstado
				+ ", configuracionFechaAlta=" + configuracionFechaAlta + ", configuracionFechaModi="
				+ configuracionFechaModi + ", configuracionModo=" + configuracionModo + ", configuracionUrl="
				+ configuracionUrl + ", configuracionHeader=" + configuracionHeader + "]";
	}


    
}
