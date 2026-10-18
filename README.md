# Turnora – Smart Digital Queue & Service Platform

Turnora is a web-based digital queue platform designed for college offices and service counters. It helps users join a queue, receive a token, and track queue progress, while staff can process tokens and administrators can manage services, counters, and staff.

## ✨ Features

- User registration and login with role-based access
- Digital token generation and queue joining
- Public token tracking and queue display board
- Live queue updates using WebSocket/STOMP
- Estimated waiting time
- Priority-based token handling
- Admin dashboard for managing services, counters, and staff
- Staff counter screen for calling and processing tokens
- User token history and notifications
- Queue analytics and service history
- PostgreSQL database integration

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| Frontend | React 18, Vite, JavaScript, Tailwind CSS |
| Routing | React Router DOM |
| API communication | Axios |
| Real-time updates | WebSocket, STOMP, SockJS |
| Backend | Java 17, Spring Boot 3 |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL |
| Authentication | Spring Security, JWT |
| API documentation | Springdoc OpenAPI / Swagger UI |
| Build tool | Maven |

## 📁 Project Structure

```text
Turnora/
├── frontend/          # React and Vite application
│   ├── src/
│   │   ├── api/       # API client configuration
│   │   ├── components/
│   │   ├── context/
│   │   ├── layouts/
│   │   └── pages/     # Public, student, staff and admin pages
│   └── package.json
├── backend/           # Spring Boot application
│   ├── src/main/java/com/turnora/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   └── pom.xml
└── README.md
```

## 📋 Prerequisites

Install the following before running the project:

- Java JDK 17
- Maven
- Node.js and npm
- PostgreSQL
- Git (optional, for cloning the repository)

## ⚙️ Setup and Installation

### 1. Get the Project

Clone the repository or extract the project ZIP, then open a terminal in the project root.

```bash
git clone https://github.com/Shruti123-coder1/Turnora.git
cd Turnora
```

### 2. Create the PostgreSQL Database

Start PostgreSQL and create a database named `turnora_db` (or use another database name and update the configuration accordingly).

```sql
CREATE DATABASE turnora_db;
```

### 3. Configure the Backend

Go to the backend folder:

```bash
cd backend
```

Copy `src/main/resources/application.properties.example` to `src/main/resources/application.properties`.

Update the database username and password, and ensure the database URL points to your PostgreSQL instance. The example configuration uses these environment variables/defaults:

| Variable | Purpose | Example Default |
|---|---|---|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/turnora_db` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | Set your local password |
| `JWT_SECRET` | Secret used for JWT signing | Replace with a long random secret (32+ characters) |
| `CORS_ORIGINS` | Allowed frontend origin | `http://localhost:5173` |
| `SMS_ENABLED` | Enable SMS integration | `false` |

Do not commit real passwords, JWT secrets, or provider API keys to version control.

### 4. Run the Backend

From the `backend` directory, run:

```bash
mvn spring-boot:run
```

The backend starts at:

```text
http://localhost:8080
```

### 5. Run the Frontend

Open a **second terminal** and navigate to the frontend directory from the project root:

```bash
cd frontend
npm install
npm run dev
```

Open the local URL printed by Vite, usually:

```text
http://localhost:5173
```

Keep both terminals running while using the application.

## 🔐 Default Local Accounts

On the first run, if the users table is empty, the backend initializer creates these development accounts:

| Role | Email | Default Password |
|---|---|---|
| Admin | `admin@turnora.com` | `Admin@123` |
| Staff | `staff1@turnora.com` | `Staff@123` |
| Staff | `staff2@turnora.com` | `Staff@123` |

These accounts are created only when the user table is empty. If users already exist, the default accounts may not be recreated. Change default credentials before deploying the application to a shared or production environment.

## 📚 API Documentation

When the backend is running, Swagger UI is typically available at:

```text
http://localhost:8080/swagger-ui/index.html
```

## 📄 License

No license is currently specified. Contact the project owner before redistributing or reusing this project.
