package py.com.base.dto;

import java.math.BigDecimal;

public class Transaccion {

	public Long id;
	public String fechaInsercion;
	public String horaInsercion;
	public String rrnBoleta;
	public String esReversa;
	public String fechaTransaccion;
	public String codigoComercio;
	public String nombreComercio;
	public String codigoSucursal;
	public String nombreSucursal;
	public String codigoAutenticacion;
	public String codigoEntidadRol;
	public String numeroTarjeta;
	public String numeroTarjetaOfuscado;
	public String tipoTarjeta;
	public String cantidadCuotas;
	public String codigoMonedaOrigen;
	public String nombreMonedaOrigen;
	public String codigoMonedaDestino;
	public String nombreMonedaDestino;
	public BigDecimal montoOrigen;
	public BigDecimal montoDestino;
	public String ambitoTransaccion;
	public String cuentaOperacion;
	public String tipoDispositivo;
	public String nombreDispositivo;
	public String codigoPaisOrigen;
	public String nombrePaisOrigen;
	public String codigoPaisDestino;
	public String nombrePaisDestino;
	public String vencimientoTarjeta;
	public String codigoServicio;
	public String nombreServicio;
	public String codigoPrestacion;
	public String nombrePrestacion;
	public String codigoMarcaTarjeta;
	public String nombreMarcaTarjeta;
	public String codigoProductoTarjeta;
	public String nombreProductoTarjeta;
	public String codigoAfinidad;
	public String nombreAfinidad;
	public String codigoRetorno;
	public String nombreRetorno;
	public String mccComercio;
	public String mccNombre;
	public String codigoTransaccion;
	public String nombreTransaccion;
	public String fechaProceso;
	public String formaPago;
	public String nombreFormaPago;
	public String estacionOrigen;
	public String estacionDestino;
	public String modoEntrada;
	public String nombreModoEntrada;
	public BigDecimal saldoActual;
	public BigDecimal saldoDisponible;
	public String estadoInitTransaccion;
	public String identificadorCajeroAtm;
	public String entidadAdministradoraAtm;
	public String entidadPropietariaAtm;
	public String operadorTelefonicoOrigen;
	public String nroTelefonoOrigen;
	public String operadorTelefonicoDestino;
	public String nroTelefonoDestino;
	public BigDecimal montoCotizacion;
	public String entidadOrigenTransferencia;
	public String nombreEntidadOrigenTransferencia;
	public String entidadDestinoTransferencia;
	public String nombreEntidadDestinoTransferencia;
	public String cuentaOrigenTransferencia;
	public String cuentaDestinoTransferencia;
	public String numeroAdherente;
	public String idPromocion;
	public String procesadorEmisor;
	public String nombreProcesadorEmisor;
	public String procesadorAdquiriente;
	public String nombreProcesadorAdquiriente;
	public String puntosTransaccion;
	public String estadoFinTransaccion;
	public String estadoExcedido;
	public String mensajeExtendido;
	public String tokenRequestorId;
	public String tokenRequestorName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFechaInsercion() {
		return fechaInsercion;
	}

	public void setFechaInsercion(String fechaInsercion) {
		this.fechaInsercion = fechaInsercion;
	}

	public String getHoraInsercion() {
		return horaInsercion;
	}

	public void setHoraInsercion(String horaInsercion) {
		this.horaInsercion = horaInsercion;
	}

	public String getRrnBoleta() {
		return rrnBoleta;
	}

	public void setRrnBoleta(String rrnBoleta) {
		this.rrnBoleta = rrnBoleta;
	}

	public String getEsReversa() {
		return esReversa;
	}

	public void setEsReversa(String esReversa) {
		this.esReversa = esReversa;
	}

	public String getFechaTransaccion() {
		return fechaTransaccion;
	}

	public void setFechaTransaccion(String fechaTransaccion) {
		this.fechaTransaccion = fechaTransaccion;
	}

	public String getCodigoComercio() {
		return codigoComercio;
	}

	public void setCodigoComercio(String codigoComercio) {
		this.codigoComercio = codigoComercio;
	}

	public String getNombreComercio() {
		return nombreComercio;
	}

	public void setNombreComercio(String nombreComercio) {
		this.nombreComercio = nombreComercio;
	}

	public String getCodigoSucursal() {
		return codigoSucursal;
	}

