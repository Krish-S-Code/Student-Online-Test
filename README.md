# Student Online Test and Grade Evaluation System

## 1. Project Overview

The Student Online Test and Grade Evaluation System is a web-based application developed to simplify the process of conducting online tests and viewing student results.

The application provides a platform where students can register, log in, attend online tests, and view their results. An administrator can access the admin dashboard and add questions for tests.

The project uses Java for backend processing, HTML, CSS, and JavaScript for the user interface, and MySQL for database management.

## 2. Objectives

- To provide an online platform for conducting student tests.
- To allow students to register and log in to the system.
- To enable administrators to add questions.
- To provide an interface for students to attend tests.
- To allow students to view their test results.
- To store and manage application data using a MySQL database.

## 3. Features

### Student Features
- Student registration
- Student login
- Online test interface
- View test results

### Administrator Features
- Admin dashboard
- Add questions for tests

## 4. Technologies Used

| Technology | Purpose |
|---|---|
| HTML | Structure of web pages |
| CSS | Styling and page layout |
| JavaScript | Client-side interactions |
| Java | Backend application logic |
| Jakarta Servlets | Handling web requests |
| MySQL | Database management |
| MySQL Connector/J | Java-to-MySQL connectivity |
| Apache Tomcat 11 | Web application server |
| Visual Studio Code | Development environment |

## 5. Database

The application uses a MySQL database named `student_online_test`.

The database contains the following tables:

- `students` — student account information
- `tests` — test information
- `questions` — test questions
- `student_answers` — student answers
- `test_attempts` — test attempt records

These tables support the application's account management, test functionality, and result-related operations.

## 6. Application Requirements

To run the application locally, the following software and components are required:

- Java Development Kit (JDK)
- MySQL Server
- Apache Tomcat 11
- MySQL Connector/J
- Visual Studio Code or another suitable code editor

## 7. Installation and Setup

### Step 1: Download the Project

Clone the repository or download the project files from GitHub.

### Step 2: Create the Database

Open MySQL and execute:

```sql
CREATE DATABASE student_online_test;
```

Create the required tables using the database schema supplied with the project.

### Step 3: Configure Database Connectivity

Check the database connection settings in `DBUtil.java` or the relevant configuration file.

Configure the database URL, username, and password for your local MySQL installation. Do not publish real credentials in the repository.

### Step 4: Configure Dependencies

Ensure that the required MySQL Connector/J library is available to the application and that the Jakarta Servlet API is provided by the Tomcat environment.

### Step 5: Deploy the Application

Deploy the application to Apache Tomcat 11 using the appropriate web application structure.

### Step 6: Run the Application

Start MySQL and Tomcat, then open the application using its configured local URL in a web browser.

## 8. Security Considerations

- Do not store database passwords or other secrets in publicly accessible source code.
- Do not commit real student records or private database backups.
- Use fictional data when demonstrating the application.
- Restrict administrative functions to authorized administrators.

## 9. Project Status

**Status:** Working locally.

The application has been developed and is running in the local development environment.

## 10. Conclusion

The Student Online Test and Grade Evaluation System demonstrates the use of Java web development, database connectivity, and frontend technologies to support an online testing workflow. It provides student registration and login, administrative question management, online tests, and access to test results.

The project serves as an academic application of web programming and database management concepts.
