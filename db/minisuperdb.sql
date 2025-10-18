--
-- PostgreSQL database dump
--

\restrict q8Lf2zlCC0R3Tip8fx50q8g1Q0vmnfdMs4eLzmvKBP8JAuu6jERS7zowPCXlqPZ

-- Dumped from database version 17.4
-- Dumped by pg_dump version 17.6

-- Started on 2025-10-18 10:24:23 -03

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 238 (class 1259 OID 32938)
-- Name: caja; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.caja (
    id_caja integer NOT NULL,
    nombre character varying(50) NOT NULL,
    descripcion text,
    estado boolean DEFAULT true,
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.caja OWNER TO postgres;

--
-- TOC entry 240 (class 1259 OID 32949)
-- Name: caja_apertura_cierre; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.caja_apertura_cierre (
    id_apertura integer NOT NULL,
    id_caja integer NOT NULL,
    id_usuario integer NOT NULL,
    fecha_apertura timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre timestamp without time zone,
    monto_inicial numeric(12,2) NOT NULL,
    monto_cierre numeric(12,2),
    estado character varying(20) DEFAULT 'ABIERTA'::character varying
);


ALTER TABLE public.caja_apertura_cierre OWNER TO postgres;

--
-- TOC entry 239 (class 1259 OID 32948)
-- Name: caja_apertura_cierre_id_apertura_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.caja_apertura_cierre_id_apertura_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.caja_apertura_cierre_id_apertura_seq OWNER TO postgres;

--
-- TOC entry 5050 (class 0 OID 0)
-- Dependencies: 239
-- Name: caja_apertura_cierre_id_apertura_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.caja_apertura_cierre_id_apertura_seq OWNED BY public.caja_apertura_cierre.id_apertura;


--
-- TOC entry 237 (class 1259 OID 32937)
-- Name: caja_id_caja_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.caja_id_caja_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.caja_id_caja_seq OWNER TO postgres;

--
-- TOC entry 5051 (class 0 OID 0)
-- Dependencies: 237
-- Name: caja_id_caja_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.caja_id_caja_seq OWNED BY public.caja.id_caja;


--
-- TOC entry 242 (class 1259 OID 32968)
-- Name: caja_movimiento; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.caja_movimiento (
    id_movimiento integer NOT NULL,
    id_apertura integer NOT NULL,
    tipo character varying(20) NOT NULL,
    concepto character varying(150) NOT NULL,
    monto numeric(12,2) NOT NULL,
    fecha_movimiento timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    id_usuario integer NOT NULL
);


ALTER TABLE public.caja_movimiento OWNER TO postgres;

--
-- TOC entry 241 (class 1259 OID 32967)
-- Name: caja_movimiento_id_movimiento_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.caja_movimiento_id_movimiento_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.caja_movimiento_id_movimiento_seq OWNER TO postgres;

--
-- TOC entry 5052 (class 0 OID 0)
-- Dependencies: 241
-- Name: caja_movimiento_id_movimiento_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.caja_movimiento_id_movimiento_seq OWNED BY public.caja_movimiento.id_movimiento;


--
-- TOC entry 218 (class 1259 OID 32789)
-- Name: categoria_producto; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.categoria_producto (
    id_categoria integer NOT NULL,
    nombre character varying(100) NOT NULL
);


ALTER TABLE public.categoria_producto OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 32788)
-- Name: categoria_producto_id_categoria_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.categoria_producto_id_categoria_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.categoria_producto_id_categoria_seq OWNER TO postgres;

--
-- TOC entry 5053 (class 0 OID 0)
-- Dependencies: 217
-- Name: categoria_producto_id_categoria_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.categoria_producto_id_categoria_seq OWNED BY public.categoria_producto.id_categoria;


--
-- TOC entry 228 (class 1259 OID 32861)
-- Name: cliente; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cliente (
    id_cliente integer NOT NULL,
    nombre character varying(150) NOT NULL,
    apellido character varying(150) NOT NULL,
    ruc_ci character varying(20) NOT NULL,
    direccion text,
    telefono character varying(20),
    email character varying(100),
    fecha_alta timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    estado boolean DEFAULT true
);


ALTER TABLE public.cliente OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 32860)
-- Name: cliente_id_cliente_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.cliente_id_cliente_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.cliente_id_cliente_seq OWNER TO postgres;

--
-- TOC entry 5054 (class 0 OID 0)
-- Dependencies: 227
-- Name: cliente_id_cliente_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.cliente_id_cliente_seq OWNED BY public.cliente.id_cliente;


--
-- TOC entry 248 (class 1259 OID 33027)
-- Name: detalle_factura; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.detalle_factura (
    id_detalle integer NOT NULL,
    id_factura integer NOT NULL,
    id_producto integer NOT NULL,
    cantidad integer NOT NULL,
    precio_unitario numeric(12,2) NOT NULL,
    subtotal numeric(12,2) NOT NULL,
    iva_aplicado numeric(12,2) DEFAULT 0
);


ALTER TABLE public.detalle_factura OWNER TO postgres;