	public void setCodigoSucursal(String codigoSucursal) {
		this.codigoSucursal = codigoSucursal;
	}

	public String getNombreSucursal() {
		return nombreSucursal;
	}

	public void setNombreSucursal(String nombreSucursal) {
		this.nombreSucursal = nombreSucursal;
	}

	public String getCodigoAutenticacion() {
		return codigoAutenticacion;
	}

	public void setCodigoAutenticacion(String codigoAutenticacion) {
		this.codigoAutenticacion = codigoAutenticacion;
	}

	public String getCodigoEntidadRol() {
		return codigoEntidadRol;
	}

	public void setCodigoEntidadRol(String codigoEntidadRol) {
		this.codigoEntidadRol = codigoEntidadRol;
	}

	public String getNumeroTarjeta() {
		return numeroTarjeta;
	}

	public void setNumeroTarjeta(String numeroTarjeta) {
		this.numeroTarjeta = numeroTarjeta;
	}

	public String getNumeroTarjetaOfuscado() {
		return numeroTarjetaOfuscado;
	}

	public void setNumeroTarjetaOfuscado(String numeroTarjetaOfuscado) {
		this.numeroTarjetaOfuscado = numeroTarjetaOfuscado;
	}

	public String getTipoTarjeta() {
		return tipoTarjeta;
	}

	public void setTipoTarjeta(String tipoTarjeta) {
		this.tipoTarjeta = tipoTarjeta;
	}

	public String getCantidadCuotas() {
		return cantidadCuotas;
	}

	public void setCantidadCuotas(String cantidadCuotas) {
		this.cantidadCuotas = cantidadCuotas;
	}

	public String getCodigoMonedaOrigen() {
		return codigoMonedaOrigen;
	}

	public void setCodigoMonedaOrigen(String codigoMonedaOrigen) {
		this.codigoMonedaOrigen = codigoMonedaOrigen;
	}

	public String getNombreMonedaOrigen() {
		return nombreMonedaOrigen;
	}

	public void setNombreMonedaOrigen(String nombreMonedaOrigen) {
		this.nombreMonedaOrigen = nombreMonedaOrigen;
	}

	public String getCodigoMonedaDestino() {
		return codigoMonedaDestino;
	}

	public void setCodigoMonedaDestino(String codigoMonedaDestino) {
		this.codigoMonedaDestino = codigoMonedaDestino;
	}

	public String getNombreMonedaDestino() {
		return nombreMonedaDestino;
	}

	public void setNombreMonedaDestino(String nombreMonedaDestino) {
		this.nombreMonedaDestino = nombreMonedaDestino;
	}

	public BigDecimal getMontoOrigen() {
		return montoOrigen;
	}

	public void setMontoOrigen(BigDecimal montoOrigen) {
		this.montoOrigen = montoOrigen;
	}

	public BigDecimal getMontoDestino() {
		return montoDestino;
	}

	public void setMontoDestino(BigDecimal montoDestino) {
		this.montoDestino = montoDestino;
	}

	public String getAmbitoTransaccion() {
		return ambitoTransaccion;
	}

	public void setAmbitoTransaccion(String ambitoTransaccion) {
		this.ambitoTransaccion = ambitoTransaccion;
	}

	public String getCuentaOperacion() {
		return cuentaOperacion;
	}

	public void setCuentaOperacion(String cuentaOperacion) {
		this.cuentaOperacion = cuentaOperacion;
	}

	public String getTipoDispositivo() {
		return tipoDispositivo;
	}

	public void setTipoDispositivo(String tipoDispositivo) {
		this.tipoDispositivo = tipoDispositivo;
	}

	public String getNombreDispositivo() {
		return nombreDispositivo;
	}

	public void setNombreDispositivo(String nombreDispositivo) {
		this.nombreDispositivo = nombreDispositivo;
	}

	public String getCodigoPaisOrigen() {
		return codigoPaisOrigen;
	}

	public void setCodigoPaisOrigen(String codigoPaisOrigen) {
		this.codigoPaisOrigen = codigoPaisOrigen;
	}

	public String getNombrePaisOrigen() {
		return nombrePaisOrigen;
	}

	public void setNombrePaisOrigen(String nombrePaisOrigen) {
		this.nombrePaisOrigen = nombrePaisOrigen;
	}

	public String getCodigoPaisDestino() {
		return codigoPaisDestino;
	}

