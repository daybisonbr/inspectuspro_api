package com.inspectuspro.api.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.inspectuspro.api.user.repository.UserEntity;
import com.inspectuspro.api.user.repository.UserRepository;
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
class AuthorizationTest {

	@LocalServerPort
	int port;

	@Autowired
	UserRepository users;

	@Test
	void shouldRejectUnauthorizedRequest() throws Exception {
		var client = HttpClient.newHttpClient();
		var req = HttpRequest.newBuilder(URI.create(url("/tenants"))).GET().build();
		var res = client.send(req, HttpResponse.BodyHandlers.ofString());
		assertThat(res.statusCode()).isEqualTo(401);
	}

	@Test
	void shouldRejectInvalidToken() throws Exception {
		var client = HttpClient.newHttpClient();
		var req = HttpRequest.newBuilder(URI.create(url("/tenants"))).header("Authorization", "Bearer invalid").GET().build();
		var res = client.send(req, HttpResponse.BodyHandlers.ofString());
		assertThat(res.statusCode()).isEqualTo(401);
	}

	@Test
	void shouldAllowValidToken() throws Exception {
		var client = HttpClient.newHttpClient();
		var passwordEncoder = new BCryptPasswordEncoder();
		users.save(new UserEntity(UUID.randomUUID(), "a@a.com", passwordEncoder.encode("pass123")));

		var loginReq = HttpRequest.newBuilder(URI.create(url("/auth/login")))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\"email\":\"a@a.com\",\"password\":\"pass123\"}"))
				.build();
		var loginRes = client.send(loginReq, HttpResponse.BodyHandlers.ofString());
		assertThat(loginRes.statusCode()).isEqualTo(200);

		var token = extract(loginRes.body(), "accessToken");
		assertThat(token).isNotBlank();

		var req = HttpRequest.newBuilder(URI.create(url("/tenants"))).header("Authorization", "Bearer " + token).GET().build();
		var res = client.send(req, HttpResponse.BodyHandlers.ofString());
		assertThat(res.statusCode()).isEqualTo(200);
	}

	private String url(String path) {
		return "http://localhost:" + port + path;
	}

	private static String extract(String json, String field) {
		String needle = "\"" + field + "\":\"";
		int start = json.indexOf(needle);
		if (start < 0) {
			return "";
		}
		start += needle.length();
		int end = json.indexOf('"', start);
		if (end < 0) {
			return "";
		}
		return json.substring(start, end);
	}
}

