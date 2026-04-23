package com.inspectuspro.api.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import com.inspectuspro.api.tenant.enums.TenantPermission;
import com.inspectuspro.api.tenant.repository.MembershipEntity;
import com.inspectuspro.api.tenant.repository.MembershipRepository;
import com.inspectuspro.api.tenant.repository.TenantEntity;
import com.inspectuspro.api.tenant.repository.TenantPermissionGrantEntity;
import com.inspectuspro.api.tenant.repository.TenantPermissionGrantRepository;
import com.inspectuspro.api.tenant.repository.TenantRepository;
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
class TenantMembersAuthorizationTest {

	@LocalServerPort
	int port;

	@Autowired
	UserRepository users;

	@Autowired
	TenantRepository tenants;

	@Autowired
	MembershipRepository memberships;

	@Autowired
	TenantPermissionGrantRepository grants;

	@Test
	void shouldRequireTenantHeader() throws Exception {
		var client = HttpClient.newHttpClient();
		createUser("u1@acme.com", "pass123");
		var token = loginAs(client, "u1@acme.com", "pass123");

		var req = HttpRequest.newBuilder(URI.create(url("/tenant/members")))
				.header("Authorization", "Bearer " + token)
				.GET()
				.build();
		var res = client.send(req, HttpResponse.BodyHandlers.ofString());
		assertThat(res.statusCode()).isEqualTo(400);
	}

	@Test
	void shouldReturn403WhenNoPermission() throws Exception {
		var client = HttpClient.newHttpClient();
		var userId = createUser("u2@acme.com", "pass123");
		var token = loginAs(client, "u2@acme.com", "pass123");

		var tenantId = UUID.randomUUID();
		tenants.save(new TenantEntity(tenantId, "Acme"));
		memberships.save(new MembershipEntity(UUID.randomUUID(), tenantId, userId, false));

		var req = HttpRequest.newBuilder(URI.create(url("/tenant/members")))
				.header("Authorization", "Bearer " + token)
				.header("X-Tenant-Id", tenantId.toString())
				.GET()
				.build();
		var res = client.send(req, HttpResponse.BodyHandlers.ofString());
		assertThat(res.statusCode()).isEqualTo(403);
	}

	@Test
	void shouldReturn200WhenGranted() throws Exception {
		var client = HttpClient.newHttpClient();
		var userId = createUser("u3@acme.com", "pass123");
		var token = loginAs(client, "u3@acme.com", "pass123");

		var tenantId = UUID.randomUUID();
		tenants.save(new TenantEntity(tenantId, "Acme"));
		memberships.save(new MembershipEntity(UUID.randomUUID(), tenantId, userId, false));
		grants.save(new TenantPermissionGrantEntity(UUID.randomUUID(), tenantId, userId, TenantPermission.USERS_READ));

		var req = HttpRequest.newBuilder(URI.create(url("/tenant/members")))
				.header("Authorization", "Bearer " + token)
				.header("X-Tenant-Id", tenantId.toString())
				.GET()
				.build();
		var res = client.send(req, HttpResponse.BodyHandlers.ofString());
		assertThat(res.statusCode()).isEqualTo(200);
	}

	private UUID createUser(String email, String rawPassword) {
		var encoder = new BCryptPasswordEncoder();
		var entity = new UserEntity(UUID.randomUUID(), email, encoder.encode(rawPassword));
		return users.save(entity).id;
	}

	private String loginAs(HttpClient client, String email, String password) throws Exception {
		var loginReq = HttpRequest.newBuilder(URI.create(url("/auth/login")))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
				.build();
		var loginRes = client.send(loginReq, HttpResponse.BodyHandlers.ofString());
		assertThat(loginRes.statusCode()).isEqualTo(200);
		return extract(loginRes.body(), "accessToken");
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

