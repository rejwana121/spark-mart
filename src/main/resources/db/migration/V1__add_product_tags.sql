-- V1: Adds the Tag entity and the Product<->Tag many-to-many join table.
-- Hibernate (ddl-auto=update) continues to own the pre-existing tables
-- (products, categories, customer_users, orders, addresses, etc.) exactly
-- as before; Flyway takes over specifically for this new schema addition.

CREATE TABLE IF NOT EXISTS tags (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS product_tags (
    product_id BIGINT NOT NULL,
    tag_id     BIGINT NOT NULL,
    PRIMARY KEY (product_id, tag_id),
    CONSTRAINT fk_product_tags_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_product_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id)
);
