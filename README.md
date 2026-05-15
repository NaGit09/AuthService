# 🔐 Furniro - Authentication Service

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.x-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

The **Authentication Service** is the backbone of the Furniro ecosystem, providing secure identity management, token-based authentication, and granular access control. Built with Spring Boot 3, it leverages modern security standards like JWT and OAuth2 Resource Server.

---

## ✨ Key Highlights

- 🔑 **Robust Authentication**: Secure registration and login flow with Bcrypt password encryption.
- 🎟️ **JWT Ecosystem**: State-of-the-art token management (Access & Refresh tokens) with customizable expiration.
- 🛡️ **Granular Security**: Role-Based Access Control (RBAC) ensuring only authorized users access sensitive endpoints.
- ⚡ **High Performance**: Redis integration for session management and token blacklisting/caching.
- 📧 **OTP Engine**: Built-in One-Time Password (OTP) verification for email validation and password recovery.
- 📡 **Event-Driven**: Kafka integration for broadcasting user-related events (e.g., account registration) to other microservices.
- 📖 **Self-Documenting**: Full OpenAPI/Swagger integration for seamless API exploration.

---

## 🛠️ Technology Stack

| Logic | Data | Security | DevOps |
| :--- | :--- | :--- | :--- |
| **Java 17** | **MySQL 8.x** | **Spring Security** | **Docker** |
| **Spring Boot 4.0.x** | **Redis** | **JWT (jjwt)** | **Maven** |
| **Spring Data JPA** | **Kafka** | **OAuth2 Resource Server** | **Dotenv-java** |
| **Lombok** | **Hibernate** | **Validation (Jakarta)** | **Springdoc OpenAPI** |

---

## 🚀 Getting Started

### Prerequisites

- **JDK 17+**
- **Maven 3.8+**
- **MySQL 8.0+** (Default port: `3306` or `3307`)
- **Redis 6.x+**
- **Apache Kafka** (Optional for local development, required for event streaming)

### Quick Setup

1. **Clone & Navigate**:
   ```bash
   git clone <repository-url>
   cd AuthService
   ```

2. **Configure Environment**:
   Create a `.env` file in the root:
   ```env
   # Database Configuration
   DATABASE_URL=jdbc:mysql://localhost:3307/furniro_db
   DATABASE_USERNAME=root
   DATABASE_PASSWORD=your_secure_password

   # JWT Configuration
   JWT_SECRET_KEY=your_super_secret_high_entropy_key_here
   JWT_ACCESS_EXPIRATION=3600000        # 1 Hour
   JWT_REFRESH_EXPIRATION=604800000    # 7 Days
   JWT_ALGORITHM=HmacSHA256

   # Server Path
   SERVER_PATH=/api/v1/furniro
   ```

3. **Run the Service**:
   ```bash
   ./mvnw spring-boot:run
   ```
   The service will be live at: `http://localhost:8080/api/v1/furniro`

---

## 📂 Project Architecture

```text
src/main/java/com/furniro/AuthService/
├── config/       # Security, Redis, Kafka, and Swagger configs
├── controller/   # REST API Entry points (Account, User, Address)
├── dto/          # Data Transfer Objects (Requests/Responses)
├── entity/       # JPA Entities (Database Schema)
├── repository/   # Data Access Layer (Spring Data JPA)
├── service/      # Core Business Logic & Orchestration
└── util/         # Helper classes (JWT, OTP generators)
```

---

## 🛣️ API Roadmap

### 🔓 Public Endpoints
| Endpoint | Method | Description |
| :--- | :---: | :--- |
| `/account/register` | `POST` | Create a new account |
| `/account/login` | `POST` | Login & receive JWT tokens |
| `/account/sendOTP` | `POST` | Request OTP for verification |
| `/account/confirmOTP` | `POST` | Verify OTP for account activation |

### 🔒 Secured Endpoints (requires JWT)
| Endpoint | Method | Description |
| :--- | :---: | :--- |
| `/account/logout` | `POST` | Invalidate current session |
| `/account/refresh` | `POST` | Get new access token |
| `/account/changePassword` | `POST` | Update account password |
| `/user/{id}` | `GET` | Get profile information |
| `/user` | `POST` | Create a new user profile |
| `/user/{id}` | `PUT` | Update an existing user profile |
| `/address/user/{userId}` | `GET` | Get addresses for a specific user |
| `/address` | `POST` | Add a new shipping/billing address |
| `/address/{id}` | `PUT` | Update an existing address |

### 🛠️ Admin Operations
| Endpoint | Method | Description |
| :--- | :---: | :--- |
| `/account/resetPassword` | `POST` | Force reset passwords (Bulk) |
| `/account/ban` | `POST` | Suspend user accounts |
| `/account/unban` | `POST` | Reactive suspended accounts |

> [!TIP]
> Use the **Swagger UI** for detailed schema definitions and live testing:
> [http://localhost:8080/api/v1/furniro/swagger-ui.html](http://localhost:8080/api/v1/furniro/swagger-ui.html)

---

## 🤝 Contributing

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git checkout origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
