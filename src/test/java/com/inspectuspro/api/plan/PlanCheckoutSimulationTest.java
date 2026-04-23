package com.inspectuspro.api.plan;

import static org.assertj.core.api.Assertions.assertThat;

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
class PlanCheckoutSimulationTest {

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
	void shouldCreatePlanCouponAndSimulateCheckoutPayment() throws Exception {
		var client = HttpClient.newHttpClient();
		var mapper = new ObjectMapper();

		var userId = users.save(new UserEntity(UUID.randomUUID(), "p@acme.com", new BCryptPasswordEncoder().encode("pass123"))).id;
		var token = login(client, "p@acme.com", "pass123");
		assertThat(token).isNotBlank();

		var sanityReq = HttpRequest.newBuilder(URI.create(url("/tenants"))).header("Authorization", "Bearer " + token).GET()
				.build();
		var sanityRes = client.send(sanityReq, HttpResponse.BodyHandlers.ofString());
		assertThat(sanityRes.statusCode()).isEqualTo(200);

		var tenantId = UUID.randomUUID();
		tenants.save(new TenantEntity(tenantId, "Acme"));
		memberships.save(new MembershipEntity(UUID.randomUUID(), tenantId, userId, false));
		grants.save(new TenantPermissionGrantEntity(UUID.randomUUID(), tenantId, userId, TenantPermission.PLANS_WRITE));
		grants.save(new TenantPermissionGrantEntity(UUID.randomUUID(), tenantId, userId, TenantPermission.PLANS_READ));

		var createPlanReq = HttpRequest.newBuilder(URI.create(url("/plans")))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + token)
				.header("X-Tenant-Id", tenantId.toString())
				.POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"Pro\",\"active\":true}"))
				.build();
		var createPlanRes = client.send(createPlanReq, HttpResponse.BodyHandlers.ofString());
		assertThat(createPlanRes.statusCode()).isEqualTo(201);
		var planId = mapper.readTree(createPlanRes.body()).path("planId").asText();
		assertThat(planId).isNotBlank();

		var createCouponReq = HttpRequest.newBuilder(URI.create(url("/coupons")))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + token)
				.header("X-Tenant-Id", tenantId.toString())
				.POST(HttpRequest.BodyPublishers.ofString("{\"code\":\"EVENT10\",\"percentOff\":10}"))
				.build();
		var createCouponRes = client.send(createCouponReq, HttpResponse.BodyHandlers.ofString());
		assertThat(createCouponRes.statusCode()).isEqualTo(201);

		var checkoutReq = HttpRequest.newBuilder(URI.create(url("/checkout")))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + token)
				.header("X-Tenant-Id", tenantId.toString())
				.POST(HttpRequest.BodyPublishers.ofString("{\"planId\":\"" + planId + "\",\"couponCode\":\"EVENT10\"}"))
				.build();
		var checkoutRes = client.send(checkoutReq, HttpResponse.BodyHandlers.ofString());
		assertThat(checkoutRes.statusCode()).withFailMessage("status=%s body=%s", checkoutRes.statusCode(), checkoutRes.body())
				.isEqualTo(201);
		var purchaseId = mapper.readTree(checkoutRes.body()).path("purchaseId").asText();
		assertThat(purchaseId).isNotBlank();

		var payReq = HttpRequest.newBuilder(URI.create(url("/checkout/" + purchaseId + "/simulate-pay")))
				.header("Authorization", "Bearer " + token)
				.header("X-Tenant-Id", tenantId.toString())
				.POST(HttpRequest.BodyPublishers.noBody())
				.build();
		var payRes = client.send(payReq, HttpResponse.BodyHandlers.ofString());
		assertThat(payRes.statusCode()).isEqualTo(200);
	}

	private String login(HttpClient client, String email, String password) throws Exception {
		var req = HttpRequest.newBuilder(URI.create(url("/auth/login")))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
				.build();
		var res = client.send(req, HttpResponse.BodyHandlers.ofString());
		assertThat(res.statusCode()).isEqualTo(200);
		return new ObjectMapper().readTree(res.body()).path("accessToken").asText().trim();
	}

	private String url(String path) {
		return "http://localhost:" + port + path;
	}
}