--
-- TOC entry 247 (class 1259 OID 33026)
-- Name: detalle_factura_id_detalle_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.detalle_factura_id_detalle_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.detalle_factura_id_detalle_seq OWNER TO postgres;

--
-- TOC entry 5055 (class 0 OID 0)
-- Dependencies: 247
-- Name: detalle_factura_id_detalle_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.detalle_factura_id_detalle_seq OWNED BY public.detalle_factura.id_detalle;


--
-- TOC entry 232 (class 1259 OID 32888)
-- Name: detalle_venta; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.detalle_venta (
    id_detalle integer NOT NULL,
    id_venta integer NOT NULL,
    id_producto integer NOT NULL,
    cantidad integer NOT NULL,
    precio_unitario numeric(12,2) NOT NULL,
    subtotal numeric(12,2) NOT NULL
);


ALTER TABLE public.detalle_venta OWNER TO postgres;

--
-- TOC entry 231 (class 1259 OID 32887)
-- Name: detalle_venta_id_detalle_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.detalle_venta_id_detalle_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.detalle_venta_id_detalle_seq OWNER TO postgres;

--
-- TOC entry 5056 (class 0 OID 0)
-- Dependencies: 231
-- Name: detalle_venta_id_detalle_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.detalle_venta_id_detalle_seq OWNED BY public.detalle_venta.id_detalle;


--
-- TOC entry 246 (class 1259 OID 32996)
-- Name: factura; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.factura (
    id_factura integer NOT NULL,
    id_venta integer NOT NULL,
    id_cliente integer NOT NULL,
    id_timbrado integer NOT NULL,
    numero_factura character varying(30) NOT NULL,
    tipo character varying(20) NOT NULL,
    fecha_emision timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    total_exento numeric(12,2) DEFAULT 0,
    total_gravado5 numeric(12,2) DEFAULT 0,
    total_gravado10 numeric(12,2) DEFAULT 0,
    total_iva5 numeric(12,2) DEFAULT 0,
    total_iva10 numeric(12,2) DEFAULT 0,
    total_general numeric(12,2) NOT NULL,
    estado character varying(20) DEFAULT 'ACTIVA'::character varying
);


ALTER TABLE public.factura OWNER TO postgres;

--
-- TOC entry 245 (class 1259 OID 32995)
-- Name: factura_id_factura_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.factura_id_factura_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.factura_id_factura_seq OWNER TO postgres;

--
-- TOC entry 5057 (class 0 OID 0)
-- Dependencies: 245
-- Name: factura_id_factura_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.factura_id_factura_seq OWNED BY public.factura.id_factura;


--
-- TOC entry 222 (class 1259 OID 32807)
-- Name: impuesto; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.impuesto (
    id_impuesto integer NOT NULL,
    nombre character varying(50) NOT NULL,
    porcentaje numeric(5,2) NOT NULL
);


ALTER TABLE public.impuesto OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 32806)
-- Name: impuesto_id_impuesto_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.impuesto_id_impuesto_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.impuesto_id_impuesto_seq OWNER TO postgres;

--
-- TOC entry 5058 (class 0 OID 0)
-- Dependencies: 221
-- Name: impuesto_id_impuesto_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.impuesto_id_impuesto_seq OWNED BY public.impuesto.id_impuesto;


--
-- TOC entry 250 (class 1259 OID 33045)
-- Name: inventario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.inventario (
    id_inventario integer NOT NULL,
    id_producto integer NOT NULL,
    stock_actual integer DEFAULT 0,
    stock_reservado integer DEFAULT 0,
    stock_disponible integer GENERATED ALWAYS AS ((stock_actual - stock_reservado)) STORED,
    fecha_actualizacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.inventario OWNER TO postgres;

--
-- TOC entry 254 (class 1259 OID 33079)
-- Name: inventario_ajuste; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.inventario_ajuste (
    id_ajuste integer NOT NULL,
    id_producto integer NOT NULL,
    stock_sistema integer NOT NULL,
    stock_fisico integer NOT NULL,
    diferencia integer NOT NULL,
    motivo character varying(150) NOT NULL,
    fecha_ajuste timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    id_usuario integer NOT NULL
);


ALTER TABLE public.inventario_ajuste OWNER TO postgres;

--
-- TOC entry 253 (class 1259 OID 33078)
-- Name: inventario_ajuste_id_ajuste_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.inventario_ajuste_id_ajuste_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.inventario_ajuste_id_ajuste_seq OWNER TO postgres;

--
-- TOC entry 5059 (class 0 OID 0)
-- Dependencies: 253
-- Name: inventario_ajuste_id_ajuste_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.inventario_ajuste_id_ajuste_seq OWNED BY public.inventario_ajuste.id_ajuste;


--
-- TOC entry 249 (class 1259 OID 33044)
-- Name: inventario_id_inventario_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.inventario_id_inventario_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.inventario_id_inventario_seq OWNER TO postgres;

--
-- TOC entry 5060 (class 0 OID 0)
-- Dependencies: 249
-- Name: inventario_id_inventario_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.inventario_id_inventario_seq OWNED BY public.inventario.id_inventario;


--
-- TOC entry 252 (class 1259 OID 33061)
-- Name: inventario_movimiento; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.inventario_movimiento (
    id_movimiento integer NOT NULL,
    id_producto integer NOT NULL,
    tipo character varying(20) NOT NULL,
    cantidad integer NOT NULL,
    motivo character varying(150),
    referencia character varying(50),
    fecha_movimiento timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    id_usuario integer NOT NULL
);


ALTER TABLE public.inventario_movimiento OWNER TO postgres;

--
-- TOC entry 251 (class 1259 OID 33060)
-- Name: inventario_movimiento_id_movimiento_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.inventario_movimiento_id_movimiento_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.inventario_movimiento_id_movimiento_seq OWNER TO postgres;

--
-- TOC entry 5061 (class 0 OID 0)
-- Dependencies: 251
-- Name: inventario_movimiento_id_movimiento_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.inventario_movimiento_id_movimiento_seq OWNED BY public.inventario_movimiento.id_movimiento;


--
-- TOC entry 220 (class 1259 OID 32798)
-- Name: marca; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.marca (
    id_marca integer NOT NULL,
    nombre character varying(100) NOT NULL
);


ALTER TABLE public.marca OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 32797)
-- Name: marca_id_marca_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.marca_id_marca_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.marca_id_marca_seq OWNER TO postgres;

