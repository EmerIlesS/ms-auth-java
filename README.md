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

Este microservicio está diseñado para funcionar con un API Gateway, que enruta las solicitudes apropiadas a este servicio en función de las rutas configuradas. La integración con la API Gateway incluye los siguientes aspectos:

### Federación de Esquemas GraphQL

- El esquema GraphQL de este microservicio se federa con otros esquemas a través de la API Gateway.
- La API Gateway combina los esquemas de todos los microservicios para proporcionar un único punto de entrada para las consultas GraphQL.
- Los clientes pueden realizar consultas que abarcan múltiples microservicios a través de la API Gateway.

### Flujo de Autenticación

1. El cliente envía credenciales de inicio de sesión a la API Gateway.
2. La API Gateway redirige la solicitud a este microservicio.
3. Este microservicio valida las credenciales y genera un token JWT.
4. El token JWT se devuelve al cliente a través de la API Gateway.
5. Para solicitudes posteriores, el cliente incluye el token JWT en el encabezado `Authorization`.
6. La API Gateway valida inicialmente el token y lo pasa a los microservicios correspondientes.

### Compartición de Tokens JWT

- Los tokens JWT generados por este microservicio son utilizados por todos los demás microservicios.
- La API Gateway pasa el token JWT en el encabezado de las solicitudes a los microservicios.
- Cada microservicio valida el token JWT utilizando la misma clave secreta configurada en las variables de entorno.

## Integración con ms-products-orders

Este microservicio se integra con el microservicio de productos y órdenes (ms-products-orders) para proporcionar autenticación y autorización. A continuación se detalla cómo funciona esta integración:

### Validación de Tokens JWT

- El microservicio ms-auth-java genera tokens JWT cuando los usuarios inician sesión.
- El microservicio ms-products-orders recibe estos tokens a través de la API Gateway en el encabezado `Authorization`.
- ms-products-orders valida el token utilizando la misma clave secreta configurada en ambos microservicios.
- Si el token es válido, ms-products-orders extrae la información del usuario y sus roles para autorizar las operaciones.

### Control de Acceso Basado en Roles

- Los roles de usuario (CUSTOMER, SELLER, ADMIN) definidos en ms-auth-java determinan los permisos en ms-products-orders:
  - **CUSTOMER**: Puede ver productos y categorías, y crear órdenes.
  - **SELLER**: Puede gestionar sus propios productos.
  - **ADMIN**: Tiene acceso completo a todas las funcionalidades.

### Gestión de Productos Favoritos

- ms-auth-java mantiene una lista de productos favoritos para cada usuario.
- Cuando un usuario marca un producto como favorito en ms-products-orders, se envía una solicitud a ms-auth-java para actualizar la lista de favoritos.
- ms-auth-java proporciona endpoints para añadir, eliminar y listar productos favoritos.

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