	public void setCodigoPaisDestino(String codigoPaisDestino) {
		this.codigoPaisDestino = codigoPaisDestino;
	}

	public String getNombrePaisDestino() {
		return nombrePaisDestino;
	}

	public void setNombrePaisDestino(String nombrePaisDestino) {
		this.nombrePaisDestino = nombrePaisDestino;
	}

	public String getVencimientoTarjeta() {
		return vencimientoTarjeta;
	}

	public void setVencimientoTarjeta(String vencimientoTarjeta) {
		this.vencimientoTarjeta = vencimientoTarjeta;
	}

	public String getCodigoServicio() {
		return codigoServicio;
	}

	public void setCodigoServicio(String codigoServicio) {
		this.codigoServicio = codigoServicio;
	}

	public String getNombreServicio() {
		return nombreServicio;
	}

	public void setNombreServicio(String nombreServicio) {
		this.nombreServicio = nombreServicio;
	}

	public String getCodigoPrestacion() {
		return codigoPrestacion;
	}

	public void setCodigoPrestacion(String codigoPrestacion) {
		this.codigoPrestacion = codigoPrestacion;
	}

	public String getNombrePrestacion() {
		return nombrePrestacion;
	}

	public void setNombrePrestacion(String nombrePrestacion) {
		this.nombrePrestacion = nombrePrestacion;
	}

	public String getCodigoMarcaTarjeta() {
		return codigoMarcaTarjeta;
	}

	public void setCodigoMarcaTarjeta(String codigoMarcaTarjeta) {
		this.codigoMarcaTarjeta = codigoMarcaTarjeta;
	}

	public String getNombreMarcaTarjeta() {
		return nombreMarcaTarjeta;
	}

	public void setNombreMarcaTarjeta(String nombreMarcaTarjeta) {
		this.nombreMarcaTarjeta = nombreMarcaTarjeta;
	}

	public String getCodigoProductoTarjeta() {
		return codigoProductoTarjeta;
	}

	public void setCodigoProductoTarjeta(String codigoProductoTarjeta) {
		this.codigoProductoTarjeta = codigoProductoTarjeta;
	}

	public String getNombreProductoTarjeta() {
		return nombreProductoTarjeta;
	}

	public void setNombreProductoTarjeta(String nombreProductoTarjeta) {
		this.nombreProductoTarjeta = nombreProductoTarjeta;
	}

	public String getCodigoAfinidad() {
		return codigoAfinidad;
	}

	public void setCodigoAfinidad(String codigoAfinidad) {
		this.codigoAfinidad = codigoAfinidad;
	}

	public String getNombreAfinidad() {
		return nombreAfinidad;
	}

	public void setNombreAfinidad(String nombreAfinidad) {
		this.nombreAfinidad = nombreAfinidad;
	}

	public String getCodigoRetorno() {
		return codigoRetorno;
	}

	public void setCodigoRetorno(String codigoRetorno) {
		this.codigoRetorno = codigoRetorno;
	}

	public String getNombreRetorno() {
		return nombreRetorno;
	}

	public void setNombreRetorno(String nombreRetorno) {
		this.nombreRetorno = nombreRetorno;
	}

	public String getMccComercio() {
		return mccComercio;
	}

	public void setMccComercio(String mccComercio) {
		this.mccComercio = mccComercio;
	}

	public String getMccNombre() {
		return mccNombre;
	}

	public void setMccNombre(String mccNombre) {
		this.mccNombre = mccNombre;
	}

	public String getCodigoTransaccion() {
		return codigoTransaccion;
	}

	public void setCodigoTransaccion(String codigoTransaccion) {
		this.codigoTransaccion = codigoTransaccion;
	}

	public String getNombreTransaccion() {
		return nombreTransaccion;
	}

	public void setNombreTransaccion(String nombreTransaccion) {
		this.nombreTransaccion = nombreTransaccion;
	}

	public String getFechaProceso() {
		return fechaProceso;
	}

	public void setFechaProceso(String fechaProceso) {
		this.fechaProceso = fechaProceso;
	}

	public String getFormaPago() {
		return formaPago;
	}

	public void setFormaPago(String formaPago) {
		this.formaPago = formaPago;
	}

	public String getNombreFormaPago() {
		return nombreFormaPago;
	}

