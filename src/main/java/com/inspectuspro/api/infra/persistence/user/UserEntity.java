package com.inspectuspro.api.infra.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

	@Id
	public UUID id;

	@Column(nullable = false, unique = true)
	public String email;

	@Column(name = "password_hash", nullable = false)
	public String passwordHash;

	protected UserEntity() {
	}

	public UserEntity(UUID id, String email, String passwordHash) {
		this.id = id;
		this.email = email;
		this.passwordHash = passwordHash;
	}
}

