# 🛍️ Core Module: Product Catalog and User Management

## 📌 Description

This is the main service of the application, responsible for:

* Managing the entire **product catalog (CRUD)**
* Handling **customer registration and authentication** with **JWT**

---

## 🛠️ Technologies Used

### Backend

* Java **17**
* Spring Boot **3**
* Spring Security **6** (JWT authentication)
* Spring Data JPA / Hibernate

### Database

* H2 (**development environment**)

### Testing

* JUnit **5**
* Mockito

### Others

* Lombok
* Maven
* Docker

---

## ▶️ How to Run

### 📋 Prerequisites

* JDK **17+**
* Maven **3.2+**
* Docker (optional)

---

### 🚀 Running with Spring Boot

You can run the application directly from your IDE or using Maven:

```bash
./mvnw spring-boot:run
```

The API will be available at:
👉 `http://localhost:8080`

---

### 🐳 Running with Docker

1. **Build the Docker image**

   ```bash
   docker build -t auth-microservice .
   ```

2. **Run the container**

   ```bash
   docker run -p 8080:8080 auth-microservice
   ```
