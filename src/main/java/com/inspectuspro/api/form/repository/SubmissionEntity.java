package com.inspectuspro.api.form.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "submissions")
public class SubmissionEntity {

	@Id
	public UUID id;

	@Column(name = "tenant_id", nullable = false)
	public UUID tenantId;

	@Column(name = "form_id", nullable = false)
	public UUID formId;

	@Column(name = "form_version_id", nullable = false)
	public UUID formVersionId;

	@Lob
	@Column(nullable = false)
	public String payloadJson;

	@Column(name = "created_at", nullable = false)
	public Instant createdAt;

	protected SubmissionEntity() {
	}

	public SubmissionEntity(UUID id, UUID tenantId, UUID formId, UUID formVersionId, String payloadJson, Instant createdAt) {
		this.id = id;
		this.tenantId = tenantId;
		this.formId = formId;
		this.formVersionId = formVersionId;
		this.payloadJson = payloadJson;
		this.createdAt = createdAt;
	}
}