--
-- TOC entry 5062 (class 0 OID 0)
-- Dependencies: 219
-- Name: marca_id_marca_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.marca_id_marca_seq OWNED BY public.marca.id_marca;


--
-- TOC entry 226 (class 1259 OID 32825)
-- Name: producto; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.producto (
    id_producto integer NOT NULL,
    codigo_barra character varying(50) NOT NULL,
    codigo_interno character varying(50),
    nombre character varying(150) NOT NULL,
    descripcion text,
    id_categoria integer NOT NULL,
    id_marca integer,
    id_impuesto integer,
    precio_costo numeric(12,2) NOT NULL,
    precio_venta numeric(12,2) NOT NULL,
    precio_mayorista numeric(12,2),
    descuento_maximo numeric(5,2),
    stock_minimo integer DEFAULT 0,
    unidad_medida character varying(20) NOT NULL,
    peso_neto numeric(10,3),
    volumen numeric(10,3),
    estado character(1) DEFAULT 'A'::bpchar,
    es_perecedero boolean DEFAULT false,
    fecha_vencimiento date,
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    imagen_url character varying(255),
    proveedor_principal integer,
    ubicacion_gondola character varying(50)
);


ALTER TABLE public.producto OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 32824)
-- Name: producto_id_producto_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.producto_id_producto_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.producto_id_producto_seq OWNER TO postgres;

--
-- TOC entry 5063 (class 0 OID 0)
-- Dependencies: 225
-- Name: producto_id_producto_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.producto_id_producto_seq OWNED BY public.producto.id_producto;


--
-- TOC entry 224 (class 1259 OID 32814)
-- Name: proveedor; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.proveedor (
    id_proveedor integer NOT NULL,
    nombre character varying(150) NOT NULL,
    ruc character varying(20) NOT NULL,
    telefono character varying(50),
    email character varying(100),
    direccion text
);


ALTER TABLE public.proveedor OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 32813)
-- Name: proveedor_id_proveedor_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.proveedor_id_proveedor_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.proveedor_id_proveedor_seq OWNER TO postgres;

--
-- TOC entry 5064 (class 0 OID 0)
-- Dependencies: 223
-- Name: proveedor_id_proveedor_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.proveedor_id_proveedor_seq OWNED BY public.proveedor.id_proveedor;


--
-- TOC entry 234 (class 1259 OID 32905)
-- Name: rol; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.rol (
    id_rol integer NOT NULL,
    nombre character varying(50) NOT NULL,
    descripcion text,
    estado boolean DEFAULT true,
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.rol OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 32904)
-- Name: rol_id_rol_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.rol_id_rol_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.rol_id_rol_seq OWNER TO postgres;

--
-- TOC entry 5065 (class 0 OID 0)
-- Dependencies: 233
-- Name: rol_id_rol_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.rol_id_rol_seq OWNED BY public.rol.id_rol;


--
-- TOC entry 244 (class 1259 OID 32987)
-- Name: timbrado; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.timbrado (
    id_timbrado integer NOT NULL,
    numero_timbrado character varying(20) NOT NULL,
    fecha_inicio date NOT NULL,
    fecha_fin date NOT NULL,
    estado boolean DEFAULT true,
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.timbrado OWNER TO postgres;

--
-- TOC entry 243 (class 1259 OID 32986)
-- Name: timbrado_id_timbrado_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.timbrado_id_timbrado_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.timbrado_id_timbrado_seq OWNER TO postgres;

--
-- TOC entry 5066 (class 0 OID 0)
-- Dependencies: 243
-- Name: timbrado_id_timbrado_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.timbrado_id_timbrado_seq OWNED BY public.timbrado.id_timbrado;


--
-- TOC entry 236 (class 1259 OID 32918)
-- Name: usuario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuario (
    id_usuario integer NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(255) NOT NULL,
    nombre character varying(100) NOT NULL,
    apellido character varying(100) NOT NULL,
    email character varying(100),
    telefono character varying(20),
    estado boolean DEFAULT true,
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    id_rol integer NOT NULL
);


ALTER TABLE public.usuario OWNER TO postgres;

--
-- TOC entry 235 (class 1259 OID 32917)
-- Name: usuario_id_usuario_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.usuario_id_usuario_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.usuario_id_usuario_seq OWNER TO postgres;

--
-- TOC entry 5067 (class 0 OID 0)
-- Dependencies: 235
-- Name: usuario_id_usuario_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.usuario_id_usuario_seq OWNED BY public.usuario.id_usuario;


--
-- TOC entry 230 (class 1259 OID 32874)
-- Name: venta; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.venta (
    id_venta integer NOT NULL,
    id_cliente integer,
    fecha_venta timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    total numeric(12,2) NOT NULL,
    metodo_pago character varying(50) NOT NULL,
    estado character varying(20) DEFAULT 'PENDIENTE'::character varying
);


ALTER TABLE public.venta OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 32873)
-- Name: venta_id_venta_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.venta_id_venta_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.venta_id_venta_seq OWNER TO postgres;

