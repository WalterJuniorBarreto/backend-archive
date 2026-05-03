# --- ETAPA 1: BUILD (Compilación) ---
# Usamos una imagen con Maven para compilar el proyecto
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos solo el archivo de dependencias primero (para aprovechar la caché de Docker)
COPY pom.xml .

# Descargamos las dependencias (esto se guarda en caché si no cambias el pom.xml)
RUN mvn dependency:go-offline

# Ahora copiamos todo el código fuente
COPY src ./src

# Compilamos el proyecto y creamos el .jar (saltando los tests para ir rápido en el build de docker)
RUN mvn clean package -DskipTests

# --- ETAPA 2: RUNTIME (Ejecución) ---
# Usamos una imagen muy ligera solo con Java (sin Maven)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiamos solo el archivo .jar compilado en la etapa anterior
# Asegúrate de que el nombre del .jar coincida con el que genera tu pom.xml
COPY --from=builder /app/target/*.jar app.jar

# Exponemos el puerto (por defecto Spring Boot usa 8080)
EXPOSE 8080

# Comando para arrancar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]