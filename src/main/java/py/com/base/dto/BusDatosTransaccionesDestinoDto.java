package py.com.base.dto;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
public class BusDatosTransaccionesDestinoDto {
    private long operacion_id;
    private java.sql.Date operacion_fech_insercion;
    private java.sql.Timestamp operacion_hor_insercion;
    private java.sql.Timestamp operacion_fech_trans;
    private Integer operacion_cod_comercio;
    private Integer operacion_cod_sucursal;
    private Integer operacion_cod_entidad_rol;
    private short operacion_cant_cuotas;
    private short operacion_cod_moneda_origen;
    private short operacion_cod_moneda_destino;
    private java.math.BigDecimal operacion_mont_origen;
    private java.math.BigDecimal operacion_mont_destino;
    private short operacion_cod_pais_origen;
    private short operacion_cod_pais_destino;
    private Integer operacion_venc_tarjeta;
    private short operacion_cod_afinidad;
    private java.sql.Date operacion_fech_proc_transaccio;
    private java.math.BigDecimal operacion_sald_actual;
    private java.math.BigDecimal operacion_sald_disp;
    private Integer operacion_enti_admi_atm;
    private Integer operacion_ent_propieta_atm;
    private Integer operacion_opera_origen;
    private Integer operacion_oper_destino;
    private java.math.BigDecimal operacion_mont_cotizacion;
    private short operacion_ent_transf_origen;
    private short operacion_ent_transfe_destino;
    private short operacion_num_adherente;
    private short operacion_proce_emisor;
    private short operacion_proce_adquiriente;
    private Integer operacion_ptos_transaccion;
    private java.sql.Timestamp operacion_fec_modificacion;
    private String operacion_cod_servicio;
    private String operacion_desc_servicio;
    private String operacion_cod_prestacion;
    private String operacion_desc_prestacion;
    private String operacion_cod_marc_tarjeta;
    private String operacion_desc_marc_tarjeta;
    private String operacion_cod_prod_tarjeta;
    private String operacion_desc_prod_tarjeta;
    private String operacion_user_modificacion;
    private String operacion_desc_afinidad;
    private String operacion_cod_retorno;
    private String operacion_desc_retorno;
    private String operacion_cat_comerc_mcc;
    private String operacion_desc_mcc;
    private String operacion_cod_transaccion;
    private String operacion_desc_transaccion;
    private String operacion_desc_enti_destino_tr;
    private String operacion_form_pago;
    private String operacion_desc_form_pago;
    private String operacion_estac_origen_ss;
    private String operacion_estac_dest_ds;
    private String operacion_mod_entrada;
    private String operacion_desc_mod_entrada;
    private String operacion_cta_transfe_origen;
    private String operacion_rrnbepsa;
    private String operacion_est_reversa;
    private String operacion_cta_transfe_destino;
    private String operacion_est_in_transaccion;
    private String operacion_denom_comercial;
    private String operacion_ident_cajero_atm;
    private String operacion_desc_sucursal;
    private String operacion_cod_autenticacion;
    private String operacion_estado_replica;
    private String operacion_num_tarjeta;
    private String operacion_num_tarjeta_enmascar;
    private String operacion_tip_uso_tarjeta;
    private String operacion_id_promocion;
    private String operacion_json;
    private String operacion_desc_moneda_origen;
    private String operacion_telef_origen;
    private String operacion_desc_moneda_destino;
    private String operacion_desc_proce_emisor;
    private String operacion_telef_destino;
    private String operacion_ambito_transaccion;
    private String operacion_cta_utda_operacion;
    private String operacion_tip_dispositivo;
    private String operacion_desc_dispositivo;
    private String operacion_est_fn_transaccion;
    private String operacion_desc_pais_origen;
    private String operacion_desc_proce_adquirien;
    private String operacion_desc_pais_destino;
    private String operacion_desc_enti_origen_tr;

    public BusDatosTransaccionesDestinoDto() {
    }

    public java.sql.Timestamp getOperacion_fech_trans() {
        return operacion_fech_trans;
    }

    public void setOperacion_fech_trans(java.sql.Timestamp operacion_fech_trans) {
        this.operacion_fech_trans = operacion_fech_trans;
    }

