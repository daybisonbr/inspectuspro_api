package com.inspectuspro.api.form;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class FormMvpFlowTest {

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
	void shouldCreateFormPublishAndSubmit() throws Exception {
		var client = HttpClient.newHttpClient();
		var mapper = new ObjectMapper();

		var userId = users.save(new UserEntity(UUID.randomUUID(), "f@acme.com", new BCryptPasswordEncoder().encode("pass123"))).id;
		var token = login(client, "f@acme.com", "pass123");
		assertThat(token).isNotBlank();

		var sanityReq = HttpRequest.newBuilder(URI.create(url("/tenants"))).header("Authorization", "Bearer " + token).GET()
				.build();
		var sanityRes = client.send(sanityReq, HttpResponse.BodyHandlers.ofString());
		assertThat(sanityRes.statusCode()).isEqualTo(200);

		var tenantId = UUID.randomUUID();
		tenants.save(new TenantEntity(tenantId, "Acme"));
		memberships.save(new MembershipEntity(UUID.randomUUID(), tenantId, userId, false));
		grants.save(new TenantPermissionGrantEntity(UUID.randomUUID(), tenantId, userId, TenantPermission.FORMS_WRITE));
		grants.save(new TenantPermissionGrantEntity(UUID.randomUUID(), tenantId, userId, TenantPermission.FORMS_READ));
		grants.save(new TenantPermissionGrantEntity(UUID.randomUUID(), tenantId, userId, TenantPermission.SUBMISSIONS_CREATE));

		// create form
		var createFormReq = HttpRequest.newBuilder(URI.create(url("/forms")))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + token)
				.header("X-Tenant-Id", tenantId.toString())
				.POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"Checklist\"}"))
				.build();
		var createFormRes = client.send(createFormReq, HttpResponse.BodyHandlers.ofString());
		assertThat(createFormRes.statusCode()).isEqualTo(201);
		var formId = mapper.readTree(createFormRes.body()).path("formId").asText();
		assertThat(formId).isNotBlank();

		// publish version
		var schema = "{\"type\":\"object\",\"properties\":{\"a\":{\"type\":\"string\"}}}";
		var publishReq = HttpRequest.newBuilder(URI.create(url("/forms/" + formId + "/versions")))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + token)
				.header("X-Tenant-Id", tenantId.toString())
				.POST(HttpRequest.BodyPublishers.ofString("{\"schema\":" + quote(schema) + "}"))
				.build();
		var publishRes = client.send(publishReq, HttpResponse.BodyHandlers.ofString());
		assertThat(publishRes.statusCode()).isEqualTo(201);
		var versionId = mapper.readTree(publishRes.body()).path("versionId").asText();
		assertThat(versionId).isNotBlank();

		// list forms (should include publishedVersionId)
		var listReq = HttpRequest.newBuilder(URI.create(url("/forms")))
				.header("Authorization", "Bearer " + token)
				.header("X-Tenant-Id", tenantId.toString())
				.GET()
				.build();
		var listRes = client.send(listReq, HttpResponse.BodyHandlers.ofString());
		assertThat(listRes.statusCode()).isEqualTo(200);
		JsonNode listJson = mapper.readTree(listRes.body());
		assertThat(listJson.path("items").isArray()).isTrue();

		// submit
		var submitReq = HttpRequest.newBuilder(URI.create(url("/forms/" + formId + "/submissions")))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + token)
				.header("X-Tenant-Id", tenantId.toString())
				.POST(HttpRequest.BodyPublishers.ofString("{\"payload\":{\"a\":\"x\"}}"))
				.build();
		var submitRes = client.send(submitReq, HttpResponse.BodyHandlers.ofString());
		assertThat(submitRes.statusCode()).isEqualTo(201);
		var submissionId = mapper.readTree(submitRes.body()).path("submissionId").asText();
		assertThat(submissionId).isNotBlank();
	}

	private String login(HttpClient client, String email, String password) throws Exception {
		var req = HttpRequest.newBuilder(URI.create(url("/auth/login")))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
				.build();
		var res = client.send(req, HttpResponse.BodyHandlers.ofString());
		assertThat(res.statusCode()).isEqualTo(200);
		return new ObjectMapper().readTree(res.body()).path("accessToken").asText();
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

	private static String quote(String s) {
		return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}
}

