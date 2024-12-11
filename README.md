# Authentication and Student-Subject Management System

This project implements a RESTful API for managing authentication services, student details, and subjects using Spring Boot. It includes best practices for security, validation, and effective API design.

## Features

### Authentication Service
- **Sign Up**: Create a new user account.
- **Sign In**: Log in to the system.
- **Log Out**: End the user's session.
- **Partial Update**: Update user details using their email ID.
- **Refresh Token**: Generate a new access token using a refresh token.
- **Validate Access Token**: Validate an existing access token.

### Student Service
- **Create Student**: Add a new student to the system.
- **Retrieve All Students**: Fetch details of all students.

### Subject Service
- **Create Subject**: Add a new subject to the system.
- **Retrieve All Subjects**: Fetch details of all available subjects.

## Tech Stack

- **Backend Framework**: Spring Boot 3.x
- **Database**: H2 (In-Memory Database)
- **Validation**: Hibernate Validator (JSR 380)
- **Security**: Spring Security with JWT-based authentication and authorization
- **Dependency Management**: Maven
- **Language**: Java 21

## API Endpoints

### Authentication Service (`/auth-service`)
| HTTP Method | Endpoint                          | Description                 |
|-------------|-----------------------------------|-----------------------------|
| POST        | `/sign-up`                       | Register a new user         |
| POST        | `/sign-in`                       | Log in a user               |
| POST        | `/log-out`                       | Log out a user              |
| PATCH       | `/partial-update/{emailId}`      | Partially update user info  |
| POST        | `/refresh-token/{refreshToken}`  | Refresh access token        |
| POST        | `/validate-access-token/{accessToken}` | Validate access token |

### Student Service (`/student-service`)
| HTTP Method | Endpoint         | Description           |
|-------------|------------------|-----------------------|
| POST        | `/`              | Add a new student     |
| GET         | `/`              | Fetch all students    |

### Subject Service (`/subject-service`)
| HTTP Method | Endpoint         | Description           |
|-------------|------------------|-----------------------|
| POST        | `/create`        | Add a new subject     |
| GET         | `/all`           | Fetch all subjects    |

## Access Token Requirement

All API endpoints (except those under `/auth-service`) are protected and require a valid **access token** for authentication. If a valid token is not provided, the server will respond with:
- **401 Unauthorized**: If the token is missing or invalid.
- **403 Forbidden**: If the user does not have permission to access the resource.

### Steps to Get a Token
1. Use the `/auth-service/sign-in` endpoint to log in with valid credentials.
2. The response will include an access token (and optionally, a refresh token).
3. Include the access token in the `Authorization` header for all subsequent requests:
   ```http
   Authorization: Bearer <access_token>
   ```

## Example Requests and Responses

### Create a Subject
**Request**:
```http
POST /subject-service/create
Authorization: Bearer <access_token>
Content-Type: application/json
```
**Body**:
```json
{
    "name": "Spring Boot",
    "instructor": "paltu Bera",
    "semester": "9"
}
```
**Response**:
```json
{
    "id": "8c19f557-8680-46e1-a4ce-ebb81be243ee",
    "name": "Spring Boot",
    "instructor": "paltu Bera",
    "semester": "9"
}
```

### Retrieve All Subjects
**Request**:
```http
GET /subject-service/all
```
**Response**:
```json
[
    {
        "id": "c0e2c820-3d91-4123-8b53-2de4a7bb2fc7",
        "name": "Spring Boot",
        "instructor": "paltu Bera",
        "semester": "9"
    }
]
```

### Create a Student
**Request**:
```http
POST /student-service
Authorization: Bearer <access_token>
Content-Type: application/json
```
**Body**:
```json
{
    "email": "paltu@gmail.com",
    "subjectIds": ["8c19f557-8680-46e1-a4ce-ebb81be243ee"]
}
```
**Response**:
```json
{
    "name": "pabitra bera",
    "email": "paltu@gmail.com",
    "subjects": [
        {
            "id": "8c19f557-8680-46e1-a4ce-ebb81be243ee",
            "name": "Spring Boot",
            "instructor": "paltu Bera",
            "semester": "9"
        }
    ]
}
```

## Setup and Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd <repository-folder>
   ```

2. Build the project using Maven:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

4. Access the application endpoints:
   - H2 Console: `http://localhost:8080/h2-console`

## Testing
- Use Postman or similar tools to test endpoints.
- Ensure that the **access token** is included in the `Authorization` header for protected endpoints.

## Future Enhancements
- Implement role-based access control (RBAC) for fine-grained permissions.
- Add caching for frequently accessed resources.
- Integrate email services for user notifications.
