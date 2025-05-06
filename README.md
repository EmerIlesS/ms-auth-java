# Microservicio de Autenticación (ms-auth-java)

Este microservicio forma parte de una arquitectura de microservicios y proporciona funcionalidades de autenticación y gestión de usuarios utilizando Spring Boot, JWT y MongoDB.

## Características

- Registro de usuarios
- Inicio de sesión con autenticación JWT
- Gestión de roles (CUSTOMER, SELLER, ADMIN)
- Gestión de productos favoritos
- API GraphQL

## Requisitos previos

- Java 21
- Maven 3.8+
- MongoDB
- Docker (opcional)

## Configuración

### Variables de entorno

Crea un archivo `.env` en la raíz del proyecto basado en `.env.example`:

```
SERVER_PORT=4001
SPRING_DATA_MONGODB_URI=mongodb+srv://username:password@host/database
SPRING_DATA_MONGODB_DATABASE=auth-db
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
```

## Ejecución local

```bash
# Compilar el proyecto
mvn clean package

# Ejecutar la aplicación
mvn spring-boot:run
```

## Ejecución con Docker

```bash
# Construir la imagen
docker build -t ms-auth-java:latest .

# Ejecutar el contenedor
docker run -p 4001:4001 --env-file .env ms-auth-java:latest
```

## Ejecución con Docker Compose

```bash
# Levantar todos los servicios
docker-compose up -d
```

## Endpoints principales

### REST API

- POST `/api/auth/register` - Registro de usuarios
- POST `/api/auth/login` - Inicio de sesión
- POST `/api/auth/favorites/{id}` - Añadir producto a favoritos
- DELETE `/api/auth/favorites/{id}` - Eliminar producto de favoritos
- GET `/api/auth/favorites` - Listar productos favoritos

### GraphQL

La API GraphQL está disponible en `/graphql` y GraphiQL (interfaz de consulta) en `/graphiql`.

## Integración con API Gateway

Este microservicio está diseñado para funcionar con un API Gateway, que enruta las solicitudes apropiadas a este servicio en función de las rutas configuradas.

## Roles de Usuario

- **CUSTOMER**: Usuarios que pueden navegar y comprar productos
- **SELLER**: Usuarios que pueden gestionar sus propios productos
- **ADMIN**: Usuarios con acceso completo a todas las funcionalidades

## Estructura del proyecto

```
ms-auth-java/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── auth/
│   │   │           ├── controller/
│   │   │           ├── model/
│   │   │           ├── repository/
│   │   │           ├── service/
│   │   │           ├── security/
│   │   │           └── config/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── graphql/
│   └── test/
├── .env.example
├── .gitignore
├── docker-compose.yaml
├── Dockerfile
├── pom.xml
└── README.md
```