--
-- TOC entry 5068 (class 0 OID 0)
-- Dependencies: 229
-- Name: venta_id_venta_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.venta_id_venta_seq OWNED BY public.venta.id_venta;


--
-- TOC entry 4754 (class 2604 OID 32941)
-- Name: caja id_caja; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.caja ALTER COLUMN id_caja SET DEFAULT nextval('public.caja_id_caja_seq'::regclass);


--
-- TOC entry 4757 (class 2604 OID 32952)
-- Name: caja_apertura_cierre id_apertura; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.caja_apertura_cierre ALTER COLUMN id_apertura SET DEFAULT nextval('public.caja_apertura_cierre_id_apertura_seq'::regclass);


--
-- TOC entry 4760 (class 2604 OID 32971)
-- Name: caja_movimiento id_movimiento; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.caja_movimiento ALTER COLUMN id_movimiento SET DEFAULT nextval('public.caja_movimiento_id_movimiento_seq'::regclass);


--
-- TOC entry 4731 (class 2604 OID 32792)
-- Name: categoria_producto id_categoria; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categoria_producto ALTER COLUMN id_categoria SET DEFAULT nextval('public.categoria_producto_id_categoria_seq'::regclass);


--
-- TOC entry 4741 (class 2604 OID 32864)
-- Name: cliente id_cliente; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cliente ALTER COLUMN id_cliente SET DEFAULT nextval('public.cliente_id_cliente_seq'::regclass);


--
-- TOC entry 4773 (class 2604 OID 33030)
-- Name: detalle_factura id_detalle; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_factura ALTER COLUMN id_detalle SET DEFAULT nextval('public.detalle_factura_id_detalle_seq'::regclass);


--
-- TOC entry 4747 (class 2604 OID 32891)
-- Name: detalle_venta id_detalle; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_venta ALTER COLUMN id_detalle SET DEFAULT nextval('public.detalle_venta_id_detalle_seq'::regclass);


--
-- TOC entry 4765 (class 2604 OID 32999)
-- Name: factura id_factura; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.factura ALTER COLUMN id_factura SET DEFAULT nextval('public.factura_id_factura_seq'::regclass);


--
-- TOC entry 4733 (class 2604 OID 32810)
-- Name: impuesto id_impuesto; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.impuesto ALTER COLUMN id_impuesto SET DEFAULT nextval('public.impuesto_id_impuesto_seq'::regclass);


--
-- TOC entry 4775 (class 2604 OID 33048)
-- Name: inventario id_inventario; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventario ALTER COLUMN id_inventario SET DEFAULT nextval('public.inventario_id_inventario_seq'::regclass);


--
-- TOC entry 4782 (class 2604 OID 33082)
-- Name: inventario_ajuste id_ajuste; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventario_ajuste ALTER COLUMN id_ajuste SET DEFAULT nextval('public.inventario_ajuste_id_ajuste_seq'::regclass);


--
-- TOC entry 4780 (class 2604 OID 33064)
-- Name: inventario_movimiento id_movimiento; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventario_movimiento ALTER COLUMN id_movimiento SET DEFAULT nextval('public.inventario_movimiento_id_movimiento_seq'::regclass);


--
-- TOC entry 4732 (class 2604 OID 32801)
-- Name: marca id_marca; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.marca ALTER COLUMN id_marca SET DEFAULT nextval('public.marca_id_marca_seq'::regclass);


--
-- TOC entry 4735 (class 2604 OID 32828)
-- Name: producto id_producto; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto ALTER COLUMN id_producto SET DEFAULT nextval('public.producto_id_producto_seq'::regclass);


--
-- TOC entry 4734 (class 2604 OID 32817)
-- Name: proveedor id_proveedor; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.proveedor ALTER COLUMN id_proveedor SET DEFAULT nextval('public.proveedor_id_proveedor_seq'::regclass);


--
-- TOC entry 4748 (class 2604 OID 32908)
-- Name: rol id_rol; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rol ALTER COLUMN id_rol SET DEFAULT nextval('public.rol_id_rol_seq'::regclass);


