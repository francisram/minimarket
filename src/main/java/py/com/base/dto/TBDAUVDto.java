package py.com.base.dto;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
public class TBDAUVDto {
    private String id;
    private String rrnboleta;
    private String esreversa;
    private String fechatransaccion;
    private String op_audfcht;
    private String codigocomercio;
    private String nombrecomercio;
    private String codigosucursal;
    private String nombresucursal;
    private String codigoautenticacion;
    private String codigoentidadrol;
    private String numerotarjeta;
    private String numerotarjetaofuscado;
    private String tipotarjeta;
    private String cantidadcuotas;
    private String codigomonedaorigen;
    private String nombremonedaorigen;
    private String codigomonedadestino;
    private String nombremonedadestino;
    private String montoorigen;
    private String montodestino;
    private String ambitotransaccion;
    private String cuentaoperacion;
    private String tipodispositivo;
    private String nombredispositivo;
    private String codigopaisorigen;
    private String nombrepaisorigen;
    private String codigopaisdestino;
    private String nombrepaisdestino;
    private String vencimientotarjeta;
    private String codigoservicio;
    private String nombreservicio;
    private String codigoprestacion;
    private String nombreprestacion;
    private String codigomarcatarjeta;
    private String nombremarcatarjeta;
    private String codigoproductotarjeta;
    private String nombreproductotarjeta;
    private String codigoafinidad;
    private String nombreafinidad;
    private String codigoretorno;
    private String nombreretorno;
    private String mcccomercio;
    private String mccnombre;
    private String codigotransaccion;
    private String nombretransaccion;
    private String fechaproceso;
    private String formapago;
    private String nombreformapago;
    private String estacionorigen;
    private String estaciondestino;
    private String modoentrada;
    private String nombremodoentrada;
    private String saldoactual;
    private String saldodisponible;
    private String estadoinittransaccion;
    private String identificadorcajeroatm;
    private String entidadadministradoraatm;
    private String entidadpropietariaatm;
    private String operadortelefonicoorigen;
    private String nrotelefonoorigen;
    private String operadortelefonicodestino;
    private String nrotelefonodestino;
    private String montocotizacion;
    private String entidadorigentransferencia;
    private String nombreentidadorigentransferencia;
    private String entidaddestinotransferencia;
    private String nombreentidaddestinotransferencia;
    private String cuentaorigentransferencia;
    private String cuentadestinotransferencia;
    private String numeroadherente;
    private String idpromocion;
    private String procesadoremisor;
    private String nombreprocesadoremisor;
    private String procesadoradquiriente;
    private String nombreprocesadoradquiriente;
    private String puntostransaccion;
    private String estadofintransaccion;
    private String estadoexcedido;
    private String mensajeextendido;
    private String tokenrequestorid;
    private String tokenrequestorname;
    private String fechainsercion;
    private String horainsercion;
    private String francis;

    public TBDAUVDto() {
    }

    public String getTokenrequestorname() {
        return tokenrequestorname;
    }

    public void setTokenrequestorname(String tokenrequestorname) {
        this.tokenrequestorname = tokenrequestorname;
    }

    public String getCodigoretorno() {
        return codigoretorno;
    }

    public void setCodigoretorno(String codigoretorno) {
        this.codigoretorno = codigoretorno;
    }

    public String getEstadofintransaccion() {
        return estadofintransaccion;
    }

    public void setEstadofintransaccion(String estadofintransaccion) {
        this.estadofintransaccion = estadofintransaccion;
    }

    public String getNombrepaisdestino() {
        return nombrepaisdestino;
    }

    public void setNombrepaisdestino(String nombrepaisdestino) {
        this.nombrepaisdestino = nombrepaisdestino;
    }

    public String getEntidadadministradoraatm() {
        return entidadadministradoraatm;
    }

    public void setEntidadadministradoraatm(String entidadadministradoraatm) {
        this.entidadadministradoraatm = entidadadministradoraatm;
    }

    public String getNombreretorno() {
        return nombreretorno;
    }

    public void setNombreretorno(String nombreretorno) {
        this.nombreretorno = nombreretorno;
    }

    public String getNombreformapago() {
        return nombreformapago;
    }

