package com.inspectuspro.api.tenant.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<MembershipEntity, UUID> {
	List<MembershipEntity> findAllByUserId(UUID userId);
}

