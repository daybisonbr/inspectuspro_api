package com.inspectuspro.api.form.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "forms")
public class FormEntity {

	@Id
	public UUID id;

	@Column(name = "tenant_id", nullable = false)
	public UUID tenantId;

	@Column(nullable = false)
	public String name;

	@Column(name = "published_version_id")
	public UUID publishedVersionId;

	protected FormEntity() {
	}

	public FormEntity(UUID id, UUID tenantId, String name, UUID publishedVersionId) {
		this.id = id;
		this.tenantId = tenantId;
		this.name = name;
		this.publishedVersionId = publishedVersionId;
	}
}

