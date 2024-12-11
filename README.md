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
- **Validation**: Hibernate Validator
- **Security**: Spring Security with best practices for securing APIs
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

## Setup and Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd <repository-folder>
