# Appointment Management

Independent Spring Boot module of the Medical Appointment Scheduling System.

## Run with IntelliJ IDEA
1. Open this folder (the one containing `pom.xml`).
2. Allow Maven to download dependencies.
3. Run `src/main/java/com/medical/MedicalApplication.java`.
4. Visit **http://localhost:8083**.

## Technology
Java 17, Spring Boot 3.3.5, Maven, HTML, CSS, JavaScript and local text-file storage.

## Module
This standalone ZIP contains **only Appointment Management** and its matching frontend, REST controller, service, model and validation classes.
It can be pushed to a member's GitHub branch/repository independently.
When merging branches into one Spring Boot app, do not blindly overwrite other people's `pom.xml`, `MedicalApplication.java` or root web pages.

## Deployment
Dockerfile is included. Set environment variable `PORT` on deployment if needed. Cloud containers may not preserve local text files between restarts. Use demonstration data, not actual patient or payment information.
