# Guía de Instalación y Despliegue Paso a Paso (Linux y Windows)

Esta guía explica cómo clonar, configurar y levantar la **API de Heladería** en cualquier equipo con **Linux** o **Windows**, utilizando Docker (método recomendado y automático) o ejecución local directa.

---

## Índice
1. [Requisitos Previos](#1-requisitos-previos)
2. [Despliegue con Docker (Método Recomendado)](#2-despliegue-con-docker-método-recomendado)
   - [En Linux (Ubuntu, Debian, Fedora, Arch)](#21-en-linux)
   - [En Windows (PowerShell / CMD / WSL)](#22-en-windows)
3. [Verificación del Funcionamiento](#3-verificación-del-funcionamiento)
4. [Despliegue Local sin Docker (Alternativa con Maven)](#4-despliegue-local-sin-docker-alternativa)
5. [Comandos Frecuentes de Gestión](#5-comandos-frecuentes-de-gestión)
6. [Resolución de Problemas Frecuentes](#6-resolución-de-problemas-frecuentes)

---

## 1. Requisitos Previos

### Para desplegar con Docker (Recomendado):
* **Linux**: [Docker Engine](https://docs.docker.com/engine/install/) y [Docker Compose Plugin](https://docs.docker.com/compose/install/).
* **Windows**: [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado y en ejecución (asegúrate de que tenga activado el soporte de WSL 2).
* **Git** instalado en ambos sistemas operativos.

### Para desplegar sin Docker (Modo desarrollador):
* **Java**: OpenJDK 21 o superior instalado.
* **Maven**: Versión 3.8 o superior.
* *(Opcional)* PostgreSQL 15+ si no deseas usar la base de datos H2 en memoria integrada.

---

## 2. Despliegue con Docker (Método Recomendado)

Con este método **NO necesitas instalar Java, Maven ni PostgreSQL** en el sistema anfitrión. Docker se encarga de compilar, configurar la base de datos y levantar la API automáticamente.

### 2.1 En Linux

1. **Abrir una terminal** (`bash`, `zsh`, etc.).
2. **Clonar el repositorio y ubicarse en la rama `heladeria`**:
   ```bash
   git clone https://github.com/francisram/minimarket.git
   cd minimarket
   git checkout heladeria
   ```
3. **Levantar los servicios con Docker Compose**:
   ```bash
   docker compose up -d --build
   ```
4. **Verificar que los contenedores estén corriendo**:
   ```bash
   docker compose ps
   ```
   Deberías ver `heladeria-api` y `heladeria-postgres` en estado `Up`.

---

### 2.2 En Windows

1. **Iniciar Docker Desktop** y esperar a que el ícono en la barra de tareas indique que el motor de Docker está activo (*Engine running*).
2. **Abrir PowerShell** o **Símbolo del sistema (CMD)** como usuario normal.
3. **Clonar el repositorio y entrar al proyecto**:
   ```powershell
   git clone https://github.com/francisram/minimarket.git
   cd minimarket
   git checkout heladeria
   ```
4. **Levantar los contenedores**:
   ```powershell
   docker compose up -d --build
   ```
5. **Comprobar el estado**:
   ```powershell
   docker compose ps
   ```

---

## 3. Verificación del Funcionamiento

Una vez que los contenedores estén arriba, puedes abrir tu navegador en:

| Recurso | URL | Descripción |
| :--- | :--- | :--- |
| **Swagger UI (Documentación interactiva)** | [http://localhost:8085/swagger-ui.html](http://localhost:8085/swagger-ui.html) | Prueba todos los endpoints directamente desde la web |
| **Listado de Sabores** | [http://localhost:8085/api/sabores](http://localhost:8085/api/sabores) | Consulta el catálogo precargado en JSON |
| **Formatos de Helado** | [http://localhost:8085/api/presentaciones](http://localhost:8085/api/presentaciones) | Potes de 1/4, 1/2, 1 kg, cucuruchos y límites |
| **OpenAPI Docs (JSON)** | [http://localhost:8085/api-docs](http://localhost:8085/api-docs) | Esquema de OpenAPI |

### Conexión a la Base de Datos PostgreSQL desde DBeaver / pgAdmin:
* **Host**: `localhost`
* **Puerto**: `5435`
* **Base de datos**: `heladeriadb`
* **Usuario**: `postgres`
* **Contraseña**: `postgres`

> [!NOTE]
> La base de datos, todas las tablas (`sabores`, `presentaciones`, `pedidos`, etc.) y el catálogo inicial de prueba se crean **automáticamente** en el primer arranque.

---

## 4. Despliegue Local sin Docker (Alternativa)

Si en el equipo destino prefieres ejecutar la aplicación directamente en Java sin contenedores:

1. **Clonar y entrar al repositorio**:
   ```bash
   git clone https://github.com/francisram/minimarket.git
   cd minimarket
   git checkout heladeria
   ```
2. **Ejecutar con Maven**:
   * En **Linux**:
     ```bash
     mvn spring-boot:run
     ```
   * En **Windows** (PowerShell):
     ```powershell
     mvn spring-boot:run
     ```
3. La aplicación arrancará usando **H2 Database en memoria**:
   * **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   * **Consola H2**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:heladeriadb`, Usuario: `sa`, Contraseña: vacía).

---

## 5. Comandos Frecuentes de Gestión

Ejecuta estos comandos situándote dentro de la carpeta del proyecto:

```bash
# Ver los logs en tiempo real de la API
docker compose logs -f app

# Ver los logs de la base de datos PostgreSQL
docker compose logs -f db

# Detener los contenedores (los datos se conservan intactos en el volumen)
docker compose stop

# Volver a encender los contenedores detenidos
docker compose start

# Detener y remover los contenedores (los datos del volumen se mantienen seguros)
docker compose down

# Reiniciar la aplicación tras hacer cambios en el código
docker compose up -d --build
```

---

## 6. Resolución de Problemas Frecuentes

### Problema: "port is already allocated" (Puerto ya ocupado)
Si en el nuevo equipo los puertos `8085` o `5435` ya están en uso por otro programa:
1. Abre el archivo `docker-compose.yml`.
2. Modifica la sección de `ports` cambiando el número de la izquierda (el puerto del equipo):
   ```yaml
   ports:
     - "8090:8080"   # Cambia 8085 por el puerto que tengas libre (ej: 8090)
   ```
   Y para la base de datos:
   ```yaml
   ports:
     - "5436:5432"   # Cambia 5435 por otro puerto libre (ej: 5436)
   ```
3. Vuelve a ejecutar:
   ```bash
   docker compose up -d
   ```

### Problema en Linux: "permission denied while trying to connect to the Docker daemon socket"
Tu usuario de Linux no tiene permisos para usar Docker sin `sudo`.
* Solución rápida: ejecuta con `sudo docker compose up -d --build`.
* Solución permanente: agrega tu usuario al grupo `docker`:
  ```bash
  sudo usermod -aG docker $USER
  newgrp docker
  ```

### Problema en Windows: Docker no arranca o da error de WSL
* Abre Docker Desktop y ve a **Settings > General > Use the WSL 2 based engine** (asegúrate de que esté tildado).
* Abre PowerShell como Administrador y ejecuta: `wsl --update`.
