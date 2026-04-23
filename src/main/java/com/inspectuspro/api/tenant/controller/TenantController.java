package com.inspectuspro.api.tenant.controller;

import com.inspectuspro.api.common.id.IdGenerator;
import com.inspectuspro.api.tenant.repository.MembershipEntity;
import com.inspectuspro.api.tenant.repository.MembershipRepository;
import com.inspectuspro.api.tenant.repository.TenantEntity;
import com.inspectuspro.api.tenant.repository.TenantRepository;
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

	private final TenantRepository tenants;
	private final MembershipRepository memberships;
	private final IdGenerator ids;

	public TenantController(TenantRepository tenants, MembershipRepository memberships, IdGenerator ids) {
		this.tenants = tenants;
		this.memberships = memberships;
		this.ids = ids;
	}

	@PostMapping("/tenants")
	public ResponseEntity<CreateTenantResponse> create(@RequestBody CreateTenantRequest request, Authentication auth) {
		UUID userId = (UUID) auth.getPrincipal();
		UUID tenantId = ids.newId();
		tenants.save(new TenantEntity(tenantId, request.name()));
		memberships.save(new MembershipEntity(ids.newId(), tenantId, userId, true));
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

