package com.inspectuspro.api.plan.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "plans")
public class PlanEntity {

	@Id
	public UUID id;

	@Column(name = "tenant_id", nullable = false)
	public UUID tenantId;

	@Column(nullable = false)
	public String name;

	@Column(nullable = false)
	public boolean active;

	protected PlanEntity() {
	}

	public PlanEntity(UUID id, UUID tenantId, String name, boolean active) {
		this.id = id;
		this.tenantId = tenantId;
		this.name = name;
		this.active = active;
	}
}