    public String getOperacion_desc_enti_origen_tr() {
        return operacion_desc_enti_origen_tr;
    }

    public void setOperacion_desc_enti_origen_tr(String operacion_desc_enti_origen_tr) {
        this.operacion_desc_enti_origen_tr = operacion_desc_enti_origen_tr;
    }

    public String getOperacion_num_tarjeta_enmascar() {
        return operacion_num_tarjeta_enmascar;
    }

    public void setOperacion_num_tarjeta_enmascar(String operacion_num_tarjeta_enmascar) {
        this.operacion_num_tarjeta_enmascar = operacion_num_tarjeta_enmascar;
    }

    public java.math.BigDecimal getOperacion_sald_disp() {
        return operacion_sald_disp;
    }

    public void setOperacion_sald_disp(java.math.BigDecimal operacion_sald_disp) {
        this.operacion_sald_disp = operacion_sald_disp;
    }

    public String getOperacion_desc_retorno() {
        return operacion_desc_retorno;
    }

    public void setOperacion_desc_retorno(String operacion_desc_retorno) {
        this.operacion_desc_retorno = operacion_desc_retorno;
    }

    public String getOperacion_desc_enti_destino_tr() {
        return operacion_desc_enti_destino_tr;
    }

    public void setOperacion_desc_enti_destino_tr(String operacion_desc_enti_destino_tr) {
        this.operacion_desc_enti_destino_tr = operacion_desc_enti_destino_tr;
    }

    public String getOperacion_desc_mcc() {
        return operacion_desc_mcc;
    }

    public void setOperacion_desc_mcc(String operacion_desc_mcc) {
        this.operacion_desc_mcc = operacion_desc_mcc;
    }

    public String getOperacion_desc_pais_destino() {
        return operacion_desc_pais_destino;
    }

    public void setOperacion_desc_pais_destino(String operacion_desc_pais_destino) {
        this.operacion_desc_pais_destino = operacion_desc_pais_destino;
    }

    public short getOperacion_proce_adquiriente() {
        return operacion_proce_adquiriente;
    }

    public void setOperacion_proce_adquiriente(short operacion_proce_adquiriente) {
        this.operacion_proce_adquiriente = operacion_proce_adquiriente;
    }

    public String getOperacion_desc_transaccion() {
        return operacion_desc_transaccion;
    }

    public void setOperacion_desc_transaccion(String operacion_desc_transaccion) {
        this.operacion_desc_transaccion = operacion_desc_transaccion;
    }

    public String getOperacion_estac_origen_ss() {
        return operacion_estac_origen_ss;
    }

    public void setOperacion_estac_origen_ss(String operacion_estac_origen_ss) {
        this.operacion_estac_origen_ss = operacion_estac_origen_ss;
    }

    public String getOperacion_cod_retorno() {
        return operacion_cod_retorno;
    }

    public void setOperacion_cod_retorno(String operacion_cod_retorno) {
        this.operacion_cod_retorno = operacion_cod_retorno;
    }

    public java.math.BigDecimal getOperacion_mont_cotizacion() {
        return operacion_mont_cotizacion;
    }

    public void setOperacion_mont_cotizacion(java.math.BigDecimal operacion_mont_cotizacion) {
        this.operacion_mont_cotizacion = operacion_mont_cotizacion;
    }

    public short getOperacion_cod_pais_destino() {
        return operacion_cod_pais_destino;
    }

    public void setOperacion_cod_pais_destino(short operacion_cod_pais_destino) {
        this.operacion_cod_pais_destino = operacion_cod_pais_destino;
    }

    public short getOperacion_cod_moneda_origen() {
        return operacion_cod_moneda_origen;
    }

    public void setOperacion_cod_moneda_origen(short operacion_cod_moneda_origen) {
        this.operacion_cod_moneda_origen = operacion_cod_moneda_origen;
    }

    public String getOperacion_desc_afinidad() {
        return operacion_desc_afinidad;
    }

    public void setOperacion_desc_afinidad(String operacion_desc_afinidad) {
        this.operacion_desc_afinidad = operacion_desc_afinidad;
    }

