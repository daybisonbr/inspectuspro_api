package com.inspectuspro.api.plan.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<CouponEntity, UUID> {
	Optional<CouponEntity> findByTenantIdAndCode(UUID tenantId, String code);
}

