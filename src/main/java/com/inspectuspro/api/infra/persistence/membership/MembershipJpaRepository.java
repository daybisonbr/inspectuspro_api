package com.inspectuspro.api.infra.persistence.membership;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipJpaRepository extends JpaRepository<MembershipEntity, UUID> {
	List<MembershipEntity> findAllByUserId(UUID userId);
}