    public String getOperacion_cod_servicio() {
        return operacion_cod_servicio;
    }

    public void setOperacion_cod_servicio(String operacion_cod_servicio) {
        this.operacion_cod_servicio = operacion_cod_servicio;
    }

    public String getOperacion_cod_autenticacion() {
        return operacion_cod_autenticacion;
    }

    public void setOperacion_cod_autenticacion(String operacion_cod_autenticacion) {
        this.operacion_cod_autenticacion = operacion_cod_autenticacion;
    }

    public String getOperacion_telef_destino() {
        return operacion_telef_destino;
    }

    public void setOperacion_telef_destino(String operacion_telef_destino) {
        this.operacion_telef_destino = operacion_telef_destino;
    }

    public String getOperacion_json() {
        return operacion_json;
    }

    public void setOperacion_json(String operacion_json) {
        this.operacion_json = operacion_json;
    }

    public String getOperacion_desc_pais_origen() {
        return operacion_desc_pais_origen;
    }

    public void setOperacion_desc_pais_origen(String operacion_desc_pais_origen) {
        this.operacion_desc_pais_origen = operacion_desc_pais_origen;
    }

    public String getOperacion_cod_prestacion() {
        return operacion_cod_prestacion;
    }

    public void setOperacion_cod_prestacion(String operacion_cod_prestacion) {
        this.operacion_cod_prestacion = operacion_cod_prestacion;
    }

    public String getOperacion_tip_uso_tarjeta() {
        return operacion_tip_uso_tarjeta;
    }

    public void setOperacion_tip_uso_tarjeta(String operacion_tip_uso_tarjeta) {
        this.operacion_tip_uso_tarjeta = operacion_tip_uso_tarjeta;
    }

    public short getOperacion_cant_cuotas() {
        return operacion_cant_cuotas;
    }

    public void setOperacion_cant_cuotas(short operacion_cant_cuotas) {
        this.operacion_cant_cuotas = operacion_cant_cuotas;
    }

    public Integer getOperacion_ent_propieta_atm() {
        return operacion_ent_propieta_atm;
    }

    public void setOperacion_ent_propieta_atm(Integer operacion_ent_propieta_atm) {
        this.operacion_ent_propieta_atm = operacion_ent_propieta_atm;
    }

    public String getOperacion_desc_prestacion() {
        return operacion_desc_prestacion;
    }

    public void setOperacion_desc_prestacion(String operacion_desc_prestacion) {
        this.operacion_desc_prestacion = operacion_desc_prestacion;
    }

    public String getOperacion_desc_sucursal() {
        return operacion_desc_sucursal;
    }

    public void setOperacion_desc_sucursal(String operacion_desc_sucursal) {
        this.operacion_desc_sucursal = operacion_desc_sucursal;
    }

    public String getOperacion_telef_origen() {
        return operacion_telef_origen;
    }

    public void setOperacion_telef_origen(String operacion_telef_origen) {
        this.operacion_telef_origen = operacion_telef_origen;
    }

    public String getOperacion_est_reversa() {
        return operacion_est_reversa;
    }

    public void setOperacion_est_reversa(String operacion_est_reversa) {
        this.operacion_est_reversa = operacion_est_reversa;
    }

    public String getOperacion_ident_cajero_atm() {
        return operacion_ident_cajero_atm;
    }

    public void setOperacion_ident_cajero_atm(String operacion_ident_cajero_atm) {
        this.operacion_ident_cajero_atm = operacion_ident_cajero_atm;
    }

    public String getOperacion_desc_form_pago() {
        return operacion_desc_form_pago;
    }

    public void setOperacion_desc_form_pago(String operacion_desc_form_pago) {
        this.operacion_desc_form_pago = operacion_desc_form_pago;
    }

    public short getOperacion_ent_transf_origen() {
        return operacion_ent_transf_origen;
    }

    public void setOperacion_ent_transf_origen(short operacion_ent_transf_origen) {
        this.operacion_ent_transf_origen = operacion_ent_transf_origen;
    }

    public String getOperacion_desc_prod_tarjeta() {
        return operacion_desc_prod_tarjeta;
    }

