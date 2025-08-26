# SSO Service

## Overview

This project is a Single Sign-On (SSO) service built with Spring Boot. It provides user authentication, registration, token management using JWT (JSON Web Tokens), and password recovery via email. The service uses a MySQL database for storing user data, Flyway for database migrations (executed after Hibernate schema creation), and Spring Security for handling CORS and public endpoints.

Key features:
- User registration and login with email and password.
- JWT-based authentication with access tokens and refresh tokens.
- Token validation and refresh.
- Password recovery workflow: Send recovery email with a token and change password using the token.
- Basic email validation and sending using JavaMailSender.

The service is designed to be secure, with password hashing via BCrypt, and supports CORS for cross-origin requests. All endpoints are under `/auth` and are publicly accessible (no authentication required for these routes, as defined in `SecurityConfig`).

## Dependencies

- **Spring Boot**: Core framework.
- **Spring Security**: For security configurations (CSRF disabled, CORS enabled).
- **JWT (io.jsonwebtoken)**: For token generation and validation.
- **Spring Data JPA**: For database interactions with entities like `User`.
- **Flyway**: For database migrations (located in `classpath:db/migration`).
- **Lombok**: For getters/setters in entities.
- **Jackson**: For JSON processing.
- **SLF4J**: For logging.
- **MySQL Connector**: Assumes a MySQL database (configured in `FlywayAfterHibernate` with hardcoded credentials for dev: URL `jdbc:mysql://127.0.0.1:3306/oiis_sso_dev_v1`, user `root`, password `masterkey`).
- **JavaMailSender**: For email sending (configured via `spring.mail` properties).

**Note**: In production, replace hardcoded database credentials with environment variables or a secure configuration.

## Database Schema

The service uses JPA entities:
- **User**: Stores user details like `email`, `password` (hashed), `lastToken`, `lastRefreshToken`, `lastPasswordChangeToken`, and status fields.
- **BaseSsoTableModel**: Abstract base for entities with common fields like `id`, `statusRegId` (active/inactive), `createdAt`, `deletedAt`.

Migrations are handled by Flyway after Hibernate creates the schema.

## Configuration

- **JWT Properties**: Defined in `JwtProperties` (e.g., secret key via `jwt.secret-key` in application properties).
- **Mail Properties**: Configured via `spring.mail.username`, etc. Emails are sent from this address.
- **Security**: All `/auth/*` endpoints are public. Other requests are denied by default.

## Endpoints

All endpoints are under `/auth` and use POST methods. Requests and responses are in JSON format. Responses are wrapped in a `DefaultDataSwap` object, which includes:
- `success`: Boolean (true/false).
- `data`: Object (e.g., user details and token).
- `message`: String (error message if failed).
- `httpStatus`: HTTP status code.

### 1. **Login**
   - **Method**: POST
   - **Path**: `/auth/login`
   - **Description**: Authenticates a user with email and password. Returns a JWT access token, refresh token, and user details if successful.
   - **Request Body** (UserRequestDTO):
     ```json
     {
       "email": "string",
       "password": "string"
     }
     ```
   - **Response**:
     - Success (200 OK):
       ```json
       {
         "success": true,
         "data": {
           "token": "jwt-access-token",
           "refreshToken": "jwt-refresh-token",
           "user": {
             "id": 1,
             "email": "user@example.com"
           }
         }
       }
       ```
     - Error (e.g., 401 Unauthorized): `{ "success": false, "message": "password not match" }`
   - **Example (curl)**:
     ```
     curl -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email": "user@example.com", "password": "secret"}'
     ```

### 2. **Register**
   - **Method**: POST
   - **Path**: `/auth/register`
   - **Description**: Registers a new user with email and password. Password is hashed with BCrypt. Returns JWT tokens and user details if successful.
   - **Request Body** (UserRequestDTO): Same as Login.
   - **Response**: Same as Login.
     - Error (409 Conflict): `{ "success": false, "message": "user already exists" }`
   - **Example (curl)**:
     ```
     curl -X POST http://localhost:8080/auth/register \
     -H "Content-Type: application/json" \
     -d '{"email": "newuser@example.com", "password": "secret"}'
     ```

