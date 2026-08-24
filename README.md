# Spark Mart

A full-stack e-commerce web application built with **Spring Boot 4.1**, **Thymeleaf**, **Spring Data JPA**, **Spring Security**, and **MySQL 8.0** — containerized and verified using **Docker Compose**. The project implements a Daraz-inspired storefront with customer, seller, and admin roles, cart/checkout/payment flows, order management, and an AI product suggestion feature.

---

### Tech Stack
* **Core Platform:** Java 25 (Virtual Threads enabled)
* **Framework:** Spring Boot 4.1.0 (Web MVC, Thymeleaf, Data JPA, Security, Actuator)
* **Database Layer:** MySQL 8.0 containerized with persistent storage (`mysql` service)
* **Database GUI:** phpMyAdmin containerized interface (http://localhost:8085)
* **ORM & Migrations:** Hibernate 7.4 (`ddl-auto=update`), Flyway 10.20
* **Containerization:** Docker & Docker Compose
* **Testing:** JUnit 5, Mockito, MockMvc (77 tests passed)

---

### Prerequisites
* Docker Desktop installed and running
* Java 25+ installed
* Maven (or the included `./mvnw` wrapper)

---

### Setup & Execution

#### Option 1: Run with Docker Compose (Recommended)
```bash
# Start all services (App + MySQL 8.0 + phpMyAdmin)
docker compose up --build -d
```
* **Application URL:** http://localhost:8080
* **phpMyAdmin URL:** http://localhost:8085

#### Option 2: Run Locally via Maven
```bash
# Run test suite (77 tests)
./mvnw clean test

# Run application locally
./mvnw spring-boot:run
```

> *The database `sparkmart` initializes automatically. Hibernate manages core entities, while Flyway manages schema migrations for `tags` and `product_tags` tables.*

---

### Environment Variables

| Variable | Default | Description |
| :--- | :--- | :--- |
| `DB_HOST` | `mysql` (Docker) / `localhost` | MySQL host address |
| `DB_PORT` | `3306` | MySQL internal port |
| `DB_NAME` | `sparkmart` | Database name |
| `DB_USERNAME` | `root` | Database username |
| `DB_PASSWORD` | `root` | Database password |
| `SPARKMART_ADMIN_USERNAME` | `admin@sparkmart.com` | Default admin email |
| `SPARKMART_ADMIN_PASSWORD` | `admin123` | Default admin password |

---

### Profiles
* **`default` / `dev`:** Connects to local environment configurations.
* **`prod`:** Configured for isolated container execution via environment variables.

*Activate via:* `--spring.profiles.active=dev` or `--spring.profiles.active=prod`

---

### Routes & Endpoints

#### Public Storefront
* `/` — Home page with featured products, categories, and search
* `/category/{slug}` — Category listing with filters
* `/categories` — All categories
* `/product/{id}` — Product detail page
* `/track-order` — Order tracking
* `/contact` — Contact page

#### Customer
* `/login` — Customer login
* `/register` — Customer registration
* `/seller/register` — Seller registration
* `/cart` — Shopping cart
* `/checkout` — Checkout form
* `/payment` — Payment processing
* `/orders` — Customer order history
* `/profile` — Customer profile management

#### Admin / Seller
* `/admin/login` — Admin & Seller login
* `/admin/dashboard` — Overview dashboard with business statistics
* `/admin/products` — Product catalog management
* `/admin/orders` — Order fulfillment and management
* `/admin/categories` — Category hierarchy management
* `/admin/customers` — Customer management
* `/admin/payments` — Transaction and payment records
* `/admin/analytics` — Business intelligence and analytics
* `/admin/inventory` — Stock and inventory tracking

#### Actuator & Monitoring
* `/actuator/health` — Health check endpoint (requires HTTP Basic authentication with admin credentials)

---

### Docker & Deployment Status
The project features a verified multi-container architecture via `docker-compose.yml`, orchestrating the Spring Boot application (port 8080), MySQL 8.0 (port 3306), and phpMyAdmin (port 8085) with isolated networking, persistent database volumes, and healthchecks.

---

### Course Documentation (DOCX References)
Authoritative chapter documents located in the `docs/` directory:
* **Chapter 1:** The Modern Spring Landscape
* **Chapter 2:** Dynamic Interfaces with Thymeleaf and Bootstrap
* **Chapter 3:** Persistence with Relational Databases
* **Chapter 4:** Advanced Relationships and Migrations
* **Chapter 5:** Spring Security and Identity
* **Chapter 6:** Testing Strategy for Modern Apps
* **Chapter 7:** Production Readiness, Cloud Native, and Spring AI

---

### Author
* **Created by:** Rejwana Akter
* **Live Showcase:** https://rejwana121.github.io/spark-mart/
* **Repository:** https://github.com/rejwana121/spark-mart