	public void setNombreFormaPago(String nombreFormaPago) {
		this.nombreFormaPago = nombreFormaPago;
	}

	public String getEstacionOrigen() {
		return estacionOrigen;
	}

	public void setEstacionOrigen(String estacionOrigen) {
		this.estacionOrigen = estacionOrigen;
	}

	public String getEstacionDestino() {
		return estacionDestino;
	}

	public void setEstacionDestino(String estacionDestino) {
		this.estacionDestino = estacionDestino;
	}

	public String getModoEntrada() {
		return modoEntrada;
	}

	public void setModoEntrada(String modoEntrada) {
		this.modoEntrada = modoEntrada;
	}

	public String getNombreModoEntrada() {
		return nombreModoEntrada;
	}

	public void setNombreModoEntrada(String nombreModoEntrada) {
		this.nombreModoEntrada = nombreModoEntrada;
	}

	public BigDecimal getSaldoActual() {
		return saldoActual;
	}

	public void setSaldoActual(BigDecimal saldoActual) {
		this.saldoActual = saldoActual;
	}

	public BigDecimal getSaldoDisponible() {
		return saldoDisponible;
	}

	public void setSaldoDisponible(BigDecimal saldoDisponible) {
		this.saldoDisponible = saldoDisponible;
	}

	public String getEstadoInitTransaccion() {
		return estadoInitTransaccion;
	}

	public void setEstadoInitTransaccion(String estadoInitTransaccion) {
		this.estadoInitTransaccion = estadoInitTransaccion;
	}

	public String getIdentificadorCajeroAtm() {
		return identificadorCajeroAtm;
	}

	public void setIdentificadorCajeroAtm(String identificadorCajeroAtm) {
		this.identificadorCajeroAtm = identificadorCajeroAtm;
	}

	public String getEntidadAdministradoraAtm() {
		return entidadAdministradoraAtm;
	}

	public void setEntidadAdministradoraAtm(String entidadAdministradoraAtm) {
		this.entidadAdministradoraAtm = entidadAdministradoraAtm;
	}

	public String getEntidadPropietariaAtm() {
		return entidadPropietariaAtm;
	}

	public void setEntidadPropietariaAtm(String entidadPropietariaAtm) {
		this.entidadPropietariaAtm = entidadPropietariaAtm;
	}

	public String getOperadorTelefonicoOrigen() {
		return operadorTelefonicoOrigen;
	}

	public void setOperadorTelefonicoOrigen(String operadorTelefonicoOrigen) {
		this.operadorTelefonicoOrigen = operadorTelefonicoOrigen;
	}

	public String getNroTelefonoOrigen() {
		return nroTelefonoOrigen;
	}

	public void setNroTelefonoOrigen(String nroTelefonoOrigen) {
		this.nroTelefonoOrigen = nroTelefonoOrigen;
	}

	public String getOperadorTelefonicoDestino() {
		return operadorTelefonicoDestino;
	}

	public void setOperadorTelefonicoDestino(String operadorTelefonicoDestino) {
		this.operadorTelefonicoDestino = operadorTelefonicoDestino;
	}

	public String getNroTelefonoDestino() {
		return nroTelefonoDestino;
	}

	public void setNroTelefonoDestino(String nroTelefonoDestino) {
		this.nroTelefonoDestino = nroTelefonoDestino;
	}

	public BigDecimal getMontoCotizacion() {
		return montoCotizacion;
	}

	public void setMontoCotizacion(BigDecimal montoCotizacion) {
		this.montoCotizacion = montoCotizacion;
	}

	public String getEntidadOrigenTransferencia() {
		return entidadOrigenTransferencia;
	}

	public void setEntidadOrigenTransferencia(String entidadOrigenTransferencia) {
		this.entidadOrigenTransferencia = entidadOrigenTransferencia;
	}

	public String getNombreEntidadOrigenTransferencia() {
		return nombreEntidadOrigenTransferencia;
	}

	public void setNombreEntidadOrigenTransferencia(String nombreEntidadOrigenTransferencia) {
		this.nombreEntidadOrigenTransferencia = nombreEntidadOrigenTransferencia;
	}

	public String getEntidadDestinoTransferencia() {
		return entidadDestinoTransferencia;
	}

	public void setEntidadDestinoTransferencia(String entidadDestinoTransferencia) {
		this.entidadDestinoTransferencia = entidadDestinoTransferencia;
	}

