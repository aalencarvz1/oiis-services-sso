# SSO Service

A lightweight, Spring Boot–based authentication and token validation microservice designed for modular integration across multiple backend or frontend applications.

---

## 🚀 Overview

The **SSO Service** provides centralized login and token validation endpoints.  
It can be used as a standalone authentication provider for any number of web or API-based systems.  

Developers can easily clone this repository, rename the base package, and adjust configuration parameters to match their own environment.

Key features:
- User registration and login with email and password.
- JWT-based authentication with access tokens and refresh tokens.
- Token validation and refresh.
- Password recovery workflow: Send recovery email with a token and change password using the token.
- Basic email validation and sending using JavaMailSender.

The service is designed to be secure, with password hashing via BCrypt, and supports CORS for cross-origin requests. All endpoints are under `/auth` and are publicly accessible (no authentication required for these routes, as defined in `SecurityConfig`).

---

## ⚙️ Project Setup

### 1. Clone the Repository

```bash
git clone https://github.com/aalencarvz1/oiis-services-sso.git
cd sso-service
```

### 2. Build the Project

Make sure you have **Java 17+** and **Maven 3.8+** installed.

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

By default, the service will start at:

```
http://localhost:3001
```

---

## How It Works

1. **Registration/Login**: Checks for existing user, hashes password, generates JWT tokens (access: 1 hour, refresh: 1 day).
2. **Token Management**: Uses HMAC-SHA256 signing. Validation checks expiration, signature, etc.
3. **Password Recovery**: Generates a special token, sends email with HTML/text content, and validates it on change.
4. **Security**: All public endpoints are listed in `SecurityConfig`. User status must be active (not deleted/soft-deleted).
5. **Logging**: Uses SLF4J for debug logs on requests and operations.
6. **Error Handling**: Catches exceptions and returns structured errors.

---

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

## 🗄️ Database Configuration

You must create the database manually before running the application.  
The default name is defined in `application.yml` under the property:

```yaml
spring:
  datasource:
    sso:
      url: jdbc:mysql://localhost:3306/sso_db
      username: root
      password: root
```

Create this database manually in MySQL or MariaDB:

```sql
CREATE DATABASE sso_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

If you want to use a different database, update the `application.yml` accordingly or create an `.env` file with custom environment variables.

---

## 🌐 Environment Configuration

You can configure environment-specific settings in **three different ways**:

1. **Default configuration:** modify directly in `application.yml`  
2. **Environment override:** create a `.env` file (e.g., `.env.dev`, `.env.prod`) with properties such as:
   ```env
   SPRING_DATASOURCE_SSO_URL=jdbc:mysql://localhost:3306/custom_db
   SPRING_DATASOURCE_SSO_USERNAME=custom_user
   SPRING_DATASOURCE_SSO_PASSWORD=custom_password
   ```
3. **Extended configuration:** create an additional `application-[profile].yml` (e.g., `application-prod.yml`) and activate it via environment variable:
   ```bash
   export SPRING_PROFILES_ACTIVE=prod
   ```

---

## 🧩 Package Renaming

If you clone this repository and want to rename the package (for example, from `com.oiis.services.sso` to `com.my_services.sso`), follow these steps:

1. In your IDE (e.g., IntelliJ IDEA), **right-click** the package name → **Refactor → Rename**  
2. Enable the options:
   - ✅ *Search in comments and strings*
   - ✅ *Search for text occurrences*
3. Rebuild the project to ensure all imports and annotations are updated.  
4. Update your **Run Configuration** if necessary — the *Main class* must point to the new package (e.g., `com.my_services.sso.SsoApplication`).

---

## 💻 JavaScript Usage Example

### Login Request

```javascript
fetch("http://localhost:3001/auth/login", {
  method: "POST",
  headers: {
    Accept: "application/json",
    "Content-Type": "application/json"
  },
  body: JSON.stringify({
    email: email,
    password: password
  })
}).then(async (resultLoginRequest) => {
  const resultLoginJson = await resultLoginRequest.json();
  if (resultLoginRequest.status === 200 && resultLoginJson?.success && resultLoginJson?.data?.token) {
    // Example of an authenticated call
    await fetch("http://your-backend-api-url", {
      headers: {
        Authorization: `Bearer ${resultLoginJson.data.token}`
      }
    });
  }
});
```

### Token Validation Middleware (Node.js Example)

```javascript
async function middlewareCheckTokenExample(req, res, next) {
  const response = await fetch("http://localhost:3001/auth/check_token", {
    method: "POST",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ token: req.headers.authorization })
  });

  const resultCheckTokenJson = await response.json();

  if (response.status === 200 && resultCheckTokenJson?.success) {
    return next();
  }

  res.status(401).send("Invalid or expired token");
}
```

---

## ☕ Java Backend Usage Example

Below is an example of how your backend service can validate tokens against this SSO service using Spring’s `WebClient`.

```java
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Missing or invalid token");
        return;
    }

    String token = authHeader.substring(7);

    ResponseEntity<DefaultDataSwap> responseEntity = webClient.post()
        .uri("http://localhost:3001/auth/check_token")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{"token":"" + token + ""}")
        .exchangeToMono(requestResponse ->
            requestResponse.bodyToMono(DefaultDataSwap.class)
                .map(body -> ResponseEntity.status(requestResponse.statusCode()).body(body))
        )
        .block();

    if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
        // Token is valid – continue the filter chain
        filterChain.doFilter(request, response);
    } else {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Token validation failed");
    }
}
```

---

## Others examples

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

---

## 🧠 Notes

- All endpoints respond with a standardized JSON structure compatible with the `DefaultDataSwap` class.
- JWT tokens generated by this service should be included in your client requests using the `Authorization: Bearer <token>` header.

---

## 🤝 Contributing

1. Fork this repository  
2. Create a feature branch (`git checkout -b feature/your-feature`)  
3. Commit your changes (`git commit -m "Added new feature"`)  
4. Push to the branch (`git push origin feature/your-feature`)  
5. Open a Pull Request  

---

## 📜 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