    public void setOperacion_desc_prod_tarjeta(String operacion_desc_prod_tarjeta) {
        this.operacion_desc_prod_tarjeta = operacion_desc_prod_tarjeta;
    }

    public String getOperacion_desc_servicio() {
        return operacion_desc_servicio;
    }

    public void setOperacion_desc_servicio(String operacion_desc_servicio) {
        this.operacion_desc_servicio = operacion_desc_servicio;
    }

    public String getOperacion_id_promocion() {
        return operacion_id_promocion;
    }

    public void setOperacion_id_promocion(String operacion_id_promocion) {
        this.operacion_id_promocion = operacion_id_promocion;
    }

    public String getOperacion_tip_dispositivo() {
        return operacion_tip_dispositivo;
    }

    public void setOperacion_tip_dispositivo(String operacion_tip_dispositivo) {
        this.operacion_tip_dispositivo = operacion_tip_dispositivo;
    }

    public short getOperacion_cod_pais_origen() {
        return operacion_cod_pais_origen;
    }

    public void setOperacion_cod_pais_origen(short operacion_cod_pais_origen) {
        this.operacion_cod_pais_origen = operacion_cod_pais_origen;
    }

    public Integer getOperacion_oper_destino() {
        return operacion_oper_destino;
    }

    public void setOperacion_oper_destino(Integer operacion_oper_destino) {
        this.operacion_oper_destino = operacion_oper_destino;
    }

    public String getOperacion_cta_transfe_destino() {
        return operacion_cta_transfe_destino;
    }

    public void setOperacion_cta_transfe_destino(String operacion_cta_transfe_destino) {
        this.operacion_cta_transfe_destino = operacion_cta_transfe_destino;
    }

    public String getOperacion_cod_marc_tarjeta() {
        return operacion_cod_marc_tarjeta;
    }

    public void setOperacion_cod_marc_tarjeta(String operacion_cod_marc_tarjeta) {
        this.operacion_cod_marc_tarjeta = operacion_cod_marc_tarjeta;
    }

    public java.math.BigDecimal getOperacion_mont_destino() {
        return operacion_mont_destino;
    }

    public void setOperacion_mont_destino(java.math.BigDecimal operacion_mont_destino) {
        this.operacion_mont_destino = operacion_mont_destino;
    }

    public java.sql.Date getOperacion_fech_proc_transaccio() {
        return operacion_fech_proc_transaccio;
    }

    public void setOperacion_fech_proc_transaccio(java.sql.Date operacion_fech_proc_transaccio) {
        this.operacion_fech_proc_transaccio = operacion_fech_proc_transaccio;
    }

    public Integer getOperacion_venc_tarjeta() {
        return operacion_venc_tarjeta;
    }

    public void setOperacion_venc_tarjeta(Integer operacion_venc_tarjeta) {
        this.operacion_venc_tarjeta = operacion_venc_tarjeta;
    }

    public short getOperacion_proce_emisor() {
        return operacion_proce_emisor;
    }

    public void setOperacion_proce_emisor(short operacion_proce_emisor) {
        this.operacion_proce_emisor = operacion_proce_emisor;
    }

    public java.sql.Timestamp getOperacion_hor_insercion() {
        return operacion_hor_insercion;
    }

    public void setOperacion_hor_insercion(java.sql.Timestamp operacion_hor_insercion) {
        this.operacion_hor_insercion = operacion_hor_insercion;
    }

    public java.sql.Date getOperacion_fech_insercion() {
        return operacion_fech_insercion;
    }

    public void setOperacion_fech_insercion(java.sql.Date operacion_fech_insercion) {
        this.operacion_fech_insercion = operacion_fech_insercion;
    }

    public java.math.BigDecimal getOperacion_mont_origen() {
        return operacion_mont_origen;
    }

    public void setOperacion_mont_origen(java.math.BigDecimal operacion_mont_origen) {
        this.operacion_mont_origen = operacion_mont_origen;
    }

    public String getOperacion_desc_moneda_destino() {
        return operacion_desc_moneda_destino;
    }