	public String getNombreEntidadDestinoTransferencia() {
		return nombreEntidadDestinoTransferencia;
	}

	public void setNombreEntidadDestinoTransferencia(String nombreEntidadDestinoTransferencia) {
		this.nombreEntidadDestinoTransferencia = nombreEntidadDestinoTransferencia;
	}

	public String getCuentaOrigenTransferencia() {
		return cuentaOrigenTransferencia;
	}

	public void setCuentaOrigenTransferencia(String cuentaOrigenTransferencia) {
		this.cuentaOrigenTransferencia = cuentaOrigenTransferencia;
	}

	public String getCuentaDestinoTransferencia() {
		return cuentaDestinoTransferencia;
	}

	public void setCuentaDestinoTransferencia(String cuentaDestinoTransferencia) {
		this.cuentaDestinoTransferencia = cuentaDestinoTransferencia;
	}

	public String getNumeroAdherente() {
		return numeroAdherente;
	}

	public void setNumeroAdherente(String numeroAdherente) {
		this.numeroAdherente = numeroAdherente;
	}

	public String getIdPromocion() {
		return idPromocion;
	}

	public void setIdPromocion(String idPromocion) {
		this.idPromocion = idPromocion;
	}

	public String getProcesadorEmisor() {
		return procesadorEmisor;
	}

	public void setProcesadorEmisor(String procesadorEmisor) {
		this.procesadorEmisor = procesadorEmisor;
	}

	public String getNombreProcesadorEmisor() {
		return nombreProcesadorEmisor;
	}

	public void setNombreProcesadorEmisor(String nombreProcesadorEmisor) {
		this.nombreProcesadorEmisor = nombreProcesadorEmisor;
	}

	public String getProcesadorAdquiriente() {
		return procesadorAdquiriente;
	}

	public void setProcesadorAdquiriente(String procesadorAdquiriente) {
		this.procesadorAdquiriente = procesadorAdquiriente;
	}

	public String getNombreProcesadorAdquiriente() {
		return nombreProcesadorAdquiriente;
	}

	public void setNombreProcesadorAdquiriente(String nombreProcesadorAdquiriente) {
		this.nombreProcesadorAdquiriente = nombreProcesadorAdquiriente;
	}

	public String getPuntosTransaccion() {
		return puntosTransaccion;
	}

	public void setPuntosTransaccion(String puntosTransaccion) {
		this.puntosTransaccion = puntosTransaccion;
	}

	public String getEstadoFinTransaccion() {
		return estadoFinTransaccion;
	}

	public void setEstadoFinTransaccion(String estadoFinTransaccion) {
		this.estadoFinTransaccion = estadoFinTransaccion;
	}

	public String getEstadoExcedido() {
		return estadoExcedido;
	}

	public void setEstadoExcedido(String estadoExcedido) {
		this.estadoExcedido = estadoExcedido;
	}

	public String getMensajeExtendido() {
		return mensajeExtendido;
	}

	public void setMensajeExtendido(String mensajeExtendido) {
		this.mensajeExtendido = mensajeExtendido;
	}

	public String getTokenRequestorId() {
		return tokenRequestorId;
	}

	public void setTokenRequestorId(String tokenRequestorId) {
		this.tokenRequestorId = tokenRequestorId;
	}

	public String getTokenRequestorName() {
		return tokenRequestorName;
	}

	public void setTokenRequestorName(String tokenRequestorName) {
		this.tokenRequestorName = tokenRequestorName;
	}

