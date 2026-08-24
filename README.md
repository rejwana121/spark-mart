# Spark Mart

A full-stack e-commerce web application built with **Spring Boot 4.1**, **Thymeleaf**, **Spring Data JPA**, **Spring Security**, and **MySQL 8.0** — containerized and verified using **Docker Compose**. The project implements a storefront architecture featuring customer, seller, and admin roles, transactional checkout flows, and automated data migrations.

---

### Tech Stack

* **Core Platform:** Java 25 (Virtual Threads enabled)
* **Framework:** Spring Boot 4.1.0 (Web MVC, Thymeleaf, Data JPA, Security, Actuator)
* **Database Layer:** MySQL 8.0 containerized with persistent volume storage
* **Database Management:** phpMyAdmin containerized interface (`http://localhost:8085`)
* **ORM & Migrations:** Hibernate 7.4 (`ddl-auto=update`), Flyway 10.20
* **Containerization:** Docker & Docker Compose
* **Testing:** JUnit 5, Mockito, MockMvc (77 test suites)

---

### Prerequisites

* Docker Desktop installed and running
* Java 25+ SDK
* Apache Maven (or bundled `./mvnw`)

---

### Setup & Execution

#### 1. Multi-Container Docker Setup (Recommended)

```bash
docker compose up --build -d
```

* **Application Endpoint:** `http://localhost:8080`
* **Database Administration:** `http://localhost:8085`

#### 2. Local Maven Execution

```bash
# Run unit & integration test suites
./mvnw clean test

# Launch local Spring Boot runtime
./mvnw spring-boot:run
```

---

### Environment Variables

<table>
  <thead>
    <tr>
      <th>Variable</th>
      <th>Default</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>DB_HOST</code></td>
      <td><code>mysql</code> (Docker) / <code>localhost</code></td>
      <td>Database host address</td>
    </tr>
    <tr>
      <td><code>DB_PORT</code></td>
      <td><code>3306</code></td>
      <td>Database port connection</td>
    </tr>
    <tr>
      <td><code>DB_NAME</code></td>
      <td><code>sparkmart</code></td>
      <td>Target schema name</td>
    </tr>
    <tr>
      <td><code>DB_USERNAME</code></td>
      <td><code>root</code></td>
      <td>Database superuser username</td>
    </tr>
    <tr>
      <td><code>DB_PASSWORD</code></td>
      <td><code>root</code></td>
      <td>Database superuser password</td>
    </tr>
    <tr>
      <td><code>SPARKMART_ADMIN_USERNAME</code></td>
      <td><code>admin@sparkmart.com</code></td>
      <td>Administrative seed identity</td>
    </tr>
    <tr>
      <td><code>SPARKMART_ADMIN_PASSWORD</code></td>
      <td><code>admin123</code></td>
      <td>Administrative access credential</td>
    </tr>
  </tbody>
</table>

---

### Execution Profiles

* **`dev` / `default`:** Local development configuration binding.
* **`prod`:** Production-ready profile strictly driven by injected environment variables.
* **Activation:** Pass `--spring.profiles.active=dev` or `--spring.profiles.active=prod`

---

### Routes & Endpoints

#### Storefront & Catalog
* `/` — Landing showcase, curated categories, and index search
* `/category/{slug}` — Parametric category catalog with faceted navigation
* `/categories` — Global department index
* `/product/{id}` — Granular product specifications and inventory status
* `/track-order` — Real-time fulfillment shipment tracker
* `/contact` — Support inquiry portal

#### Customer Management & Checkout
* `/login` & `/register` — Customer authentication and lifecycle onboarding
* `/seller/register` — Merchant enrollment portal
* `/cart` — Stateful session cart management
* `/checkout` & `/payment` — Transactional checkout workflow
* `/orders` — Historical fulfillment logs
* `/profile` — Customer identity settings

#### Administrative Control Center
* `/admin/login` — Administrative console authentication
* `/admin/dashboard` — Executive business performance metrics
* `/admin/products` & `/admin/categories` — Catalog lifecycle orchestration
* `/admin/orders` & `/admin/payments` — Financial auditing and fulfillment management
* `/admin/inventory` & `/admin/analytics` — Stock allocation and business intelligence

#### Diagnostics & Actuator
* `/actuator/health` — Service availability monitor (Protected by HTTP Basic auth)

---

### Architecture & Deployment Verification

The architecture runs as a unified multi-service network orchestrated via `docker-compose.yml`. The Spring Boot service connects over isolated Docker bridge networking to the dedicated MySQL 8.0 engine, backed by isolated volumes for transactional integrity and automatic Flyway schema baseline application.

---

### Reference Course Chapters

Comprehensive architectural and engineering documentation located in `docs/`:
* **Chapter 1:** The Modern Spring Landscape
* **Chapter 2:** Dynamic Interfaces with Thymeleaf and Bootstrap
* **Chapter 3:** Persistence with Relational Databases
* **Chapter 4:** Advanced Relationships and Migrations
* **Chapter 5:** Spring Security and Identity
* **Chapter 6:** Testing Strategy for Modern Apps
* **Chapter 7:** Production Readiness, Cloud Native, and Spring AI

---

### Project Maintainer
* **Author:** Rejwana Akter
* **Live Showcase:** [https://rejwana121.github.io/spark-mart/](https://rejwana121.github.io/spark-mart/)
* **Repository:** [https://github.com/rejwana121/spark-mart](https://github.com/rejwana121/spark-mart)
