# Chapters 1-7 Implementation Evidence

This document maps each course chapter to the specific files, features, and implementation details in the Spark Mart project.

---

## Chapter 1: Spring Boot Fundamentals

**Status: Fully Implemented**

| Topic | Implementation |
|---|---|
| Spring Boot application entry point | `SparkMartApplication.java` |
| Application properties | `application.properties`, `application-dev.properties`, `application-prod.properties` |
| Embedded Tomcat server | `server.port=8081` in properties |
| Maven build & packaging | `pom.xml` with `spring-boot-maven-plugin` |
| Spring profiles | Dev and prod profiles with environment variable overrides |
| Project structure | Multi-package layout: admin, cart, catalog, checkout, config, customer, order, security, seller |

---

## Chapter 2: Spring MVC & Thymeleaf

**Status: Fully Implemented**

| Topic | Implementation |
|---|---|
| `@Controller` route handling | 7 controllers: `StorefrontController`, `CustomerAuthController`, `AdminController`, `CartController`, `CheckoutController`, `OrderController`, `AddressController` |
| `@GetMapping` / `@PostMapping` | ~55 handler methods across all controllers |
| Thymeleaf fragments | `fragments/site.html` (5 fragments), `fragments/admin.html` (2 fragments), `fragments/product-card.html` (1 fragment) |
| `th:each` iteration | Used in 19 templates for products, categories, orders, cart items, payments, addresses |
| `th:object` / `th:field` form binding | `checkout.html` (CheckoutForm), `admin/product-form.html` (ProductForm) |
| `th:action` form submissions | 17 templates with Thymeleaf URL expressions |
| `th:if` / `th:unless` conditionals | 17 templates with conditional rendering |
| `th:replace` template composition | All 15 page templates compose fragments via `th:replace` |
| Bootstrap 5 integration | Used in admin product form for tag multi-select |

---

## Chapter 3: Virtual Threads & Concurrency

**Status: Implemented via Configuration**

| Topic | Implementation |
|---|---|
| Virtual threads enabled | `spring.threads.virtual.enabled=true` in `application.properties` |
| Java 25 runtime | `java.version` set to 25 in `pom.xml` |
| Dockerfile JDK 25 | `eclipse-temurin:25-jdk` (build) and `eclipse-temurin:25-jre` (runtime) |
| Non-blocking design | Controllers and services use synchronous blocking style compatible with virtual threads |

---

## Chapter 4: Data Access with Spring Data JPA & Flyway

**Status: Mostly Implemented (Product-Category migration deferred)**

### JPA Entities (8 entities + 2 embeddables)

| Entity | Table | Key Features |
|---|---|---|
| `Product` | `products` | `@ManyToMany` with `Tag`, `@Id` manual assignment |
| `Category` | `categories` | String `@Id` (slug) |
| `Tag` | `tags` | `@ManyToMany(mappedBy="tags")` inverse side |
| `CustomerUser` | `customers` | BCrypt password, role field, seller fields |
| `OrderRecord` | `orders` | `@ElementCollection` for `OrderLine`, string `@Id` |
| `PaymentRecord` | `payments` | Linked to order by orderNumber |
| `CartRecord` | `carts` | `@ElementCollection` for `CartLine` |
| `Address` | `addresses` | Linked to customer by customerId |
| `Seller` | `sellers` | Linked to customer by customerId |
| `CartLine` | `cart_items` | `@Embeddable` |
| `OrderLine` | `order_items` | `@Embeddable` with snapshot fields |

### Repositories (9 interfaces)

| Repository | Custom Methods |
|---|---|
| `ProductRepository` | `findByCategorySlug`, `findTopByOrderByIdDesc` |
| `CategoryRepository` | _(inherited)_ |
| `TagRepository` | _(inherited)_ |
| `CustomerRepository` | `findByEmailIgnoreCase` |
| `OrderRepository` | `findByCustomerIdOrderByCreatedAtDesc` |
| `PaymentRepository` | `findByOrderNumber` |
| `CartRepository` | `findByCustomerId` |
| `AddressRepository` | `findByCustomerId` |
| `SellerRepository` | `findByCustomerId` |

### Flyway

| Topic | Implementation |
|---|---|
| Flyway dependency | `flyway-core` 10.20.1 + `flyway-mysql` 10.20.1 |
| Migration file | `db/migration/V1__add_product_tags.sql` — creates `tags` and `product_tags` tables |
| Baseline-on-migrate | `spring.flyway.baseline-on-migrate=true` adopted existing schema |

