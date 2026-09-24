package com.royalpearl.hotel;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for every integration test.
 * Spins up a single PostgreSQL container (reused across all subclasses via
 * the static field) and wires Spring Boot against it.
 *
 * Flyway migrations run automatically on context startup, giving each test
 * a fully-migrated, seeded database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("royalpearl_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);   // reuse container between test classes

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        // Always use dev profile for tests (verbose logging, no prod restrictions)
        registry.add("spring.profiles.active",     () -> "test");
        // Deterministic JWT secret for tests
        registry.add("app.jwt.secret",
                     () -> "test-secret-key-at-least-32-chars-long-for-hs256!");
        registry.add("app.admin.allowlist",        () -> "admin@test.com");
        registry.add("app.cors.allowed-origins",   () -> "http://localhost:3000");
    }
}
