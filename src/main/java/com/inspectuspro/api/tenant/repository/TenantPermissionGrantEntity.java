package com.inspectuspro.api.tenant.repository;

import com.inspectuspro.api.tenant.enums.TenantPermission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "tenant_permission_grants")
public class TenantPermissionGrantEntity {

	@Id
	public UUID id;

	@Column(name = "tenant_id", nullable = false)
	public UUID tenantId;

	@Column(name = "user_id", nullable = false)
	public UUID userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public TenantPermission permission;

	protected TenantPermissionGrantEntity() {
	}

	public TenantPermissionGrantEntity(UUID id, UUID tenantId, UUID userId, TenantPermission permission) {
		this.id = id;
		this.tenantId = tenantId;
		this.userId = userId;
		this.permission = permission;
	}
}

