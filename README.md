# Furniro - Authentication Service

Welcome to the **Authentication Service** of the Furniro project. This service handles user registration, authentication, token management (JWT), and security for the Furniro ecosystem.

## 🚀 Features

- **User Authentication**: Secure login and signup with encrypted passwords.
- **JWT Management**: Access and Refresh token generation and validation.
- **Role-Based Access Control**: Secure endpoints based on user roles.
- **Session Management**: Integrated with Redis for scalable session handling.
- **Database Integration**: MySQL for persistent storage using Spring Data JPA.
- **API Documentation**: Interactive Swagger/OpenAPI documentation.
- **Environment Support**: Configuration managed via `.env` files.

## 🛠️ Technology Stack

- **Lanuage**: Java 17
- **Framework**: Spring Boot 4.0.x
- **Security**: Spring Security (OAuth2 Resource Server, JWT)
- **Database**: MySQL 8.x
- **Caching/Session**: Redis
- **ORM**: Hibernate / Spring Data JPA
- **Documentation**: Springdoc OpenAPI (Swagger)
- **Utilities**: Lombok, Dotenv-java

## 📋 Prerequisites

Before running the application, ensure you have the following installed:

- **Java Development Kit (JDK) 17** or higher.
- **Maven** (to build and run the project).
- **MySQL Server** (running on port `3307` or as configured in `.env`).
- **Redis Server** (running on port `6379`).

## ⚙️ Configuration

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd AuthService
   ```

2. **Environment Variables**:
   Create a `.env` file in the root directory (one already exists as a template) and configure your local settings:

   ```env
   JWT_SECRET_KEY=your_secret_key_here
   JWT_ACCESS_EXPIRATION=3600000
   JWT_REFRESH_EXPIRATION=604800000
   JWT_ALGORITHM=HmacSHA256

   SERVER_PATH=/api/v1/furniro

   DATABASE_URL=jdbc:mysql://localhost:3307/furniro_db
   DATABASE_USERNAME=root
   DATABASE_PASSWORD=your_password
   ```

3. **Database Setup**:
   - Create a database named `furniro_db` in your MySQL instance.
   - The application is configured with `ddl-auto: update`, so tables will be created automatically on startup.

## 🏃 Running the Application

You can run the application using Maven:

```bash
./mvnw spring-boot:run
```

The server will start at `http://localhost:8080/api/v1/furniro` (unless you change the port or context path).

## 📖 API Documentation

Once the application is running, you can access the interactive API documentation at:

- **Swagger UI**: `http://localhost:8080/api/v1/furniro/swagger-ui.html`
- **OpenAPI Specs**: `http://localhost:8080/api/v1/furniro/v3/api-docs`

## 🔑 Key API Endpoints

The service exposes the following authentication endpoints (all prefixed with `/api/v1/furniro`):

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/account/register` | `POST` | Register a new user account |
| `/account/login` | `POST` | Authenticate user and receive tokens |
| `/account/sendOTP` | `POST` | Send OTP for email verification/password reset |
| `/account/confirmOTP` | `POST` | Verify the OTP sent to the user |
| `/account/refresh` | `POST` | Refresh the access token using a refresh token |
| `/account/logout` | `POST` | Invalidate the current session |
| `/account/changePassword`| `POST` | Update user password |

*Note: Most endpoints are white-listed for authentication purposes. More details can be found in the Swagger documentation.*

## 📂 Project Structure

- `src/main/java/com/furniro/AuthService/`
  - `controller/`: REST controllers for handling API requests.
  - `service/`: Business logic and service layer.
  - `repository/`: Data access layer (Spring Data JPA).
  - `entity/`: Database entities/models.
  - `config/`: Configuration classes (Security, Redis, etc.).
  - `dto/`: Data Transfer Objects for API requests/responses.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
