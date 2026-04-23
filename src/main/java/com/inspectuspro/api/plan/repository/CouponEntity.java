package com.inspectuspro.api.plan.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "coupons")
public class CouponEntity {

	@Id
	public UUID id;

	@Column(name = "tenant_id", nullable = false)
	public UUID tenantId;

	@Column(nullable = false, unique = true)
	public String code;

	@Column(name = "percent_off", nullable = false)
	public int percentOff;

	@Column(nullable = false)
	public boolean active;

	protected CouponEntity() {
	}

	public CouponEntity(UUID id, UUID tenantId, String code, int percentOff, boolean active) {
		this.id = id;
		this.tenantId = tenantId;
		this.code = code;
		this.percentOff = percentOff;
		this.active = active;
	}
}

