package com.inspectuspro.api.tenant.service;

import com.inspectuspro.api.tenant.enums.TenantPermission;
import com.inspectuspro.api.tenant.repository.MembershipRepository;
import com.inspectuspro.api.tenant.repository.TenantPermissionGrantRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TenantAuthorizationService {

	private final MembershipRepository memberships;
	private final TenantPermissionGrantRepository grants;

	public TenantAuthorizationService(MembershipRepository memberships, TenantPermissionGrantRepository grants) {
		this.memberships = memberships;
		this.grants = grants;
	}

	public boolean can(UUID tenantId, UUID userId, TenantPermission permission) {
		var membership = memberships.findAllByUserId(userId).stream().filter(m -> m.tenantId.equals(tenantId)).findFirst()
				.orElse(null);
		if (membership == null) {
			return false;
		}
		if (membership.isOwner) {
			return true;
		}
		return grants.existsByTenantIdAndUserIdAndPermission(tenantId, userId, permission);
	}
}

