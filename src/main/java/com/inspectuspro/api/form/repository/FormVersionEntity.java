package com.inspectuspro.api.form.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "form_versions")
public class FormVersionEntity {

	@Id
	public UUID id;

	@Column(name = "tenant_id", nullable = false)
	public UUID tenantId;

	@Column(name = "form_id", nullable = false)
	public UUID formId;

	@Lob
	@Column(nullable = false)
	public String schema;

	@Column(name = "created_at", nullable = false)
	public Instant createdAt;

	protected FormVersionEntity() {
	}

	public FormVersionEntity(UUID id, UUID tenantId, UUID formId, String schema, Instant createdAt) {
		this.id = id;
		this.tenantId = tenantId;
		this.formId = formId;
		this.schema = schema;
		this.createdAt = createdAt;
	}
}