--
-- TOC entry 4762 (class 2604 OID 32990)
-- Name: timbrado id_timbrado; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.timbrado ALTER COLUMN id_timbrado SET DEFAULT nextval('public.timbrado_id_timbrado_seq'::regclass);


--
-- TOC entry 4751 (class 2604 OID 32921)
-- Name: usuario id_usuario; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario ALTER COLUMN id_usuario SET DEFAULT nextval('public.usuario_id_usuario_seq'::regclass);


--
-- TOC entry 4744 (class 2604 OID 32877)
-- Name: venta id_venta; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.venta ALTER COLUMN id_venta SET DEFAULT nextval('public.venta_id_venta_seq'::regclass);


--
-- TOC entry 5028 (class 0 OID 32938)
-- Dependencies: 238
-- Data for Name: caja; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.caja (id_caja, nombre, descripcion, estado, fecha_creacion) FROM stdin;
\.


--
-- TOC entry 5030 (class 0 OID 32949)
-- Dependencies: 240
-- Data for Name: caja_apertura_cierre; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.caja_apertura_cierre (id_apertura, id_caja, id_usuario, fecha_apertura, fecha_cierre, monto_inicial, monto_cierre, estado) FROM stdin;
\.


--
-- TOC entry 5032 (class 0 OID 32968)
-- Dependencies: 242
-- Data for Name: caja_movimiento; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.caja_movimiento (id_movimiento, id_apertura, tipo, concepto, monto, fecha_movimiento, id_usuario) FROM stdin;
\.


--
-- TOC entry 5008 (class 0 OID 32789)
-- Dependencies: 218
-- Data for Name: categoria_producto; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.categoria_producto (id_categoria, nombre) FROM stdin;
\.


--
-- TOC entry 5018 (class 0 OID 32861)
-- Dependencies: 228
-- Data for Name: cliente; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cliente (id_cliente, nombre, apellido, ruc_ci, direccion, telefono, email, fecha_alta, estado) FROM stdin;
\.


--
-- TOC entry 5038 (class 0 OID 33027)
-- Dependencies: 248
-- Data for Name: detalle_factura; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.detalle_factura (id_detalle, id_factura, id_producto, cantidad, precio_unitario, subtotal, iva_aplicado) FROM stdin;
\.


--
-- TOC entry 5022 (class 0 OID 32888)
-- Dependencies: 232
-- Data for Name: detalle_venta; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.detalle_venta (id_detalle, id_venta, id_producto, cantidad, precio_unitario, subtotal) FROM stdin;
\.


--
-- TOC entry 5036 (class 0 OID 32996)
-- Dependencies: 246
-- Data for Name: factura; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.factura (id_factura, id_venta, id_cliente, id_timbrado, numero_factura, tipo, fecha_emision, total_exento, total_gravado5, total_gravado10, total_iva5, total_iva10, total_general, estado) FROM stdin;
\.


--
-- TOC entry 5012 (class 0 OID 32807)
-- Dependencies: 222
-- Data for Name: impuesto; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.impuesto (id_impuesto, nombre, porcentaje) FROM stdin;
\.


--
-- TOC entry 5040 (class 0 OID 33045)
-- Dependencies: 250
-- Data for Name: inventario; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.inventario (id_inventario, id_producto, stock_actual, stock_reservado, fecha_actualizacion) FROM stdin;
\.


--
-- TOC entry 5044 (class 0 OID 33079)
-- Dependencies: 254
-- Data for Name: inventario_ajuste; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.inventario_ajuste (id_ajuste, id_producto, stock_sistema, stock_fisico, diferencia, motivo, fecha_ajuste, id_usuario) FROM stdin;
\.


--
-- TOC entry 5042 (class 0 OID 33061)
-- Dependencies: 252
-- Data for Name: inventario_movimiento; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.inventario_movimiento (id_movimiento, id_producto, tipo, cantidad, motivo, referencia, fecha_movimiento, id_usuario) FROM stdin;
\.


--
-- TOC entry 5010 (class 0 OID 32798)
-- Dependencies: 220
-- Data for Name: marca; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.marca (id_marca, nombre) FROM stdin;
\.


--
-- TOC entry 5016 (class 0 OID 32825)
-- Dependencies: 226
-- Data for Name: producto; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.producto (id_producto, codigo_barra, codigo_interno, nombre, descripcion, id_categoria, id_marca, id_impuesto, precio_costo, precio_venta, precio_mayorista, descuento_maximo, stock_minimo, unidad_medida, peso_neto, volumen, estado, es_perecedero, fecha_vencimiento, fecha_creacion, fecha_actualizacion, imagen_url, proveedor_principal, ubicacion_gondola) FROM stdin;
\.


--
-- TOC entry 5014 (class 0 OID 32814)
-- Dependencies: 224
-- Data for Name: proveedor; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.proveedor (id_proveedor, nombre, ruc, telefono, email, direccion) FROM stdin;
\.


--
-- TOC entry 5024 (class 0 OID 32905)
-- Dependencies: 234
-- Data for Name: rol; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.rol (id_rol, nombre, descripcion, estado, fecha_creacion) FROM stdin;
\.


