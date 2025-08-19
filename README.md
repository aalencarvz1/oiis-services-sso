# SSO Service

This project implements a Single Sign-On (SSO) service built with Spring Boot, providing authentication and token validation for client applications. It uses JSON Web Tokens (JWT) for secure authentication and integrates with a database to manage user credentials. The primary entry point for client API requests is the `AuthenticationRestController`.

## Features
- User authentication via email and password.
- JWT token generation and validation.
- Public endpoints for login, token checking, and other authentication-related operations.
- Secure password storage using BCrypt.
- Configurable public endpoints for unauthenticated access.
- RESTful API for integration with client applications.

## Prerequisites
- **Java**: 17 or higher
- **Spring Boot**: 3.x
- **Database**: Configured relational database (e.g., MySQL, PostgreSQL) for user storage
- **Maven**: For dependency management
- **External Client Applications**: Must be configured to send JWT tokens in the `Authorization` header

## Installation

1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd sso-service
   ```

2. **Add Dependencies**:
   Ensure the following dependencies are included in your `pom.xml`:

   ```xml
   <dependencies>
       <!-- Spring Boot Starter Web -->
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-web</artifactId>
       </dependency>
       <!-- Spring Boot Starter Security -->
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-security</artifactId>
       </dependency>
       <!-- Spring Boot Starter Data JPA -->
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-data-jpa</artifactId>
       </dependency>
       <!-- JWT Library -->
       <dependency>
           <groupId>io.jsonwebtoken</groupId>
           <artifactId>jjwt</artifactId>
           <version>0.9.1</version>
       </dependency>
       <!-- Database Driver (e.g., MySQL) -->
       <dependency>
           <groupId>mysql</groupId>
           <artifactId>mysql-connector-java</artifactId>
           <version>8.0.33</version>
       </dependency>
       <!-- Lombok (optional, for reducing boilerplate code) -->
       <dependency>
           <groupId>org.projectlombok</groupId>
           <artifactId>lombok</artifactId>
           <scope>provided</scope>
       </dependency>
   </dependencies>
   ```

3. **Build the Project**:
   ```bash
   mvn clean install
   ```

4. **Run the Application**:
   ```bash
   mvn spring-boot:run
   ```

## Configuration

Configure the application using `application.yml` or `application.properties`. Below are the required properties:

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sso_db?useSSL=false&serverTimezone=UTC
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

jwt:
  secret-key: "your-secure-jwt-secret-key"  # Replace with a secure key (at least 256 bits)
```

### application.properties
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sso_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
jwt.secret-key=your-secure-jwt-secret-key
```

### Property Descriptions
- **`spring.datasource.url`**: URL of the database for user storage.
- **`spring.datasource.username`**: Database username.
- **`spring.datasource.password`**: Database password.
- **`spring.datasource.driver-class-name`**: JDBC driver class for the database.
- **`spring.jpa.hibernate.ddl-auto`**: Hibernate DDL strategy (e.g., `update`, `create`, `validate`).
- **`jwt.secret-key`**: A secure key for signing JWT tokens (must be at least 256 bits for HS256).

## Usage

The `AuthenticationRestController` is the main entry point for client API requests. It exposes the following endpoints:

### Public Endpoints
The following endpoints are publicly accessible (no authentication required), as defined in `SecurityConfig.java`:

- **`/auth/login`**: Authenticates a user and returns a JWT token.
- **`/auth/check_token`**: Validates a JWT token.
- **`/auth/register`**: Registers a new user (implementation not shown in provided files).
- **`/auth/send_email_recover_password`**: Sends a password recovery email.
- **`/auth/password_change`**: Changes a user's password.
- **`/auth/refresh_token`**: Refreshes an existing JWT token.

### API Endpoints

#### 1. Login
- **Endpoint**: `POST /auth/login`
- **Request Body**:
  ```json
  {
      "email": "user@example.com",
      "password": "your_password"
  }
  ```
- **Response** (Success):
  ```json
  {
      "success": true,
      "data": {
          "user_id": 1,
          "token": "eyJhbGciOiJIUzI1NiJ9..."
      }
  }
  ```
- **Response** (Error):
  ```json
  {
      "success": false,
      "message": "user not found",
      "httpStatus": 401
  }
  ```

#### 2. Check Token
- **Endpoint**: `POST /auth/check_token`
- **Request Body**:
  ```json
  {
      "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
  ```
- **Response** (Success):
  ```json
  {
      "success": true,
      "data": {
          "user_id": 1,
          "token": "eyJhbGciOiJIUzI1NiJ9..."
      }
  }
  ```
- **Response** (Error):
  ```json
  {
      "success": false,
      "message": "invalid token",
      "httpStatus": 401
  }
  ```

### Example Request
To authenticate a user:

```bash
curl -X POST http://localhost:8080/auth/login \
-H "Content-Type: application/json" \
-d '{"email": "user@example.com", "password": "your_password"}'
```

To validate a token:

```bash
curl -X POST http://localhost:8080/auth/check_token \
-H "Content-Type: application/json" \
-d '{"token": "eyJhbGciOiJIUzI1NiJ9..."}'
```

## How It Works
1. **AuthenticationRestController**:
    - Handles incoming requests for login and token validation.
    - Delegates to `AuthenticationService` for processing.

2. **AuthenticationService**:
    - Manages user authentication by verifying email and password against the database.
    - Uses `JwtService` to generate or validate JWT tokens.
    - Checks user status (active, not deleted) before issuing tokens.

3. **JwtService**:
    - Generates JWT tokens with a user ID claim and a 1-day expiration.
    - Validates tokens by checking their signature, expiration, and structure.
    - Returns a `DefaultDataSwap` object with success/failure details.

4. **SecurityConfig**:
    - Configures Spring Security to allow public access to authentication endpoints.
    - Denies access to all other endpoints by default.
    - Disables CSRF for stateless API usage.

## Integration with Client Applications
Client applications should:
1. Call `/auth/login` to obtain a JWT token.
2. Include the token in the `Authorization` header (`Bearer <token>`) for protected API requests.
3. Use `/auth/check_token` to validate tokens if needed.

Ensure the client application is configured to use the SSO server's base URL (e.g., `http://localhost:8080`) and the `/auth/check_token` endpoint for token validation, as shown in the previous `BaseClientSsoSecurityConfig` example.

## Troubleshooting
- **401 Unauthorized**: Check if the email/password is correct or if the user is active in the database.
- **Invalid Token**: Ensure the `jwt.secret-key` matches between the SSO server and client applications.
- **Database Errors**: Verify database connectivity and correct configuration in `application.yml`.
- **CORS Issues**: If integrating with a frontend, ensure CORS is enabled or customized in `SecurityConfig`.

## Logging
The application uses SLF4J for logging. Key events (e.g., login attempts, token validation) are logged at the `DEBUG` level. Configure your logging framework (e.g., Logback) to capture these logs.

## Contributing
For issues, feature requests, or contributions, please submit a pull request or contact the maintainers.

## License
This project is licensed under the MIT License. See the `LICENSE` file for details.