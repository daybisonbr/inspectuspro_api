package com.inspectuspro.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspectuspro.api.infra.persistence.user.UserEntity;
import com.inspectuspro.api.infra.persistence.user.UserJpaRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthAndTenantFlowTest {

	@Autowired
	UserJpaRepository users;

	@LocalServerPort
	int port;

	@Test
	void shouldLoginAndCreateTenantAndListTenants() throws Exception {
		var client = HttpClient.newHttpClient();
		var objectMapper = new ObjectMapper();
		var passwordEncoder = new BCryptPasswordEncoder();
		var user = new UserEntity(UUID.randomUUID(), "owner@acme.com", passwordEncoder.encode("pass123"));
		users.save(user);

		var loginBody = """
				{"email":"owner@acme.com","password":"pass123"}
				""";

		var loginRequest = HttpRequest.newBuilder(URI.create(url("/auth/login")))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(loginBody))
				.build();
		var loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());
		assertThat(loginResponse.statusCode()).isEqualTo(200);

		JsonNode loginJson = objectMapper.readTree(loginResponse.body());
		String accessToken = loginJson.path("accessToken").asText();
		assertThat(accessToken).isNotBlank();

		var createTenantBody = """
				{"name":"Acme"}
				""";
		var createTenantRequest = HttpRequest.newBuilder(URI.create(url("/tenants")))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + accessToken)
				.POST(HttpRequest.BodyPublishers.ofString(createTenantBody))
				.build();
		var createTenantResponse = client.send(createTenantRequest, HttpResponse.BodyHandlers.ofString());
		assertThat(createTenantResponse.statusCode()).isEqualTo(201);

		JsonNode createTenantJson = objectMapper.readTree(createTenantResponse.body());
		String tenantId = createTenantJson.path("tenantId").asText();
		assertThat(tenantId).isNotBlank();

		var listTenantsRequest = HttpRequest.newBuilder(URI.create(url("/tenants")))
				.header("Authorization", "Bearer " + accessToken)
				.GET()
				.build();
		var listTenantsResponse = client.send(listTenantsRequest, HttpResponse.BodyHandlers.ofString());
		assertThat(listTenantsResponse.statusCode()).isEqualTo(200);

		JsonNode listJson = objectMapper.readTree(listTenantsResponse.body());
		assertThat(listJson.path("items").isArray()).isTrue();
		assertThat(listJson.path("items").get(0).path("tenantId").asText()).isNotBlank();
		assertThat(listJson.path("items").get(0).path("name").asText()).isEqualTo("Acme");
	}

	private String url(String path) {
		return "http://localhost:" + port + path;
	}
}