--
-- TOC entry 5034 (class 0 OID 32987)
-- Dependencies: 244
-- Data for Name: timbrado; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.timbrado (id_timbrado, numero_timbrado, fecha_inicio, fecha_fin, estado, fecha_creacion) FROM stdin;
\.


--
-- TOC entry 5026 (class 0 OID 32918)
-- Dependencies: 236
-- Data for Name: usuario; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.usuario (id_usuario, username, password, nombre, apellido, email, telefono, estado, fecha_creacion, id_rol) FROM stdin;
\.


--
-- TOC entry 5020 (class 0 OID 32874)
-- Dependencies: 230
-- Data for Name: venta; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.venta (id_venta, id_cliente, fecha_venta, total, metodo_pago, estado) FROM stdin;
\.


--
-- TOC entry 5069 (class 0 OID 0)
-- Dependencies: 239
-- Name: caja_apertura_cierre_id_apertura_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.caja_apertura_cierre_id_apertura_seq', 1, false);


--
-- TOC entry 5070 (class 0 OID 0)
-- Dependencies: 237
-- Name: caja_id_caja_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.caja_id_caja_seq', 1, false);


--
-- TOC entry 5071 (class 0 OID 0)
-- Dependencies: 241
-- Name: caja_movimiento_id_movimiento_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.caja_movimiento_id_movimiento_seq', 1, false);


--
-- TOC entry 5072 (class 0 OID 0)
-- Dependencies: 217
-- Name: categoria_producto_id_categoria_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.categoria_producto_id_categoria_seq', 1, false);


--
-- TOC entry 5073 (class 0 OID 0)
-- Dependencies: 227
-- Name: cliente_id_cliente_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.cliente_id_cliente_seq', 1, false);


--
-- TOC entry 5074 (class 0 OID 0)
-- Dependencies: 247
-- Name: detalle_factura_id_detalle_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.detalle_factura_id_detalle_seq', 1, false);


--
-- TOC entry 5075 (class 0 OID 0)
-- Dependencies: 231
-- Name: detalle_venta_id_detalle_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.detalle_venta_id_detalle_seq', 1, false);


--
-- TOC entry 5076 (class 0 OID 0)
-- Dependencies: 245
-- Name: factura_id_factura_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.factura_id_factura_seq', 1, false);


--
-- TOC entry 5077 (class 0 OID 0)
-- Dependencies: 221
-- Name: impuesto_id_impuesto_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.impuesto_id_impuesto_seq', 1, false);


--
-- TOC entry 5078 (class 0 OID 0)
-- Dependencies: 253
-- Name: inventario_ajuste_id_ajuste_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.inventario_ajuste_id_ajuste_seq', 1, false);


--
-- TOC entry 5079 (class 0 OID 0)
-- Dependencies: 249
-- Name: inventario_id_inventario_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.inventario_id_inventario_seq', 1, false);


--
-- TOC entry 5080 (class 0 OID 0)
-- Dependencies: 251
-- Name: inventario_movimiento_id_movimiento_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.inventario_movimiento_id_movimiento_seq', 1, false);


--
-- TOC entry 5081 (class 0 OID 0)
-- Dependencies: 219
-- Name: marca_id_marca_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.marca_id_marca_seq', 1, false);


--
-- TOC entry 5082 (class 0 OID 0)
-- Dependencies: 225
-- Name: producto_id_producto_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.producto_id_producto_seq', 1, false);


--
-- TOC entry 5083 (class 0 OID 0)
-- Dependencies: 223
-- Name: proveedor_id_proveedor_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.proveedor_id_proveedor_seq', 1, false);


--
-- TOC entry 5084 (class 0 OID 0)
-- Dependencies: 233
-- Name: rol_id_rol_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.rol_id_rol_seq', 1, false);


--
-- TOC entry 5085 (class 0 OID 0)
-- Dependencies: 243
-- Name: timbrado_id_timbrado_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.timbrado_id_timbrado_seq', 1, false);


--
-- TOC entry 5086 (class 0 OID 0)
-- Dependencies: 235
-- Name: usuario_id_usuario_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.usuario_id_usuario_seq', 1, false);


--
-- TOC entry 5087 (class 0 OID 0)
-- Dependencies: 229
-- Name: venta_id_venta_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.venta_id_venta_seq', 1, false);


--
-- TOC entry 4823 (class 2606 OID 32956)
-- Name: caja_apertura_cierre caja_apertura_cierre_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.caja_apertura_cierre
    ADD CONSTRAINT caja_apertura_cierre_pkey PRIMARY KEY (id_apertura);


--
-- TOC entry 4825 (class 2606 OID 32974)
-- Name: caja_movimiento caja_movimiento_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.caja_movimiento
    ADD CONSTRAINT caja_movimiento_pkey PRIMARY KEY (id_movimiento);


--
-- TOC entry 4821 (class 2606 OID 32947)
-- Name: caja caja_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.caja
    ADD CONSTRAINT caja_pkey PRIMARY KEY (id_caja);


--
-- TOC entry 4785 (class 2606 OID 32796)
-- Name: categoria_producto categoria_producto_nombre_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categoria_producto
    ADD CONSTRAINT categoria_producto_nombre_key UNIQUE (nombre);