    public void setNombreformapago(String nombreformapago) {
        this.nombreformapago = nombreformapago;
    }

    public String getNumerotarjetaofuscado() {
        return numerotarjetaofuscado;
    }

    public void setNumerotarjetaofuscado(String numerotarjetaofuscado) {
        this.numerotarjetaofuscado = numerotarjetaofuscado;
    }

    public String getCodigomonedadestino() {
        return codigomonedadestino;
    }

    public void setCodigomonedadestino(String codigomonedadestino) {
        this.codigomonedadestino = codigomonedadestino;
    }

    public String getCodigoentidadrol() {
        return codigoentidadrol;
    }

    public void setCodigoentidadrol(String codigoentidadrol) {
        this.codigoentidadrol = codigoentidadrol;
    }

    public String getProcesadoremisor() {
        return procesadoremisor;
    }

    public void setProcesadoremisor(String procesadoremisor) {
        this.procesadoremisor = procesadoremisor;
    }

    public String getFechainsercion() {
        return fechainsercion;
    }

    public void setFechainsercion(String fechainsercion) {
        this.fechainsercion = fechainsercion;
    }

    public String getNombreproductotarjeta() {
        return nombreproductotarjeta;
    }

    public void setNombreproductotarjeta(String nombreproductotarjeta) {
        this.nombreproductotarjeta = nombreproductotarjeta;
    }

    public String getCuentaoperacion() {
        return cuentaoperacion;
    }

    public void setCuentaoperacion(String cuentaoperacion) {
        this.cuentaoperacion = cuentaoperacion;
    }

    public String getNombremodoentrada() {
        return nombremodoentrada;
    }

    public void setNombremodoentrada(String nombremodoentrada) {
        this.nombremodoentrada = nombremodoentrada;
    }

    public String getRrnboleta() {
        return rrnboleta;
    }

    public void setRrnboleta(String rrnboleta) {
        this.rrnboleta = rrnboleta;
    }

    public String getCodigocomercio() {
        return codigocomercio;
    }

    public void setCodigocomercio(String codigocomercio) {
        this.codigocomercio = codigocomercio;
    }

    public String getEstadoinittransaccion() {
        return estadoinittransaccion;
    }

    public void setEstadoinittransaccion(String estadoinittransaccion) {
        this.estadoinittransaccion = estadoinittransaccion;
    }

    public String getEstaciondestino() {
        return estaciondestino;
    }

