package com.inspectuspro.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class FoundationDependenciesSmokeTest {

	@Test
	void shouldHaveWebMvcOnClasspath() {
		assertDoesNotThrow(() -> Class.forName("org.springframework.web.servlet.DispatcherServlet"));
	}

	@Test
	void shouldHaveSpringSecurityOnClasspath() {
		assertDoesNotThrow(() -> Class.forName("org.springframework.security.config.annotation.web.builders.HttpSecurity"));
	}

	@Test
	void shouldHaveJpaOnClasspath() {
		assertDoesNotThrow(() -> Class.forName("jakarta.persistence.Entity"));
	}

	@Test
	void shouldHaveFlywayOnClasspath() {
		assertDoesNotThrow(() -> Class.forName("org.flywaydb.core.Flyway"));
	}

	@Test
	void shouldHavePostgresDriverOnClasspath() {
		assertDoesNotThrow(() -> Class.forName("org.postgresql.Driver"));
	}
}