--
-- TOC entry 4787 (class 2606 OID 32794)
-- Name: categoria_producto categoria_producto_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categoria_producto
    ADD CONSTRAINT categoria_producto_pkey PRIMARY KEY (id_categoria);


--
-- TOC entry 4803 (class 2606 OID 32870)
-- Name: cliente cliente_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cliente
    ADD CONSTRAINT cliente_pkey PRIMARY KEY (id_cliente);


--
-- TOC entry 4805 (class 2606 OID 32872)
-- Name: cliente cliente_ruc_ci_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cliente
    ADD CONSTRAINT cliente_ruc_ci_key UNIQUE (ruc_ci);


--
-- TOC entry 4833 (class 2606 OID 33033)
-- Name: detalle_factura detalle_factura_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_factura
    ADD CONSTRAINT detalle_factura_pkey PRIMARY KEY (id_detalle);


--
-- TOC entry 4809 (class 2606 OID 32893)
-- Name: detalle_venta detalle_venta_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_venta
    ADD CONSTRAINT detalle_venta_pkey PRIMARY KEY (id_detalle);


--
-- TOC entry 4829 (class 2606 OID 33010)
-- Name: factura factura_numero_factura_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.factura
    ADD CONSTRAINT factura_numero_factura_key UNIQUE (numero_factura);


--
-- TOC entry 4831 (class 2606 OID 33008)
-- Name: factura factura_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.factura
    ADD CONSTRAINT factura_pkey PRIMARY KEY (id_factura);


--
-- TOC entry 4793 (class 2606 OID 32812)
-- Name: impuesto impuesto_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.impuesto
    ADD CONSTRAINT impuesto_pkey PRIMARY KEY (id_impuesto);


--
-- TOC entry 4839 (class 2606 OID 33085)
-- Name: inventario_ajuste inventario_ajuste_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventario_ajuste
    ADD CONSTRAINT inventario_ajuste_pkey PRIMARY KEY (id_ajuste);


--
-- TOC entry 4837 (class 2606 OID 33067)
-- Name: inventario_movimiento inventario_movimiento_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventario_movimiento
    ADD CONSTRAINT inventario_movimiento_pkey PRIMARY KEY (id_movimiento);


--
-- TOC entry 4835 (class 2606 OID 33054)
-- Name: inventario inventario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventario
    ADD CONSTRAINT inventario_pkey PRIMARY KEY (id_inventario);


--
-- TOC entry 4789 (class 2606 OID 32805)
-- Name: marca marca_nombre_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.marca
    ADD CONSTRAINT marca_nombre_key UNIQUE (nombre);


--
-- TOC entry 4791 (class 2606 OID 32803)
-- Name: marca marca_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.marca
    ADD CONSTRAINT marca_pkey PRIMARY KEY (id_marca);


--
-- TOC entry 4799 (class 2606 OID 32839)
-- Name: producto producto_codigo_barra_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT producto_codigo_barra_key UNIQUE (codigo_barra);


--
-- TOC entry 4801 (class 2606 OID 32837)
-- Name: producto producto_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT producto_pkey PRIMARY KEY (id_producto);


--
-- TOC entry 4795 (class 2606 OID 32821)
-- Name: proveedor proveedor_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.proveedor
    ADD CONSTRAINT proveedor_pkey PRIMARY KEY (id_proveedor);


--
-- TOC entry 4797 (class 2606 OID 32823)
-- Name: proveedor proveedor_ruc_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.proveedor
    ADD CONSTRAINT proveedor_ruc_key UNIQUE (ruc);


--
-- TOC entry 4811 (class 2606 OID 32916)
-- Name: rol rol_nombre_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rol
    ADD CONSTRAINT rol_nombre_key UNIQUE (nombre);


--
-- TOC entry 4813 (class 2606 OID 32914)
-- Name: rol rol_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rol
    ADD CONSTRAINT rol_pkey PRIMARY KEY (id_rol);


--
-- TOC entry 4827 (class 2606 OID 32994)
-- Name: timbrado timbrado_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.timbrado
    ADD CONSTRAINT timbrado_pkey PRIMARY KEY (id_timbrado);


--
-- TOC entry 4815 (class 2606 OID 32931)
-- Name: usuario usuario_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_email_key UNIQUE (email);


--
-- TOC entry 4817 (class 2606 OID 32927)
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id_usuario);


--
-- TOC entry 4819 (class 2606 OID 32929)
-- Name: usuario usuario_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_username_key UNIQUE (username);


--
-- TOC entry 4807 (class 2606 OID 32881)
-- Name: venta venta_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.venta
    ADD CONSTRAINT venta_pkey PRIMARY KEY (id_venta);


--
-- TOC entry 4850 (class 2606 OID 32975)
-- Name: caja_movimiento fk_apertura; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.caja_movimiento
    ADD CONSTRAINT fk_apertura FOREIGN KEY (id_apertura) REFERENCES public.caja_apertura_cierre(id_apertura);


--
-- TOC entry 4848 (class 2606 OID 32957)
-- Name: caja_apertura_cierre fk_caja; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.caja_apertura_cierre
    ADD CONSTRAINT fk_caja FOREIGN KEY (id_caja) REFERENCES public.caja(id_caja);


