package com.inspectuspro.api.tenant.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "memberships")
public class MembershipEntity {

	@Id
	public UUID id;

	@Column(name = "tenant_id", nullable = false)
	public UUID tenantId;

	@Column(name = "user_id", nullable = false)
	public UUID userId;

	@Column(name = "is_owner", nullable = false)
	public boolean isOwner;

	protected MembershipEntity() {
	}

	public MembershipEntity(UUID id, UUID tenantId, UUID userId, boolean isOwner) {
		this.id = id;
		this.tenantId = tenantId;
		this.userId = userId;
		this.isOwner = isOwner;
	}
}