### 3. **Check Token**
   - **Method**: POST
   - **Path**: `/auth/check_token`
   - **Description**: Validates a JWT token and returns user details if valid.
   - **Request Body** (TokenRequestDTO):
     ```json
     {
       "token": "jwt-token"
     }
     ```
   - **Response**: Similar to Login (includes user and token).
     - Error (401 Unauthorized): `{ "success": false, "message": "token expired" }`
   - **Example (curl)**:
     ```
     curl -X POST http://localhost:8080/auth/check_token \
     -H "Content-Type: application/json" \
     -d '{"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}'
     ```

### 4. **Refresh Token**
   - **Method**: POST
   - **Path**: `/auth/refresh_token`
   - **Description**: Generates a new access token and refresh token using a valid refresh token.
   - **Request Body** (RefreshTokenRequestDTO):
     ```json
     {
       "refreshToken": "jwt-refresh-token"
     }
     ```
   - **Response**: Same as Login (new tokens and user).
     - Error: Similar to Check Token.
   - **Example (curl)**:
     ```
     curl -X POST http://localhost:8080/auth/refresh_token \
     -H "Content-Type: application/json" \
     -d '{"refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}'
     ```

### 5. **Send Email Recover Password**
   - **Method**: POST
   - **Path**: `/auth/send_email_recover_password`
   - **Description**: Sends a password recovery email with a link containing a one-time token. The link points to a provided interface path.
   - **Request Body** (PasswordRecoverRequestDTO):
     ```json
     {
       "email": "string",
       "passwordChangeInterfacePath": "string"
     }
     ```
   - **Response**:
     - Success (200 OK): `{ "success": true }`
     - Error (417 Expectation Failed): `{ "success": false, "message": "user not found" }`
   - **Email Content**: Includes a link like `{passwordChangeInterfacePath}/{token}`.
   - **Example (curl)**:
     ```
     curl -X POST http://localhost:8080/auth/send_email_recover_password \
     -H "Content-Type: application/json" \
     -d '{"email": "user@example.com", "passwordChangeInterfacePath": "https://example.com/reset"}'
     ```

### 6. **Password Change**
   - **Method**: POST
   - **Path**: `/auth/password_change`
   - **Description**: Changes the user's password using a valid recovery token. Password is hashed.
   - **Request Body** (PasswordChangeRequestDTO):
     ```json
     {
       "token": "recovery-token",
       "password": "new-password"
     }
     ```
   - **Response**:
     - Success (200 OK): `{ "success": true }`
     - Error (417 Expectation Failed): `{ "success": false, "message": "token not match" }`
   - **Example (curl)**:
     ```
     curl -X POST http://localhost:8080/auth/password_change \
     -H "Content-Type: application/json" \
     -d '{"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", "password": "newsecret"}'
     ```

## How It Works

1. **Registration/Login**: Checks for existing user, hashes password, generates JWT tokens (access: 1 hour, refresh: 1 day).
2. **Token Management**: Uses HMAC-SHA256 signing. Validation checks expiration, signature, etc.
3. **Password Recovery**: Generates a special token, sends email with HTML/text content, and validates it on change.
4. **Security**: All public endpoints are listed in `SecurityConfig`. User status must be active (not deleted/soft-deleted).
5. **Logging**: Uses SLF4J for debug logs on requests and operations.
6. **Error Handling**: Catches exceptions and returns structured errors.

## Setup and Running

1. Clone the repository.
2. Configure `application.properties` for JWT secret, mail server, and database.
3. Run with Maven: `mvn spring-boot:run`.
4. Database: Ensure MySQL is running; Flyway will migrate on startup.

## Limitations and Improvements

- No user roles/permissions beyond basic access.
- Hardcoded DB credentials in `FlywayAfterHibernate`—use profiles for prod.
- Email validation is basic; enhance for production.
- Add rate limiting for brute-force protection.
- Tests: Add unit/integration tests for services and controllers.

For questions, contact the development team.