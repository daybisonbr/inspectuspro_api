package com.inspectuspro.api.form.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormVersionRepository extends JpaRepository<FormVersionEntity, UUID> {
	List<FormVersionEntity> findAllByTenantIdAndFormId(UUID tenantId, UUID formId);
}

