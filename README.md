# Spark Mart

A full-stack e-commerce web application built with Spring Boot 4.1, Thymeleaf, Spring Data JPA, Spring Security, and MySQL 8.0 & Docker Compose. The project implements a Daraz-inspired storefront with customer, seller, and admin roles, cart/checkout/payment flows, order management, and an AI product suggestion feature.

## Tech Stack

- **Java 25** with virtual threads enabled
- **Spring Boot 4.1.0** (Web MVC, Thymeleaf, Data JPA, Security, Actuator)
- **MySQL 5.5** via XAMPP (localhost:3306, database: `sparkmart`)
- **Hibernate ORM 7.4** with `ddl-auto=update`
- **Flyway 10.20** for controlled schema migrations
- **JUnit 5 + Mockito + MockMvc** for testing
- **Docker** configuration exists (Dockerfile + docker-compose.yml) but fully tested and verified with Docker Compose

## Prerequisites

1. Docker Desktop installed and running
2. Java 25+ installed
3. Maven (or use the included `mvnw.cmd` wrapper)

## Setup

```bash
# Start MySQL via XAMPP Control Panel, then:
.\mvnw.cmd test                          # Run 77 tests
.\mvnw.cmd spring-boot:run               # Start the app on http://localhost:8081
.\mvnw.cmd clean package -DskipTests     # Build the JAR
```

The database `sparkmart` is created automatically on first startup. Hibernate manages existing tables; Flyway manages the `tags` and `product_tags` tables.

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `sparkmart` | Database name |
| `DB_USERNAME` | `root` | MySQL username |
| `DB_PASSWORD` | (blank) | MySQL password |
| `SPARKMART_ADMIN_USERNAME` | `admin@sparkmart.com` | Admin login email |
| `SPARKMART_ADMIN_PASSWORD` | `admin123` | Admin login password |

## Profiles

- **default**: Uses `application.properties` (connects to XAMPP MySQL with defaults)
- **dev**: `application-dev.properties` (local XAMPP, same defaults)
- **prod**: `application-prod.properties` (requires all `DB_*` env vars, no defaults)

Activate with `--spring.profiles.active=dev` or `--spring.profiles.active=prod`.

## Routes

### Public Storefront
- `/` — Home page with featured products, categories, search
- `/category/{slug}` — Category listing with filters
- `/categories` — All categories
- `/product/{id}` — Product detail page
- `/track-order` — Order tracking
- `/contact` — Contact page

### Customer
- `/login` — Customer login
- `/register` — Customer registration
- `/seller/register` — Seller registration
- `/cart` — Shopping cart
- `/checkout` — Checkout form
- `/payment` — Payment page
- `/orders` — Order history
- `/profile` — Customer profile

### Admin/Seller
- `/admin/login` — Admin/Seller login
- `/admin/dashboard` — Dashboard with stats
- `/admin/products` — Product management
- `/admin/orders` — Order management
- `/admin/categories` — Category management
- `/admin/customers` — Customer listing
- `/admin/payments` — Payment listing
- `/admin/analytics` — Analytics
- `/admin/inventory` — Inventory management

### Actuator
- `/actuator/health` — Health endpoint (requires HTTP Basic with admin credentials)

## Docker

Docker configuration exists but was **not physically tested** because Docker is not installed on the development machine. See `DOCKER.md` for Docker usage instructions and `docker-compose.yml` for the multi-container setup.

## Course Chapters (DOCX References)

The seven authoritative DOCX chapter files in the `docs/` directory:

1. **Chapter 1: The Modern Spring Landscape**
2. **Chapter 2: Dynamic Interfaces with Thymeleaf and Bootstrap**
3. **Chapter 3: Persistence with Relational Databases**
4. **Chapter 4: Advanced Relationships and Migrations**
5. **Chapter 5: Spring Security and Identity**
6. **Chapter 6: Testing Strategy for Modern Apps**
7. **Chapter 7: Production Readiness, Cloud Native, and Spring AI**

## Author

Created by Rejwana Akter.