    public void setOperacion_desc_moneda_destino(String operacion_desc_moneda_destino) {
        this.operacion_desc_moneda_destino = operacion_desc_moneda_destino;
    }

    public String getOperacion_cta_utda_operacion() {
        return operacion_cta_utda_operacion;
    }

    public void setOperacion_cta_utda_operacion(String operacion_cta_utda_operacion) {
        this.operacion_cta_utda_operacion = operacion_cta_utda_operacion;
    }

    public Integer getOperacion_cod_sucursal() {
        return operacion_cod_sucursal;
    }

    public void setOperacion_cod_sucursal(Integer operacion_cod_sucursal) {
        this.operacion_cod_sucursal = operacion_cod_sucursal;
    }

    public short getOperacion_num_adherente() {
        return operacion_num_adherente;
    }

    public void setOperacion_num_adherente(short operacion_num_adherente) {
        this.operacion_num_adherente = operacion_num_adherente;
    }

    public String getOperacion_desc_dispositivo() {
        return operacion_desc_dispositivo;
    }

    public void setOperacion_desc_dispositivo(String operacion_desc_dispositivo) {
        this.operacion_desc_dispositivo = operacion_desc_dispositivo;
    }

    public Integer getOperacion_cod_comercio() {
        return operacion_cod_comercio;
    }

    public void setOperacion_cod_comercio(Integer operacion_cod_comercio) {
        this.operacion_cod_comercio = operacion_cod_comercio;
    }

    public short getOperacion_cod_afinidad() {
        return operacion_cod_afinidad;
    }

    public void setOperacion_cod_afinidad(short operacion_cod_afinidad) {
        this.operacion_cod_afinidad = operacion_cod_afinidad;
    }

    public String getOperacion_cat_comerc_mcc() {
        return operacion_cat_comerc_mcc;
    }

    public void setOperacion_cat_comerc_mcc(String operacion_cat_comerc_mcc) {
        this.operacion_cat_comerc_mcc = operacion_cat_comerc_mcc;
    }

    public String getOperacion_ambito_transaccion() {
        return operacion_ambito_transaccion;
    }

    public void setOperacion_ambito_transaccion(String operacion_ambito_transaccion) {
        this.operacion_ambito_transaccion = operacion_ambito_transaccion;
    }

    public String getOperacion_est_fn_transaccion() {
        return operacion_est_fn_transaccion;
    }

    public void setOperacion_est_fn_transaccion(String operacion_est_fn_transaccion) {
        this.operacion_est_fn_transaccion = operacion_est_fn_transaccion;
    }

    public short getOperacion_cod_moneda_destino() {
        return operacion_cod_moneda_destino;
    }

    public void setOperacion_cod_moneda_destino(short operacion_cod_moneda_destino) {
        this.operacion_cod_moneda_destino = operacion_cod_moneda_destino;
    }

    public Integer getOperacion_ptos_transaccion() {
        return operacion_ptos_transaccion;
    }

    public void setOperacion_ptos_transaccion(Integer operacion_ptos_transaccion) {
        this.operacion_ptos_transaccion = operacion_ptos_transaccion;
    }

    public short getOperacion_ent_transfe_destino() {
        return operacion_ent_transfe_destino;
    }

    public void setOperacion_ent_transfe_destino(short operacion_ent_transfe_destino) {
        this.operacion_ent_transfe_destino = operacion_ent_transfe_destino;
    }

    public String getOperacion_estado_replica() {
        return operacion_estado_replica;
    }

    public void setOperacion_estado_replica(String operacion_estado_replica) {
        this.operacion_estado_replica = operacion_estado_replica;
    }

    public String getOperacion_user_modificacion() {
        return operacion_user_modificacion;
    }

    public void setOperacion_user_modificacion(String operacion_user_modificacion) {
        this.operacion_user_modificacion = operacion_user_modificacion;
    }

    public String getOperacion_desc_moneda_origen() {
        return operacion_desc_moneda_origen;
    }

    public void setOperacion_desc_moneda_origen(String operacion_desc_moneda_origen) {
        this.operacion_desc_moneda_origen = operacion_desc_moneda_origen;
    }

    public String getOperacion_denom_comercial() {
        return operacion_denom_comercial;
    }

