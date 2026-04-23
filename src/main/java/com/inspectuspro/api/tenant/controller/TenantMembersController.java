package com.inspectuspro.api.tenant.controller;

import com.inspectuspro.api.tenant.enums.TenantPermission;
import com.inspectuspro.api.tenant.service.TenantAuthorizationService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TenantMembersController {

	private final TenantAuthorizationService authz;

	public TenantMembersController(TenantAuthorizationService authz) {
		this.authz = authz;
	}

	@GetMapping("/tenant/members")
	public ResponseEntity<ListMembersResponse> listMembers(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
			Authentication auth) {
		if (tenantId == null || tenantId.isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		UUID userId = (UUID) auth.getPrincipal();
		UUID tId = UUID.fromString(tenantId);

		if (!authz.can(tId, userId, TenantPermission.USERS_READ)) {
			return ResponseEntity.status(403).build();
		}

		return ResponseEntity.ok(new ListMembersResponse(java.util.List.of()));
	}

	public record ListMembersResponse(java.util.List<Object> items) {
	}
}

