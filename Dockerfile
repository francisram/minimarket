# Etapa 1: Compilación con Maven y Java 21
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

# Compilar código y generar JAR ejecutable saltando tests para agilizar el build
RUN mvn clean package -DskipTests

# Etapa 2: Imagen liviana de ejecución con JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Instalar cliente CUPS para habilitar el binario 'lp'
RUN apk add --no-cache cups-client

# Crear usuario sin privilegios por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiar el artefacto compilado
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto interno de la API
EXPOSE 8080

# Comando de arranque con codificación UTF-8 explícita
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