    public void setOperacion_denom_comercial(String operacion_denom_comercial) {
        this.operacion_denom_comercial = operacion_denom_comercial;
    }

    public String getOperacion_cod_transaccion() {
        return operacion_cod_transaccion;
    }

    public void setOperacion_cod_transaccion(String operacion_cod_transaccion) {
        this.operacion_cod_transaccion = operacion_cod_transaccion;
    }

    public String getOperacion_cta_transfe_origen() {
        return operacion_cta_transfe_origen;
    }

    public void setOperacion_cta_transfe_origen(String operacion_cta_transfe_origen) {
        this.operacion_cta_transfe_origen = operacion_cta_transfe_origen;
    }

    public String getOperacion_desc_proce_emisor() {
        return operacion_desc_proce_emisor;
    }

    public void setOperacion_desc_proce_emisor(String operacion_desc_proce_emisor) {
        this.operacion_desc_proce_emisor = operacion_desc_proce_emisor;
    }

    public String getOperacion_desc_marc_tarjeta() {
        return operacion_desc_marc_tarjeta;
    }

    public void setOperacion_desc_marc_tarjeta(String operacion_desc_marc_tarjeta) {
        this.operacion_desc_marc_tarjeta = operacion_desc_marc_tarjeta;
    }

    public String getOperacion_rrnbepsa() {
        return operacion_rrnbepsa;
    }

    public void setOperacion_rrnbepsa(String operacion_rrnbepsa) {
        this.operacion_rrnbepsa = operacion_rrnbepsa;
    }

    public String getOperacion_mod_entrada() {
        return operacion_mod_entrada;
    }

    public void setOperacion_mod_entrada(String operacion_mod_entrada) {
        this.operacion_mod_entrada = operacion_mod_entrada;
    }

    public String getOperacion_est_in_transaccion() {
        return operacion_est_in_transaccion;
    }

    public void setOperacion_est_in_transaccion(String operacion_est_in_transaccion) {
        this.operacion_est_in_transaccion = operacion_est_in_transaccion;
    }

    public String getOperacion_desc_proce_adquirien() {
        return operacion_desc_proce_adquirien;
    }

    public void setOperacion_desc_proce_adquirien(String operacion_desc_proce_adquirien) {
        this.operacion_desc_proce_adquirien = operacion_desc_proce_adquirien;
    }

    public String getOperacion_desc_mod_entrada() {
        return operacion_desc_mod_entrada;
    }

    public void setOperacion_desc_mod_entrada(String operacion_desc_mod_entrada) {
        this.operacion_desc_mod_entrada = operacion_desc_mod_entrada;
    }

    public java.sql.Timestamp getOperacion_fec_modificacion() {
        return operacion_fec_modificacion;
    }

    public void setOperacion_fec_modificacion(java.sql.Timestamp operacion_fec_modificacion) {
        this.operacion_fec_modificacion = operacion_fec_modificacion;
    }

    public String getOperacion_form_pago() {
        return operacion_form_pago;
    }

    public void setOperacion_form_pago(String operacion_form_pago) {
        this.operacion_form_pago = operacion_form_pago;
    }

    public java.math.BigDecimal getOperacion_sald_actual() {
        return operacion_sald_actual;
    }

    public void setOperacion_sald_actual(java.math.BigDecimal operacion_sald_actual) {
        this.operacion_sald_actual = operacion_sald_actual;
    }

    public Integer getOperacion_opera_origen() {
        return operacion_opera_origen;
    }

    public void setOperacion_opera_origen(Integer operacion_opera_origen) {
        this.operacion_opera_origen = operacion_opera_origen;
    }

    public String getOperacion_estac_dest_ds() {
        return operacion_estac_dest_ds;
    }

    public void setOperacion_estac_dest_ds(String operacion_estac_dest_ds) {
        this.operacion_estac_dest_ds = operacion_estac_dest_ds;
    }

    public String getOperacion_cod_prod_tarjeta() {
        return operacion_cod_prod_tarjeta;
    }

    public void setOperacion_cod_prod_tarjeta(String operacion_cod_prod_tarjeta) {
        this.operacion_cod_prod_tarjeta = operacion_cod_prod_tarjeta;
    }

