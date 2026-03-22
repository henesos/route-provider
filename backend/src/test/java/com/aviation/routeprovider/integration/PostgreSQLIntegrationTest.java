package com.aviation.routeprovider.integration;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for integration tests using a shared Testcontainers PostgreSQL instance.
 *
 * SINGLETON CONTAINER PATTERN:
 * The container is started ONCE for the entire test suite via a static initializer block,
 * not per test class. This prevents the following race condition:
 *
 *   1. AuthIntegrationTest starts → Testcontainers starts postgres on port X
 *   2. Spring creates ApplicationContext, stores datasource URL (localhost:X) in context cache
 *   3. AuthIntegrationTest finishes → @Testcontainers stops the container (port X is gone)
 *   4. LocationIntegrationTest starts → Spring reuses the CACHED context (still pointing to port X)
 *   5. HikariCP tries to connect to localhost:X → "Connection refused" → pool exhaustion → 20s timeout
 *
 * With the singleton pattern the container lives for the entire JVM lifetime.
 * Testcontainers registers a Ryuk shutdown hook automatically, so cleanup is handled.
 *
 * NOTE: @Testcontainers and @Container annotations are intentionally removed.
 * Those annotations tie container lifecycle to the test class, which breaks
 * Spring's context caching when used with a shared base class.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class PostgreSQLIntegrationTest {

    static final PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("routeprovider_test")
            .withUsername("test")
            .withPassword("test");
        // Start once — Testcontainers Ryuk will stop it when the JVM exits
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect",
            () -> "org.hibernate.dialect.PostgreSQLDialect");
    }
}
