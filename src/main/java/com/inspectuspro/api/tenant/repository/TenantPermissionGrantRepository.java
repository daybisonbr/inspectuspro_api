package com.inspectuspro.api.tenant.repository;

import com.inspectuspro.api.tenant.enums.TenantPermission;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantPermissionGrantRepository extends JpaRepository<TenantPermissionGrantEntity, UUID> {
	boolean existsByTenantIdAndUserIdAndPermission(UUID tenantId, UUID userId, TenantPermission permission);
}

