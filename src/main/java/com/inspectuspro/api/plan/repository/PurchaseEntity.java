package com.inspectuspro.api.plan.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "purchases")
public class PurchaseEntity {

	public enum Status {
		PENDING,
		PAID,
		CANCELLED
	}

	@Id
	public UUID id;

	@Column(name = "tenant_id", nullable = false)
	public UUID tenantId;

	@Column(name = "plan_id", nullable = false)
	public UUID planId;

	@Column(name = "coupon_code")
	public String couponCode;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public Status status;

	@Column(name = "created_at", nullable = false)
	public Instant createdAt;

	protected PurchaseEntity() {
	}

	public PurchaseEntity(UUID id, UUID tenantId, UUID planId, String couponCode, Status status, Instant createdAt) {
		this.id = id;
		this.tenantId = tenantId;
		this.planId = planId;
		this.couponCode = couponCode;
		this.status = status;
		this.createdAt = createdAt;
	}
}