### Deferred

- **Product-Category `@ManyToOne` migration**: The existing `categorySlug` string reference was not converted to a JPA relationship to avoid schema migration risk.

---

## Chapter 5: Spring Security

**Status: Implemented**

| Topic | Implementation |
|---|---|
| `SecurityFilterChain` bean | `SecurityConfig.java` — Actuator endpoints require `ROLE_ADMIN` |
| BCrypt password encoding | `BCryptPasswordEncoder` bean used by `AdminService` and `CustomerService` |
| HTTP Basic for Actuator | Configured in `SecurityFilterChain` |
| CSRF disabled for Actuator | `csrf.ignoringRequestMatchers("/actuator/**")` |
| Session-based auth (hand-rolled) | `AdminAuthenticationInterceptor` checks `sparkMartAdmin` session attribute |
| Customer auth interceptor | `CustomerAuthenticationInterceptor` checks `sparkMartCustomerId` session attribute |
| Role isolation | Admin login clears customer/seller sessions; customer login clears admin/seller sessions |
| Seller role upgrade | Customer accounts can become sellers via `becomeSeller()` |

---

## Chapter 6: Testing

**Status: Implemented**

### Test Summary: 77 tests, 0 failures

| Test Class | Tests | Type |
|---|---|---|
| `SparkMartApplicationTests` | 1 | `@SpringBootTest` (full context) |
| `AdminServiceTest` | 3 | Unit test (BCrypt auth) |
| `AdminControllerRoutingTest` | 10 | MockMvc standalone (routing, login, logout) |
| `CustomerAuthControllerLoginTest` | 3 | Unit test (login flow, session isolation) |
| `CustomerAuthControllerRoutingTest` | 6 | MockMvc standalone (login, register, logout) |
| `CustomerServiceValidationTest` | 9 | Unit test (registration, authentication) |
| `ShoppingCartTest` | 12 | Unit test (add, update, remove, totals) |
| `CartControllerRoutingTest` | 6 | MockMvc standalone (add, buy-now, update, remove) |
| `CheckoutFormTest` | 9 | Unit test (form validation) |
| `CheckoutControllerValidationTest` | 5 | MockMvc standalone (redirects, empty cart) |
| `OrderServiceTest` | 4 | Unit test (fulfillment status transitions) |
| `SessionAuthenticationTest` | 6 | Unit test (admin/seller isolation) |
| `CustomerAuthenticationInterceptorTest` | 3 | Unit test (customer interceptor) |

### Testing Approach

- Pure unit tests for business logic (no Spring context)
- Standalone MockMvc for controller routing (no database)
- `@SpringBootTest` for full context load (connects to real MySQL)
- No H2, Testcontainers, or Docker-dependent tests

---

## Chapter 7: Observability, Docker & Deployment

**Status: Partially Implemented**

### Actuator (Implemented)

| Topic | Implementation |
|---|---|
| Spring Boot Actuator dependency | `spring-boot-starter-actuator` in `pom.xml` |
| Health endpoint | `/actuator/health` |
| Metrics endpoint | `/actuator/metrics` |
| Security | HTTP Basic required for `/actuator/**` |
| Configuration | `management.endpoints.web.exposure.include=health,metrics` |

### Docker (Configuration Exists, Not Tested)

| Topic | Implementation |
|---|---|
| Dockerfile | Multi-stage: `eclipse-temurin:25-jdk` build, `eclipse-temurin:25-jre` runtime |
| docker-compose.yml | MySQL 8.0 + app with healthcheck dependency |
| Dockerfile tested | **No** — Docker is not installed on the development machine |
| docker-compose tested | **No** — same reason |

### Spring AI (Deferred)

- Spring AI is **not implemented**. The project has an AI suggestion feature (`CatalogService.aiSuggestionFor()`) that generates deterministic product suggestions using string hashing, not a real AI/ML model.
- Spring AI was documented as future work in the course chapters but is explicitly deferred from this implementation.

---

## Summary Table

| Chapter | Title | Status |
|---|---|---|
| 1 | Spring Boot Fundamentals | Fully implemented |
| 2 | Spring MVC & Thymeleaf | Fully implemented |
| 3 | Virtual Threads & Concurrency | Implemented via configuration |
| 4 | Data Access (JPA + Flyway) | Mostly implemented (Product-Category deferred) |
| 5 | Spring Security | Fully implemented |
| 6 | Testing | 77 tests, all passing |
| 7 | Observability, Docker & Deployment | Actuator implemented; Docker configured but not tested |