    public void setEstaciondestino(String estaciondestino) {
        this.estaciondestino = estaciondestino;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdentificadorcajeroatm() {
        return identificadorcajeroatm;
    }

    public void setIdentificadorcajeroatm(String identificadorcajeroatm) {
        this.identificadorcajeroatm = identificadorcajeroatm;
    }

    public String getMensajeextendido() {
        return mensajeextendido;
    }

    public void setMensajeextendido(String mensajeextendido) {
        this.mensajeextendido = mensajeextendido;
    }

    public String getMontocotizacion() {
        return montocotizacion;
    }

    public void setMontocotizacion(String montocotizacion) {
        this.montocotizacion = montocotizacion;
    }

    public String getNombreprocesadoremisor() {
        return nombreprocesadoremisor;
    }

    public void setNombreprocesadoremisor(String nombreprocesadoremisor) {
        this.nombreprocesadoremisor = nombreprocesadoremisor;
    }

    public String getProcesadoradquiriente() {
        return procesadoradquiriente;
    }

    public void setProcesadoradquiriente(String procesadoradquiriente) {
        this.procesadoradquiriente = procesadoradquiriente;
    }

    public String getCodigotransaccion() {
        return codigotransaccion;
    }

    public void setCodigotransaccion(String codigotransaccion) {
        this.codigotransaccion = codigotransaccion;
    }

    public String getNombreservicio() {
        return nombreservicio;
    }

    public void setNombreservicio(String nombreservicio) {
        this.nombreservicio = nombreservicio;
    }

    public String getNombreafinidad() {
        return nombreafinidad;
    }

    public void setNombreafinidad(String nombreafinidad) {
        this.nombreafinidad = nombreafinidad;
    }

    public String getMontoorigen() {
        return montoorigen;
    }

    public void setMontoorigen(String montoorigen) {
        this.montoorigen = montoorigen;
    }

    public String getCuentadestinotransferencia() {
        return cuentadestinotransferencia;
    }

    public void setCuentadestinotransferencia(String cuentadestinotransferencia) {
        this.cuentadestinotransferencia = cuentadestinotransferencia;
    }

    public String getModoentrada() {
        return modoentrada;
    }

    public void setModoentrada(String modoentrada) {
        this.modoentrada = modoentrada;
    }

    public String getNumerotarjeta() {
        return numerotarjeta;
    }

    public void setNumerotarjeta(String numerotarjeta) {
        this.numerotarjeta = numerotarjeta;
    }

    public String getOp_audfcht() {
        return op_audfcht;
    }

    public void setOp_audfcht(String op_audfcht) {
        this.op_audfcht = op_audfcht;
    }

    public String getNrotelefonodestino() {
        return nrotelefonodestino;
    }

    public void setNrotelefonodestino(String nrotelefonodestino) {
        this.nrotelefonodestino = nrotelefonodestino;
    }

    public String getNombrepaisorigen() {
        return nombrepaisorigen;
    }

    public void setNombrepaisorigen(String nombrepaisorigen) {
        this.nombrepaisorigen = nombrepaisorigen;
    }

    public String getEntidadorigentransferencia() {
        return entidadorigentransferencia;
    }

    public void setEntidadorigentransferencia(String entidadorigentransferencia) {
        this.entidadorigentransferencia = entidadorigentransferencia;
    }

    public String getTokenrequestorid() {
        return tokenrequestorid;
    }

    public void setTokenrequestorid(String tokenrequestorid) {
        this.tokenrequestorid = tokenrequestorid;
    }

    public String getNombretransaccion() {
        return nombretransaccion;
    }

    public void setNombretransaccion(String nombretransaccion) {
        this.nombretransaccion = nombretransaccion;
    }

    public String getNombresucursal() {
        return nombresucursal;
    }

    public void setNombresucursal(String nombresucursal) {
        this.nombresucursal = nombresucursal;
    }

    public String getOperadortelefonicodestino() {
        return operadortelefonicodestino;
    }

    public void setOperadortelefonicodestino(String operadortelefonicodestino) {
        this.operadortelefonicodestino = operadortelefonicodestino;
    }

    public String getNumeroadherente() {
        return numeroadherente;
    }

    public void setNumeroadherente(String numeroadherente) {
        this.numeroadherente = numeroadherente;
    }

    public String getCodigoautenticacion() {
        return codigoautenticacion;
    }

    public void setCodigoautenticacion(String codigoautenticacion) {
        this.codigoautenticacion = codigoautenticacion;
    }

    public String getMcccomercio() {
        return mcccomercio;
    }

    public void setMcccomercio(String mcccomercio) {
        this.mcccomercio = mcccomercio;
    }

    public String getEsreversa() {
        return esreversa;
    }

    public void setEsreversa(String esreversa) {
        this.esreversa = esreversa;
    }

    public String getSaldodisponible() {
        return saldodisponible;
    }

    public void setSaldodisponible(String saldodisponible) {
        this.saldodisponible = saldodisponible;
    }

    public String getNombrecomercio() {
        return nombrecomercio;
    }

    public void setNombrecomercio(String nombrecomercio) {
        this.nombrecomercio = nombrecomercio;
    }

    public String getIdpromocion() {
        return idpromocion;
    }

    public void setIdpromocion(String idpromocion) {
        this.idpromocion = idpromocion;
    }

    public String getAmbitotransaccion() {
        return ambitotransaccion;
    }

    public void setAmbitotransaccion(String ambitotransaccion) {
        this.ambitotransaccion = ambitotransaccion;
    }

    public String getNombremonedadestino() {
        return nombremonedadestino;
    }

    public void setNombremonedadestino(String nombremonedadestino) {
        this.nombremonedadestino = nombremonedadestino;
    }

    public String getFormapago() {
        return formapago;
    }

    public void setFormapago(String formapago) {
        this.formapago = formapago;
    }

    public String getVencimientotarjeta() {
        return vencimientotarjeta;
    }

    public void setVencimientotarjeta(String vencimientotarjeta) {
        this.vencimientotarjeta = vencimientotarjeta;
    }

    public String getSaldoactual() {
        return saldoactual;
    }

    public void setSaldoactual(String saldoactual) {
        this.saldoactual = saldoactual;
    }

    public String getNombreentidaddestinotransferencia() {
        return nombreentidaddestinotransferencia;
    }

    public void setNombreentidaddestinotransferencia(String nombreentidaddestinotransferencia) {
        this.nombreentidaddestinotransferencia = nombreentidaddestinotransferencia;
    }

    public String getCuentaorigentransferencia() {
        return cuentaorigentransferencia;
    }

    public void setCuentaorigentransferencia(String cuentaorigentransferencia) {
        this.cuentaorigentransferencia = cuentaorigentransferencia;
    }

    public String getHorainsercion() {
        return horainsercion;
    }

    public void setHorainsercion(String horainsercion) {
        this.horainsercion = horainsercion;
    }

    public String getNrotelefonoorigen() {
        return nrotelefonoorigen;
    }

    public void setNrotelefonoorigen(String nrotelefonoorigen) {
        this.nrotelefonoorigen = nrotelefonoorigen;
    }

    public String getMccnombre() {
        return mccnombre;
    }

    public void setMccnombre(String mccnombre) {
        this.mccnombre = mccnombre;
    }

    public String getCodigoproductotarjeta() {
        return codigoproductotarjeta;
    }

    public void setCodigoproductotarjeta(String codigoproductotarjeta) {
        this.codigoproductotarjeta = codigoproductotarjeta;
    }

    public String getOperadortelefonicoorigen() {
        return operadortelefonicoorigen;
    }

    public void setOperadortelefonicoorigen(String operadortelefonicoorigen) {
        this.operadortelefonicoorigen = operadortelefonicoorigen;
    }

    public String getCodigopaisorigen() {
        return codigopaisorigen;
    }

    public void setCodigopaisorigen(String codigopaisorigen) {
        this.codigopaisorigen = codigopaisorigen;
    }

    public String getCodigoafinidad() {
        return codigoafinidad;
    }

    public void setCodigoafinidad(String codigoafinidad) {
        this.codigoafinidad = codigoafinidad;
    }

    public String getCodigomarcatarjeta() {
        return codigomarcatarjeta;
    }

    public void setCodigomarcatarjeta(String codigomarcatarjeta) {
        this.codigomarcatarjeta = codigomarcatarjeta;
    }

    public String getCodigoservicio() {
        return codigoservicio;
    }

    public void setCodigoservicio(String codigoservicio) {
        this.codigoservicio = codigoservicio;
    }

    public String getFechaproceso() {
        return fechaproceso;
    }

    public void setFechaproceso(String fechaproceso) {
        this.fechaproceso = fechaproceso;
    }

    public String getNombreprocesadoradquiriente() {
        return nombreprocesadoradquiriente;
    }

    public void setNombreprocesadoradquiriente(String nombreprocesadoradquiriente) {
        this.nombreprocesadoradquiriente = nombreprocesadoradquiriente;
    }

    public String getEstadoexcedido() {
        return estadoexcedido;
    }

    public void setEstadoexcedido(String estadoexcedido) {
        this.estadoexcedido = estadoexcedido;
    }

    public String getCodigomonedaorigen() {
        return codigomonedaorigen;
    }

    public void setCodigomonedaorigen(String codigomonedaorigen) {
        this.codigomonedaorigen = codigomonedaorigen;
    }

    public String getNombremonedaorigen() {
        return nombremonedaorigen;
    }

    public void setNombremonedaorigen(String nombremonedaorigen) {
        this.nombremonedaorigen = nombremonedaorigen;
    }

    public String getCodigopaisdestino() {
        return codigopaisdestino;
    }

    public void setCodigopaisdestino(String codigopaisdestino) {
        this.codigopaisdestino = codigopaisdestino;
    }

    public String getTipodispositivo() {
        return tipodispositivo;
    }

    public void setTipodispositivo(String tipodispositivo) {
        this.tipodispositivo = tipodispositivo;
    }

    public String getCodigoprestacion() {
        return codigoprestacion;
    }

    public void setCodigoprestacion(String codigoprestacion) {
        this.codigoprestacion = codigoprestacion;
    }

    public String getNombreentidadorigentransferencia() {
        return nombreentidadorigentransferencia;
    }

    public void setNombreentidadorigentransferencia(String nombreentidadorigentransferencia) {
        this.nombreentidadorigentransferencia = nombreentidadorigentransferencia;
    }

    public String getFechatransaccion() {
        return fechatransaccion;
    }

    public void setFechatransaccion(String fechatransaccion) {
        this.fechatransaccion = fechatransaccion;
    }

    public String getNombremarcatarjeta() {
        return nombremarcatarjeta;
    }

    public void setNombremarcatarjeta(String nombremarcatarjeta) {
        this.nombremarcatarjeta = nombremarcatarjeta;
    }

    public String getEntidaddestinotransferencia() {
        return entidaddestinotransferencia;
    }

    public void setEntidaddestinotransferencia(String entidaddestinotransferencia) {
        this.entidaddestinotransferencia = entidaddestinotransferencia;
    }

    public String getEstacionorigen() {
        return estacionorigen;
    }

    public void setEstacionorigen(String estacionorigen) {
        this.estacionorigen = estacionorigen;
    }

    public String getCodigosucursal() {
        return codigosucursal;
    }

    public void setCodigosucursal(String codigosucursal) {
        this.codigosucursal = codigosucursal;
    }

    public String getNombredispositivo() {
        return nombredispositivo;
    }

    public void setNombredispositivo(String nombredispositivo) {
        this.nombredispositivo = nombredispositivo;
    }

    public String getMontodestino() {
        return montodestino;
    }

    public void setMontodestino(String montodestino) {
        this.montodestino = montodestino;
    }

    public String getNombreprestacion() {
        return nombreprestacion;
    }

    public void setNombreprestacion(String nombreprestacion) {
        this.nombreprestacion = nombreprestacion;
    }

    public String getPuntostransaccion() {
        return puntostransaccion;
    }

    public void setPuntostransaccion(String puntostransaccion) {
        this.puntostransaccion = puntostransaccion;
    }

    public String getCantidadcuotas() {
        return cantidadcuotas;
    }

    public void setCantidadcuotas(String cantidadcuotas) {
        this.cantidadcuotas = cantidadcuotas;
    }

    public String getFrancis() {
        return francis;
    }

    public void setFrancis(String francis) {
        this.francis = francis;
    }

    public String getEntidadpropietariaatm() {
        return entidadpropietariaatm;
    }

    public void setEntidadpropietariaatm(String entidadpropietariaatm) {
        this.entidadpropietariaatm = entidadpropietariaatm;
    }

    public String getTipotarjeta() {
        return tipotarjeta;
    }

    public void setTipotarjeta(String tipotarjeta) {
        this.tipotarjeta = tipotarjeta;
    }

    @Override
    public String toString() {
        return "TBDAUVDto [" +
            "tokenrequestorname='" + tokenrequestorname + "'" +
            "codigoretorno='" + codigoretorno + "'" +
            "estadofintransaccion='" + estadofintransaccion + "'" +
            "nombrepaisdestino='" + nombrepaisdestino + "'" +
            "entidadadministradoraatm='" + entidadadministradoraatm + "'" +
            "nombreretorno='" + nombreretorno + "'" +
            "nombreformapago='" + nombreformapago + "'" +
            "numerotarjetaofuscado='" + numerotarjetaofuscado + "'" +
            "codigomonedadestino='" + codigomonedadestino + "'" +
            "codigoentidadrol='" + codigoentidadrol + "'" +
            "procesadoremisor='" + procesadoremisor + "'" +
            "fechainsercion='" + fechainsercion + "'" +
            "nombreproductotarjeta='" + nombreproductotarjeta + "'" +
            "cuentaoperacion='" + cuentaoperacion + "'" +
            "nombremodoentrada='" + nombremodoentrada + "'" +
            "rrnboleta='" + rrnboleta + "'" +
            "codigocomercio='" + codigocomercio + "'" +
            "estadoinittransaccion='" + estadoinittransaccion + "'" +
            "estaciondestino='" + estaciondestino + "'" +
            "id='" + id + "'" +
            "identificadorcajeroatm='" + identificadorcajeroatm + "'" +
            "mensajeextendido='" + mensajeextendido + "'" +
            "montocotizacion='" + montocotizacion + "'" +
            "nombreprocesadoremisor='" + nombreprocesadoremisor + "'" +
            "procesadoradquiriente='" + procesadoradquiriente + "'" +
            "codigotransaccion='" + codigotransaccion + "'" +
            "nombreservicio='" + nombreservicio + "'" +
            "nombreafinidad='" + nombreafinidad + "'" +
            "montoorigen='" + montoorigen + "'" +
            "cuentadestinotransferencia='" + cuentadestinotransferencia + "'" +
            "modoentrada='" + modoentrada + "'" +
            "numerotarjeta='" + numerotarjeta + "'" +
            "op_audfcht='" + op_audfcht + "'" +
            "nrotelefonodestino='" + nrotelefonodestino + "'" +
            "nombrepaisorigen='" + nombrepaisorigen + "'" +
            "entidadorigentransferencia='" + entidadorigentransferencia + "'" +
            "tokenrequestorid='" + tokenrequestorid + "'" +
            "nombretransaccion='" + nombretransaccion + "'" +
            "nombresucursal='" + nombresucursal + "'" +
            "operadortelefonicodestino='" + operadortelefonicodestino + "'" +
            "numeroadherente='" + numeroadherente + "'" +
            "codigoautenticacion='" + codigoautenticacion + "'" +
            "mcccomercio='" + mcccomercio + "'" +
            "esreversa='" + esreversa + "'" +
            "saldodisponible='" + saldodisponible + "'" +
            "nombrecomercio='" + nombrecomercio + "'" +
            "idpromocion='" + idpromocion + "'" +
            "ambitotransaccion='" + ambitotransaccion + "'" +
            "nombremonedadestino='" + nombremonedadestino + "'" +
            "formapago='" + formapago + "'" +
            "vencimientotarjeta='" + vencimientotarjeta + "'" +
            "saldoactual='" + saldoactual + "'" +
            "nombreentidaddestinotransferencia='" + nombreentidaddestinotransferencia + "'" +
            "cuentaorigentransferencia='" + cuentaorigentransferencia + "'" +
            "horainsercion='" + horainsercion + "'" +
            "nrotelefonoorigen='" + nrotelefonoorigen + "'" +
            "mccnombre='" + mccnombre + "'" +
            "codigoproductotarjeta='" + codigoproductotarjeta + "'" +
            "operadortelefonicoorigen='" + operadortelefonicoorigen + "'" +
            "codigopaisorigen='" + codigopaisorigen + "'" +
            "codigoafinidad='" + codigoafinidad + "'" +
            "codigomarcatarjeta='" + codigomarcatarjeta + "'" +
            "codigoservicio='" + codigoservicio + "'" +
            "fechaproceso='" + fechaproceso + "'" +
            "nombreprocesadoradquiriente='" + nombreprocesadoradquiriente + "'" +
            "estadoexcedido='" + estadoexcedido + "'" +
            "codigomonedaorigen='" + codigomonedaorigen + "'" +
            "nombremonedaorigen='" + nombremonedaorigen + "'" +
            "codigopaisdestino='" + codigopaisdestino + "'" +
            "tipodispositivo='" + tipodispositivo + "'" +
            "codigoprestacion='" + codigoprestacion + "'" +
            "nombreentidadorigentransferencia='" + nombreentidadorigentransferencia + "'" +
            "fechatransaccion='" + fechatransaccion + "'" +
            "nombremarcatarjeta='" + nombremarcatarjeta + "'" +
            "entidaddestinotransferencia='" + entidaddestinotransferencia + "'" +
            "estacionorigen='" + estacionorigen + "'" +
            "codigosucursal='" + codigosucursal + "'" +
            "nombredispositivo='" + nombredispositivo + "'" +
            "montodestino='" + montodestino + "'" +
            "nombreprestacion='" + nombreprestacion + "'" +
            "puntostransaccion='" + puntostransaccion + "'" +
            "cantidadcuotas='" + cantidadcuotas + "'" +
            "francis='" + francis + "'" +
            "entidadpropietariaatm='" + entidadpropietariaatm + "'" +
            "tipotarjeta='" + tipotarjeta + "'" +
            ']';
    }
}
