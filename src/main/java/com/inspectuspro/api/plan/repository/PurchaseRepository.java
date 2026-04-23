package com.inspectuspro.api.plan.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<PurchaseEntity, UUID> {
}

