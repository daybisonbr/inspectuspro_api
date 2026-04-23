package com.inspectuspro.api.plan.controller;

import com.inspectuspro.api.common.id.IdGenerator;
import com.inspectuspro.api.plan.repository.CouponEntity;
import com.inspectuspro.api.plan.repository.CouponRepository;
import com.inspectuspro.api.plan.repository.PlanEntity;
import com.inspectuspro.api.plan.repository.PlanRepository;
import com.inspectuspro.api.plan.repository.PurchaseEntity;
import com.inspectuspro.api.plan.repository.PurchaseRepository;
import com.inspectuspro.api.tenant.enums.TenantPermission;
import com.inspectuspro.api.tenant.service.TenantAuthorizationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Clock;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlanController {

	private final PlanRepository plans;
	private final CouponRepository coupons;
	private final PurchaseRepository purchases;
	private final TenantAuthorizationService authz;
	private final IdGenerator ids;
	private final Clock clock;

	public PlanController(PlanRepository plans, CouponRepository coupons, PurchaseRepository purchases,
			TenantAuthorizationService authz, IdGenerator ids, Clock clock) {
		this.plans = plans;
		this.coupons = coupons;
		this.purchases = purchases;
		this.authz = authz;
		this.ids = ids;
		this.clock = clock;
	}

	@PostMapping("/plans")
	public ResponseEntity<CreatePlanResponse> createPlan(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
			@RequestBody CreatePlanRequest request, Authentication auth) {
		var tId = parseTenantId(tenantId);
		if (tId == null) {
			return ResponseEntity.badRequest().build();
		}
		UUID userId = (UUID) auth.getPrincipal();
		if (!authz.can(tId, userId, TenantPermission.PLANS_WRITE)) {
			return ResponseEntity.status(403).build();
		}
		UUID planId = ids.newId();
		plans.save(new PlanEntity(planId, tId, request.name(), request.active()));
		return ResponseEntity.status(201).body(new CreatePlanResponse(planId.toString()));
	}

	@PostMapping("/coupons")
	public ResponseEntity<CreateCouponResponse> createCoupon(
			@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId, @RequestBody CreateCouponRequest request,
			Authentication auth) {
		var tId = parseTenantId(tenantId);
		if (tId == null) {
			return ResponseEntity.badRequest().build();
		}
		UUID userId = (UUID) auth.getPrincipal();
		if (!authz.can(tId, userId, TenantPermission.PLANS_WRITE)) {
			return ResponseEntity.status(403).build();
		}
		UUID couponId = ids.newId();
		coupons.save(new CouponEntity(couponId, tId, request.code(), request.percentOff(), true));
		return ResponseEntity.status(201).body(new CreateCouponResponse(couponId.toString()));
	}

	@PostMapping("/checkout")
	public ResponseEntity<CreateCheckoutResponse> checkout(
			@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId, @RequestBody CheckoutRequest request,
			Authentication auth) {
		var tId = parseTenantId(tenantId);
		if (tId == null) {
			return ResponseEntity.badRequest().build();
		}
		UUID userId = (UUID) auth.getPrincipal();
		if (!authz.can(tId, userId, TenantPermission.PLANS_READ)) {
			return ResponseEntity.status(403).build();
		}

		UUID planId = UUID.fromString(request.planId());
		var plan = plans.findById(planId).orElse(null);
		if (plan == null || !plan.tenantId.equals(tId) || !plan.active) {
			return ResponseEntity.status(404).build();
		}

		if (request.couponCode() != null && !request.couponCode().isBlank()) {
			var coupon = coupons.findByTenantIdAndCode(tId, request.couponCode()).orElse(null);
			if (coupon == null || !coupon.active) {
				return ResponseEntity.status(404).build();
			}
		}

		UUID purchaseId = ids.newId();
		purchases.save(new PurchaseEntity(purchaseId, tId, planId, request.couponCode(), PurchaseEntity.Status.PENDING,
				clock.instant()));
		return ResponseEntity.status(201).body(new CreateCheckoutResponse(purchaseId.toString()));
	}

	@PostMapping("/checkout/{purchaseId}/simulate-pay")
	public ResponseEntity<Void> simulatePay(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
			@PathVariable String purchaseId, Authentication auth) {
		var tId = parseTenantId(tenantId);
		if (tId == null) {
			return ResponseEntity.badRequest().build();
		}
		UUID userId = (UUID) auth.getPrincipal();
		if (!authz.can(tId, userId, TenantPermission.PLANS_WRITE)) {
			return ResponseEntity.status(403).build();
		}

		var pId = UUID.fromString(purchaseId);
		var purchase = purchases.findById(pId).orElse(null);
		if (purchase == null || !purchase.tenantId.equals(tId)) {
			return ResponseEntity.status(404).build();
		}
		purchase.status = PurchaseEntity.Status.PAID;
		purchases.save(purchase);
		return ResponseEntity.ok().build();
	}

	private static UUID parseTenantId(String tenantId) {
		if (tenantId == null || tenantId.isBlank()) {
			return null;
		}
		try {
			return UUID.fromString(tenantId);
		} catch (Exception e) {
			return null;
		}
	}

	public record CreatePlanRequest(@NotBlank String name, boolean active) {
	}

	public record CreatePlanResponse(String planId) {
	}

	public record CreateCouponRequest(@NotBlank String code, @Min(1) @Max(100) int percentOff) {
	}

	public record CreateCouponResponse(String couponId) {
	}

	public record CheckoutRequest(@NotBlank String planId, String couponCode) {
	}

	public record CreateCheckoutResponse(String purchaseId) {
	}
}

