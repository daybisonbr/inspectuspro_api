package com.inspectuspro.api.presentation.tenant;

import com.inspectuspro.api.infra.persistence.membership.MembershipEntity;
import com.inspectuspro.api.infra.persistence.membership.MembershipJpaRepository;
import com.inspectuspro.api.infra.persistence.tenant.TenantEntity;
import com.inspectuspro.api.infra.persistence.tenant.TenantJpaRepository;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TenantController {

	private final TenantJpaRepository tenants;
	private final MembershipJpaRepository memberships;

	public TenantController(TenantJpaRepository tenants, MembershipJpaRepository memberships) {
		this.tenants = tenants;
		this.memberships = memberships;
	}

	@PostMapping("/tenants")
	public ResponseEntity<CreateTenantResponse> create(@RequestBody CreateTenantRequest request, Authentication auth) {
		UUID userId = (UUID) auth.getPrincipal();
		UUID tenantId = UUID.randomUUID();
		tenants.save(new TenantEntity(tenantId, request.name()));
		memberships.save(new MembershipEntity(UUID.randomUUID(), tenantId, userId, true));
		return ResponseEntity.status(201).body(new CreateTenantResponse(tenantId.toString()));
	}

	@GetMapping("/tenants")
	public ResponseEntity<ListTenantsResponse> list(Authentication auth) {
		UUID userId = (UUID) auth.getPrincipal();
		var membershipsForUser = memberships.findAllByUserId(userId);
		var items = membershipsForUser.stream()
				.map(m -> tenants.findById(m.tenantId).orElse(null))
				.filter(t -> t != null)
				.map(t -> new TenantItem(t.id.toString(), t.name))
				.toList();
		return ResponseEntity.ok(new ListTenantsResponse(items));
	}

	public record CreateTenantRequest(@NotBlank String name) {
	}

	public record CreateTenantResponse(String tenantId) {
	}

	public record TenantItem(String tenantId, String name) {
	}

	public record ListTenantsResponse(java.util.List<TenantItem> items) {
	}
}