	@Override
	public String toString() {
		return "Transaccion [id=" + id + ", fechaInsercion=" + fechaInsercion + ", horaInsercion=" + horaInsercion
				+ ", rrnBoleta=" + rrnBoleta + ", esReversa=" + esReversa + ", fechaTransaccion=" + fechaTransaccion
				+ ", codigoComercio=" + codigoComercio + ", nombreComercio=" + nombreComercio + ", codigoSucursal="
				+ codigoSucursal + ", nombreSucursal=" + nombreSucursal + ", codigoAutenticacion=" + codigoAutenticacion
				+ ", codigoEntidadRol=" + codigoEntidadRol + ", numeroTarjeta=" + numeroTarjeta
				+ ", numeroTarjetaOfuscado=" + numeroTarjetaOfuscado + ", tipoTarjeta=" + tipoTarjeta
				+ ", cantidadCuotas=" + cantidadCuotas + ", codigoMonedaOrigen=" + codigoMonedaOrigen
				+ ", nombreMonedaOrigen=" + nombreMonedaOrigen + ", codigoMonedaDestino=" + codigoMonedaDestino
				+ ", nombreMonedaDestino=" + nombreMonedaDestino + ", montoOrigen=" + montoOrigen + ", montoDestino="
				+ montoDestino + ", ambitoTransaccion=" + ambitoTransaccion + ", cuentaOperacion=" + cuentaOperacion
				+ ", tipoDispositivo=" + tipoDispositivo + ", nombreDispositivo=" + nombreDispositivo
				+ ", codigoPaisOrigen=" + codigoPaisOrigen + ", nombrePaisOrigen=" + nombrePaisOrigen
				+ ", codigoPaisDestino=" + codigoPaisDestino + ", nombrePaisDestino=" + nombrePaisDestino
				+ ", vencimientoTarjeta=" + vencimientoTarjeta + ", codigoServicio=" + codigoServicio
				+ ", nombreServicio=" + nombreServicio + ", codigoPrestacion=" + codigoPrestacion
				+ ", nombrePrestacion=" + nombrePrestacion + ", codigoMarcaTarjeta=" + codigoMarcaTarjeta
				+ ", nombreMarcaTarjeta=" + nombreMarcaTarjeta + ", codigoProductoTarjeta=" + codigoProductoTarjeta
				+ ", nombreProductoTarjeta=" + nombreProductoTarjeta + ", codigoAfinidad=" + codigoAfinidad
				+ ", nombreAfinidad=" + nombreAfinidad + ", codigoRetorno=" + codigoRetorno + ", nombreRetorno="
				+ nombreRetorno + ", mccComercio=" + mccComercio + ", mccNombre=" + mccNombre + ", codigoTransaccion="
				+ codigoTransaccion + ", nombreTransaccion=" + nombreTransaccion + ", fechaProceso=" + fechaProceso
				+ ", formaPago=" + formaPago + ", nombreFormaPago=" + nombreFormaPago + ", estacionOrigen="
				+ estacionOrigen + ", estacionDestino=" + estacionDestino + ", modoEntrada=" + modoEntrada
				+ ", nombreModoEntrada=" + nombreModoEntrada + ", saldoActual=" + saldoActual + ", saldoDisponible="
				+ saldoDisponible + ", estadoInitTransaccion=" + estadoInitTransaccion + ", identificadorCajeroAtm="
				+ identificadorCajeroAtm + ", entidadAdministradoraAtm=" + entidadAdministradoraAtm
				+ ", entidadPropietariaAtm=" + entidadPropietariaAtm + ", operadorTelefonicoOrigen="
				+ operadorTelefonicoOrigen + ", nroTelefonoOrigen=" + nroTelefonoOrigen + ", operadorTelefonicoDestino="
				+ operadorTelefonicoDestino + ", nroTelefonoDestino=" + nroTelefonoDestino + ", montoCotizacion="
				+ montoCotizacion + ", entidadOrigenTransferencia=" + entidadOrigenTransferencia
				+ ", nombreEntidadOrigenTransferencia=" + nombreEntidadOrigenTransferencia
				+ ", entidadDestinoTransferencia=" + entidadDestinoTransferencia
				+ ", nombreEntidadDestinoTransferencia=" + nombreEntidadDestinoTransferencia
				+ ", cuentaOrigenTransferencia=" + cuentaOrigenTransferencia + ", cuentaDestinoTransferencia="
				+ cuentaDestinoTransferencia + ", numeroAdherente=" + numeroAdherente + ", idPromocion=" + idPromocion
				+ ", procesadorEmisor=" + procesadorEmisor + ", nombreProcesadorEmisor=" + nombreProcesadorEmisor
				+ ", procesadorAdquiriente=" + procesadorAdquiriente + ", nombreProcesadorAdquiriente="
				+ nombreProcesadorAdquiriente + ", puntosTransaccion=" + puntosTransaccion + ", estadoFinTransaccion="
				+ estadoFinTransaccion + ", estadoExcedido=" + estadoExcedido + ", mensajeExtendido=" + mensajeExtendido
				+ ", tokenRequestorId=" + tokenRequestorId + ", tokenRequestorName=" + tokenRequestorName + "]";
	}

}
