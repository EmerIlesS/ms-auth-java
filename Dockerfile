# Etapa 1: Construir la aplicación
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia el archivo POM y descarga las dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Construir la aplicación
RUN mvn package -DskipTests

# Etapa 2: Crear la imagen final
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el archivo JAR desde la etapa de compilación
COPY --from=build /app/target/*.jar app.jar

# Variables de entorno (configurar en el entorno o en un archivo .env)
ENV SERVER_PORT=4001

# Exponer el puerto
EXPOSE 4001

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia el archivo POM y descarga las dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Construir la aplicación
RUN mvn package -DskipTests

# Etapa 2: Crear la imagen final
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el archivo JAR desde la etapa de compilación
COPY --from=build /app/target/*.jar app.jar

# Copiar el archivo .env: asegúrese de que exista durante la compilación
COPY .env .

# Exponer el puerto
EXPOSE 4001

#Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia el archivo POM y descarga las dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Construir la aplicación
RUN mvn package -DskipTests

# Etapa 2: Crear la imagen final
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el archivo JAR desde la etapa de compilación
COPY --from=build /app/target/*.jar app.jar

# Variables de entorno
ENV SPRING_DATA_MONGODB_URI=mongodb+srv://ilesemerson5:mCim1EmN14b4ZgCk@cluster0.f3tgtcm.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0&ssl=true&tlsAllowInvalidCertificates=true
ENV SPRING_DATA_MONGODB_DATABASE=auth-db
ENV SERVER_PORT=4001

# Exponer el puerto
EXPOSE 4001

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
