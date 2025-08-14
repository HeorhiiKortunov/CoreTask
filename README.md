# ✅ Core Task REST API

Core Task is a RESTful service for managing companies, projects, and tasks. Built with Spring Boot, it uses JWT authentication and role-based access control. Designed for integration with web or mobile clients.

## 🌐 Features

- JWT authentication (company registration + user login)
- Role-based access:
  - **COMPANY_ADMIN** – manage company users and projects
  - **MANAGER** – manage projects and tasks within the company
  - **USER** – work with own tasks
- Company → Projects → Tasks hierarchy (full CRUD)
- Task filtering and pagination (including optional `projectId`)
- Request validation and global exception handling
- Authomatic email servise for sending invtations
- Multi-tenancy support – complete data isolation between companies
- Unit tests with JUnit 5 and Mockito

## 🛠 Technologies Used

- Java 17
- Spring Boot 3 (Web, Security, Data JPA, Validation)
- Hibernate & JPA
- PostgreSQL
- JWT (Bearer Tokens)
- Lombok
- JUnit 5, Mockito
- Docker / docker-compose

## 🚀 Getting Started

Clone the repository:

```bash
git clone https://github.com/HeorhiiKortunov/CoreTask.git
cd core-task
```

Start the database with Docker:

```bash
docker-compose up -d
```

By default, PostgreSQL will be available on localhost:5432.

Run the application:

```bash
mvn spring-boot:run -Dspring-boot.run
```

The API will be available at:

```
http://localhost:8080/api
```

🔧 Environment Variables

Create a .env file or set via system properties:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/core_task
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=root
JWT_SECRET=<your-strong-secret>
JWT_EXPIRATION=3600000
```

🔑 Example Endpoints
Authentication

POST /api/auth/register-company – register a company and its first admin
Request example:

```json
{
  "companyName": "Acme Inc",
  "adminEmail": "admin@acme.com",
  "adminPassword": "StrongPass123"
}
```

POST /api/auth/login – login and receive JWT
Response example:

{ "token": "<JWT_TOKEN>" }

  Use the header:
    Authorization: Bearer <JWT_TOKEN>

Users

  GET /api/users/me – get current user profile

  PATCH /api/users/{id}/roles – update user roles (COMPANY_ADMIN only)
  Request example:

  ```json
  { "roles": ["MANAGER", "USER"] }
```

Projects

  GET /api/projects – list company projects

   POST /api/projects – create a project

  ```json
  { "name": "Mobile App", "description": "iOS + Android" }
  ```

   PUT /api/projects/{id} – update a project

  DELETE /api/projects/{id} – delete a project

Tasks

  GET /api/tasks?projectId={id}&page=0&size=20 – get tasks (optional projectId)

  POST /api/tasks – create a task

  ```json
  {
    "projectId": 1,
    "title": "Implement login",
    "description": "JWT flow",
    "assigneeId": 5,
    "status": "OPEN",
    "priority": "HIGH",
    "dueDate": "2025-08-31"
  }
  ```

  GET /api/tasks/{id} – get a task

  PATCH /api/tasks/{id} – partially update a task (only provided fields)

  DELETE /api/tasks/{id} – delete a task

🧪 Testing

Run tests:

mvn test

🔒 Error Handling

  401 Unauthorized – missing or invalid JWT token

  403 Forbidden – insufficient permissions

  404 Not Found – resource not found

  400 Bad Request – validation error

Example response:

{
  "timestamp": "2025-08-14T14:10:00Z",
  "status": 400,
  "errors": [
    { "field": "title", "message": "must not be blank" }
  ]
}

🧰 Integration Notes

  Base URL: http://localhost:8080/api

  Headers:

  Content-Type: application/json

  athorization: Bearer <token>

🐳 Docker

Build and run:

```bash
mvn -DskipTests package
docker build -t core-task:latest .
docker run --env-file .env -p 8080:8080 core-task:latest
```

📄 License

This project is licensed under the MIT License.
👤 Heorhii Kortunov
📧 heorhiikortunov@gmail.com
🔗 https://github.com/HeorhiiKortunov
