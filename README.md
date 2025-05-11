# Microservicio de Autenticación (ms-auth-java)

Este microservicio forma parte de una arquitectura de microservicios y proporciona funcionalidades de autenticación y gestión de usuarios utilizando Spring Boot, JWT y MongoDB.

## Características Implementadas

- **Registro de usuarios**: Soporte para registro de diferentes tipos de usuarios (CUSTOMER, SELLER, ADMIN) con validación de datos
- **Inicio de sesión**: Autenticación segura con generación de tokens JWT
- **Gestión de perfiles**: Consulta de información de perfil de usuario autenticado
- **Gestión de roles**: Implementación de tres roles distintos con diferentes niveles de acceso
- **Gestión de productos favoritos**: Funcionalidad para añadir, eliminar y listar productos favoritos
- **API GraphQL**: Implementación completa con queries y mutations para todas las funcionalidades
- **Integración con otros microservicios**: Comunicación con ms-products-orders para validación de productos

## Tecnologías Utilizadas

- **Spring Boot 3.2.2**: Framework principal para el desarrollo del microservicio
- **Spring Security**: Implementación de seguridad y autenticación
- **JWT (JSON Web Tokens)**: Para la generación y validación de tokens de autenticación
- **MongoDB**: Base de datos NoSQL para almacenamiento de usuarios y sus datos
- **GraphQL**: API declarativa para consultas y mutaciones
- **WebFlux**: Para comunicación reactiva con otros microservicios
- **Lombok**: Reducción de código boilerplate
- **Spring Dotenv**: Gestión de variables de entorno

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
PRODUCTS_SERVICE_URL=http://localhost:4002
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

## Endpoints implementados

### REST API

- **POST `/api/auth/register`**: Registro de usuarios con rol CUSTOMER
- **POST `/api/auth/register/vendor`**: Registro de usuarios con rol SELLER
- **POST `/api/auth/register/admin`**: Registro de usuarios con rol ADMIN
- **POST `/api/auth/login`**: Inicio de sesión y generación de token JWT
- **GET `/api/auth/me`**: Obtener información del perfil del usuario autenticado
- **POST `/api/auth/favorites/{id}`**: Añadir producto a favoritos
- **DELETE `/api/auth/favorites/{id}`**: Eliminar producto de favoritos
- **GET `/api/auth/favorites`**: Listar productos favoritos del usuario

### GraphQL

La API GraphQL está disponible en `/graphql` y GraphiQL (interfaz de consulta) en `/graphiql`.

#### Queries

- **me**: Obtener información del perfil del usuario autenticado

#### Mutations

- **register**: Registro de usuarios con rol CUSTOMER
- **registerVendor**: Registro de usuarios con rol SELLER
- **registerAdmin**: Registro de usuarios con rol ADMIN
- **login**: Inicio de sesión y generación de token JWT
- **addToFavorites**: Añadir producto a favoritos
- **removeFromFavorites**: Eliminar producto de favoritos

## Modelo de datos

### Usuario (User)

- **id**: Identificador único del usuario
- **email**: Correo electrónico (único, usado para autenticación)
- **password**: Contraseña encriptada
- **firstName**: Nombre del usuario
- **lastName**: Apellido del usuario
- **role**: Rol del usuario (CUSTOMER, SELLER, ADMIN)
- **favorites**: Lista de IDs de productos favoritos

## Integración con API Gateway

Este microservicio está diseñado para funcionar con un API Gateway, que enruta las solicitudes apropiadas a este servicio en función de las rutas configuradas. La integración con la API Gateway incluye los siguientes aspectos:

### Federación de Esquemas GraphQL

- El esquema GraphQL de este microservicio se federa con otros esquemas a través de la API Gateway.
- La API Gateway combina los esquemas de todos los microservicios para proporcionar un único punto de entrada para las consultas GraphQL.
- Los clientes pueden realizar consultas que abarcan múltiples microservicios a través de la API Gateway.

### Configuración para la Federación

Para habilitar la federación con el API Gateway, este microservicio:

1. Expone su esquema GraphQL en el endpoint `/graphql`
2. Utiliza Spring GraphQL para manejar las consultas y mutaciones
3. Configura correctamente los tipos de datos para ser compatibles con la federación

### Autenticación y Autorización

- Este microservicio genera tokens JWT que son utilizados por el API Gateway para autenticar solicitudes a otros servicios.
- Los tokens contienen información sobre el usuario, incluyendo su ID, email y rol.
- El API Gateway pasa estos tokens a los demás microservicios para validar permisos.

### Comunicación con otros Microservicios

- Este microservicio puede comunicarse con el microservicio de productos y órdenes para gestionar los productos favoritos de los usuarios.
- La comunicación se realiza a través del API Gateway, utilizando el esquema federado.

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
- Antes de añadir un producto a favoritos, se verifica su existencia mediante una llamada al servicio ms-products-orders.

## Roles de Usuario

- **CUSTOMER**: Usuarios que pueden navegar y comprar productos, gestionar su perfil y sus productos favoritos
- **SELLER**: Usuarios que pueden gestionar sus propios productos, además de todas las funcionalidades de CUSTOMER
- **ADMIN**: Usuarios con acceso completo a todas las funcionalidades del sistema

## Validaciones de seguridad implementadas

- Encriptación de contraseñas mediante BCrypt
- Validación de tokens JWT en cada solicitud
- Verificación de roles para acceso a endpoints protegidos
- Validación de datos de entrada en registro y login
- Verificación de unicidad de email en el registro
- Verificación de existencia de productos antes de añadirlos a favoritos

## Estructura del proyecto

```
ms-auth-java/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── auth/
│   │   │           ├── controller/      # Controladores REST y GraphQL
│   │   │           ├── model/           # Entidades y DTOs
│   │   │           ├── repository/      # Repositorios MongoDB
│   │   │           ├── service/         # Servicios de negocio
│   │   │           ├── security/        # Configuración de seguridad y JWT
│   │   │           └── config/          # Configuraciones generales
│   │   └── resources/
│   │       ├── application.yml          # Configuración de la aplicación
│   │       └── graphql/                 # Esquemas GraphQL
│   └── test/                            # Pruebas unitarias e integración
├── .env.example                         # Ejemplo de variables de entorno
├── .gitignore
├── docker-compose.yaml
├── Dockerfile
├── pom.xml                              # Dependencias Maven
└── README.md
```
