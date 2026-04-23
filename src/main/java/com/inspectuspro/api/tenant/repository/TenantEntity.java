package com.inspectuspro.api.tenant.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class TenantEntity {

	@Id
	public UUID id;

	@Column(nullable = false)
	public String name;

	protected TenantEntity() {
	}

	public TenantEntity(UUID id, String name) {
		this.id = id;
		this.name = name;
	}
}