    public String getOperacion_num_tarjeta() {
        return operacion_num_tarjeta;
    }

    public void setOperacion_num_tarjeta(String operacion_num_tarjeta) {
        this.operacion_num_tarjeta = operacion_num_tarjeta;
    }

    public long getOperacion_id() {
        return operacion_id;
    }

    public void setOperacion_id(long operacion_id) {
        this.operacion_id = operacion_id;
    }

    public Integer getOperacion_enti_admi_atm() {
        return operacion_enti_admi_atm;
    }

    public void setOperacion_enti_admi_atm(Integer operacion_enti_admi_atm) {
        this.operacion_enti_admi_atm = operacion_enti_admi_atm;
    }

    public Integer getOperacion_cod_entidad_rol() {
        return operacion_cod_entidad_rol;
    }

    public void setOperacion_cod_entidad_rol(Integer operacion_cod_entidad_rol) {
        this.operacion_cod_entidad_rol = operacion_cod_entidad_rol;
    }

    @Override
    public String toString() {
        return "BusDatosTransaccionesDestinoDto [" +
            "operacion_fech_trans='" + operacion_fech_trans + "'" +
            "operacion_desc_enti_origen_tr='" + operacion_desc_enti_origen_tr + "'" +
            "operacion_num_tarjeta_enmascar='" + operacion_num_tarjeta_enmascar + "'" +
            "operacion_sald_disp='" + operacion_sald_disp + "'" +
            "operacion_desc_retorno='" + operacion_desc_retorno + "'" +
            "operacion_desc_enti_destino_tr='" + operacion_desc_enti_destino_tr + "'" +
            "operacion_desc_mcc='" + operacion_desc_mcc + "'" +
            "operacion_desc_pais_destino='" + operacion_desc_pais_destino + "'" +
            "operacion_proce_adquiriente='" + operacion_proce_adquiriente + "'" +
            "operacion_desc_transaccion='" + operacion_desc_transaccion + "'" +
            "operacion_estac_origen_ss='" + operacion_estac_origen_ss + "'" +
            "operacion_cod_retorno='" + operacion_cod_retorno + "'" +
            "operacion_mont_cotizacion='" + operacion_mont_cotizacion + "'" +
            "operacion_cod_pais_destino='" + operacion_cod_pais_destino + "'" +
            "operacion_cod_moneda_origen='" + operacion_cod_moneda_origen + "'" +
            "operacion_desc_afinidad='" + operacion_desc_afinidad + "'" +
            "operacion_cod_servicio='" + operacion_cod_servicio + "'" +
            "operacion_cod_autenticacion='" + operacion_cod_autenticacion + "'" +
            "operacion_telef_destino='" + operacion_telef_destino + "'" +
            "operacion_json='" + operacion_json + "'" +
            "operacion_desc_pais_origen='" + operacion_desc_pais_origen + "'" +
            "operacion_cod_prestacion='" + operacion_cod_prestacion + "'" +
            "operacion_tip_uso_tarjeta='" + operacion_tip_uso_tarjeta + "'" +
            "operacion_cant_cuotas='" + operacion_cant_cuotas + "'" +
            "operacion_ent_propieta_atm='" + operacion_ent_propieta_atm + "'" +
            "operacion_desc_prestacion='" + operacion_desc_prestacion + "'" +
            "operacion_desc_sucursal='" + operacion_desc_sucursal + "'" +
            "operacion_telef_origen='" + operacion_telef_origen + "'" +
            "operacion_est_reversa='" + operacion_est_reversa + "'" +
            "operacion_ident_cajero_atm='" + operacion_ident_cajero_atm + "'" +
            "operacion_desc_form_pago='" + operacion_desc_form_pago + "'" +
            "operacion_ent_transf_origen='" + operacion_ent_transf_origen + "'" +
            "operacion_desc_prod_tarjeta='" + operacion_desc_prod_tarjeta + "'" +
            "operacion_desc_servicio='" + operacion_desc_servicio + "'" +
            "operacion_id_promocion='" + operacion_id_promocion + "'" +
            "operacion_tip_dispositivo='" + operacion_tip_dispositivo + "'" +
            "operacion_cod_pais_origen='" + operacion_cod_pais_origen + "'" +
            "operacion_oper_destino='" + operacion_oper_destino + "'" +
            "operacion_cta_transfe_destino='" + operacion_cta_transfe_destino + "'" +
            "operacion_cod_marc_tarjeta='" + operacion_cod_marc_tarjeta + "'" +
            "operacion_mont_destino='" + operacion_mont_destino + "'" +
            "operacion_fech_proc_transaccio='" + operacion_fech_proc_transaccio + "'" +
            "operacion_venc_tarjeta='" + operacion_venc_tarjeta + "'" +
            "operacion_proce_emisor='" + operacion_proce_emisor + "'" +
            "operacion_hor_insercion='" + operacion_hor_insercion + "'" +
            "operacion_fech_insercion='" + operacion_fech_insercion + "'" +
            "operacion_mont_origen='" + operacion_mont_origen + "'" +
            "operacion_desc_moneda_destino='" + operacion_desc_moneda_destino + "'" +
            "operacion_cta_utda_operacion='" + operacion_cta_utda_operacion + "'" +
            "operacion_cod_sucursal='" + operacion_cod_sucursal + "'" +
            "operacion_num_adherente='" + operacion_num_adherente + "'" +
            "operacion_desc_dispositivo='" + operacion_desc_dispositivo + "'" +
            "operacion_cod_comercio='" + operacion_cod_comercio + "'" +
            "operacion_cod_afinidad='" + operacion_cod_afinidad + "'" +
            "operacion_cat_comerc_mcc='" + operacion_cat_comerc_mcc + "'" +
            "operacion_ambito_transaccion='" + operacion_ambito_transaccion + "'" +
            "operacion_est_fn_transaccion='" + operacion_est_fn_transaccion + "'" +
            "operacion_cod_moneda_destino='" + operacion_cod_moneda_destino + "'" +
            "operacion_ptos_transaccion='" + operacion_ptos_transaccion + "'" +
            "operacion_ent_transfe_destino='" + operacion_ent_transfe_destino + "'" +
            "operacion_estado_replica='" + operacion_estado_replica + "'" +
            "operacion_user_modificacion='" + operacion_user_modificacion + "'" +
            "operacion_desc_moneda_origen='" + operacion_desc_moneda_origen + "'" +
            "operacion_denom_comercial='" + operacion_denom_comercial + "'" +
            "operacion_cod_transaccion='" + operacion_cod_transaccion + "'" +
            "operacion_cta_transfe_origen='" + operacion_cta_transfe_origen + "'" +
            "operacion_desc_proce_emisor='" + operacion_desc_proce_emisor + "'" +
            "operacion_desc_marc_tarjeta='" + operacion_desc_marc_tarjeta + "'" +
            "operacion_rrnbepsa='" + operacion_rrnbepsa + "'" +
            "operacion_mod_entrada='" + operacion_mod_entrada + "'" +
            "operacion_est_in_transaccion='" + operacion_est_in_transaccion + "'" +
            "operacion_desc_proce_adquirien='" + operacion_desc_proce_adquirien + "'" +
            "operacion_desc_mod_entrada='" + operacion_desc_mod_entrada + "'" +
            "operacion_fec_modificacion='" + operacion_fec_modificacion + "'" +
            "operacion_form_pago='" + operacion_form_pago + "'" +
            "operacion_sald_actual='" + operacion_sald_actual + "'" +
            "operacion_opera_origen='" + operacion_opera_origen + "'" +
            "operacion_estac_dest_ds='" + operacion_estac_dest_ds + "'" +
            "operacion_cod_prod_tarjeta='" + operacion_cod_prod_tarjeta + "'" +
            "operacion_num_tarjeta='" + operacion_num_tarjeta + "'" +
            "operacion_id='" + operacion_id + "'" +
            "operacion_enti_admi_atm='" + operacion_enti_admi_atm + "'" +
            "operacion_cod_entidad_rol='" + operacion_cod_entidad_rol + "'" +
            ']';
    }
}