--
-- TOC entry 4840 (class 2606 OID 32840)
-- Name: producto fk_categoria; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT fk_categoria FOREIGN KEY (id_categoria) REFERENCES public.categoria_producto(id_categoria);


--
-- TOC entry 4844 (class 2606 OID 32882)
-- Name: venta fk_cliente; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.venta
    ADD CONSTRAINT fk_cliente FOREIGN KEY (id_cliente) REFERENCES public.cliente(id_cliente);


--
-- TOC entry 4852 (class 2606 OID 33016)
-- Name: factura fk_cliente_factura; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.factura
    ADD CONSTRAINT fk_cliente_factura FOREIGN KEY (id_cliente) REFERENCES public.cliente(id_cliente);


--
-- TOC entry 4855 (class 2606 OID 33034)
-- Name: detalle_factura fk_factura; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_factura
    ADD CONSTRAINT fk_factura FOREIGN KEY (id_factura) REFERENCES public.factura(id_factura);


--
-- TOC entry 4841 (class 2606 OID 32850)
-- Name: producto fk_impuesto; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT fk_impuesto FOREIGN KEY (id_impuesto) REFERENCES public.impuesto(id_impuesto);


--
-- TOC entry 4842 (class 2606 OID 32845)
-- Name: producto fk_marca; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT fk_marca FOREIGN KEY (id_marca) REFERENCES public.marca(id_marca);


--
-- TOC entry 4845 (class 2606 OID 32899)
-- Name: detalle_venta fk_producto; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_venta
    ADD CONSTRAINT fk_producto FOREIGN KEY (id_producto) REFERENCES public.producto(id_producto);


--
-- TOC entry 4860 (class 2606 OID 33086)
-- Name: inventario_ajuste fk_producto_ajuste; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventario_ajuste
    ADD CONSTRAINT fk_producto_ajuste FOREIGN KEY (id_producto) REFERENCES public.producto(id_producto);


--
-- TOC entry 4856 (class 2606 OID 33039)
-- Name: detalle_factura fk_producto_factura; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_factura
    ADD CONSTRAINT fk_producto_factura FOREIGN KEY (id_producto) REFERENCES public.producto(id_producto);


--
-- TOC entry 4857 (class 2606 OID 33055)
-- Name: inventario fk_producto_inventario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventario
    ADD CONSTRAINT fk_producto_inventario FOREIGN KEY (id_producto) REFERENCES public.producto(id_producto);


--
-- TOC entry 4858 (class 2606 OID 33068)
-- Name: inventario_movimiento fk_producto_mov; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventario_movimiento
    ADD CONSTRAINT fk_producto_mov FOREIGN KEY (id_producto) REFERENCES public.producto(id_producto);


--
-- TOC entry 4843 (class 2606 OID 32855)
-- Name: producto fk_proveedor; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT fk_proveedor FOREIGN KEY (proveedor_principal) REFERENCES public.proveedor(id_proveedor);


--
-- TOC entry 4847 (class 2606 OID 32932)
-- Name: usuario fk_rol; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT fk_rol FOREIGN KEY (id_rol) REFERENCES public.rol(id_rol);


--
-- TOC entry 4853 (class 2606 OID 33021)
-- Name: factura fk_timbrado; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.factura
    ADD CONSTRAINT fk_timbrado FOREIGN KEY (id_timbrado) REFERENCES public.timbrado(id_timbrado);


--
-- TOC entry 4849 (class 2606 OID 32962)
-- Name: caja_apertura_cierre fk_usuario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.caja_apertura_cierre
    ADD CONSTRAINT fk_usuario FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario);


--
-- TOC entry 4861 (class 2606 OID 33091)
-- Name: inventario_ajuste fk_usuario_ajuste; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventario_ajuste
    ADD CONSTRAINT fk_usuario_ajuste FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario);


--
-- TOC entry 4851 (class 2606 OID 32980)
-- Name: caja_movimiento fk_usuario_mov; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.caja_movimiento
    ADD CONSTRAINT fk_usuario_mov FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario);


--
-- TOC entry 4859 (class 2606 OID 33073)
-- Name: inventario_movimiento fk_usuario_mov_inv; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventario_movimiento
    ADD CONSTRAINT fk_usuario_mov_inv FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario);


--
-- TOC entry 4846 (class 2606 OID 32894)
-- Name: detalle_venta fk_venta; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_venta
    ADD CONSTRAINT fk_venta FOREIGN KEY (id_venta) REFERENCES public.venta(id_venta);


--
-- TOC entry 4854 (class 2606 OID 33011)
-- Name: factura fk_venta_factura; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.factura
    ADD CONSTRAINT fk_venta_factura FOREIGN KEY (id_venta) REFERENCES public.venta(id_venta);


-- Completed on 2025-10-18 10:24:25 -03

--
-- PostgreSQL database dump complete
--

\unrestrict q8Lf2zlCC0R3Tip8fx50q8g1Q0vmnfdMs4eLzmvKBP8JAuu6jERS7zowPCXlqPZ

