# Heladería API REST

Backend especializado para la gestión y punto de venta de una **Heladería**, desarrollado con **Java 21**, **Spring Boot 3** y **PostgreSQL**.

---

## Características Principales
* **Gestión de Sabores**: Clasificación por categorías (`CREMA`, `FRUTAL`, `CHOCOLATE`, `DULCE_DE_LECHE`, `ESPECIAL`), atributos dietéticos (apto celíacos, veganos, sin azúcar) y control rápido de disponibilidad en mostrador.
* **Formatos y Presentaciones**: Configuración de envases (cucuruchos, potes de 1/4, 1/2 y 1 kg) con reglas de negocio para límites de sabores (`maxSabores`).
* **Toppings y Agregados**: Salsas y adicionales con cálculo dinámico de costo.
* **Ventas y Pedidos**: Validación estricta de armado de helados, control de stock en productos simples/bebidas y ciclo de vida del pedido.
* **Documentación OpenAPI / Swagger**: Interfaz interactiva para probar endpoints.
* **Contenerizado con Docker Compose**: Despliegue con un solo comando junto a PostgreSQL con persistencia en volumen.

---

## 🚀 Guía de Despliegue e Instalación

Para instrucciones detalladas paso a paso sobre cómo levantar el proyecto en **Linux** o **Windows**, consulta:
👉 **[INSTRUCCIONES.md](INSTRUCCIONES.md)**

### Inicio rápido con Docker:
```bash
docker compose up -d --build
```
* **Swagger UI**: [http://localhost:8085/swagger-ui.html](http://localhost:8085/swagger-ui.html)
* **PostgreSQL**: `localhost:5435` (Base de datos: `heladeriadb`, Usuario: `postgres`, Contraseña: `postgres`)
