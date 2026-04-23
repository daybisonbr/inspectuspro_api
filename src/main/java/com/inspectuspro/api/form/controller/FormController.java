package com.inspectuspro.api.form.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspectuspro.api.common.id.IdGenerator;
import com.inspectuspro.api.form.repository.FormEntity;
import com.inspectuspro.api.form.repository.FormRepository;
import com.inspectuspro.api.form.repository.FormVersionEntity;
import com.inspectuspro.api.form.repository.FormVersionRepository;
import com.inspectuspro.api.form.repository.SubmissionEntity;
import com.inspectuspro.api.form.repository.SubmissionRepository;
import com.inspectuspro.api.tenant.enums.TenantPermission;
import com.inspectuspro.api.tenant.service.TenantAuthorizationService;
import jakarta.validation.constraints.NotBlank;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FormController {

	private final FormRepository forms;
	private final FormVersionRepository versions;
	private final SubmissionRepository submissions;
	private final TenantAuthorizationService authz;
	private final IdGenerator ids;
	private final Clock clock;

	public FormController(FormRepository forms, FormVersionRepository versions, SubmissionRepository submissions,
			TenantAuthorizationService authz, IdGenerator ids, Clock clock) {
		this.forms = forms;
		this.versions = versions;
		this.submissions = submissions;
		this.authz = authz;
		this.ids = ids;
		this.clock = clock;
	}

	@PostMapping("/forms")
	public ResponseEntity<CreateFormResponse> create(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
			@RequestBody CreateFormRequest request, Authentication auth) {
		var tId = parseTenantId(tenantId);
		if (tId == null) {
			return ResponseEntity.badRequest().build();
		}
		UUID userId = (UUID) auth.getPrincipal();
		if (!authz.can(tId, userId, TenantPermission.FORMS_WRITE)) {
			return ResponseEntity.status(403).build();
		}

		UUID formId = ids.newId();
		forms.save(new FormEntity(formId, tId, request.name(), null));
		return ResponseEntity.status(201).body(new CreateFormResponse(formId.toString()));
	}

	@GetMapping("/forms")
	public ResponseEntity<ListFormsResponse> list(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
			Authentication auth) {
		var tId = parseTenantId(tenantId);
		if (tId == null) {
			return ResponseEntity.badRequest().build();
		}
		UUID userId = (UUID) auth.getPrincipal();
		if (!authz.can(tId, userId, TenantPermission.FORMS_READ)) {
			return ResponseEntity.status(403).build();
		}

		var items = forms.findAllByTenantId(tId).stream()
				.map(f -> new FormItem(f.id.toString(), f.name, f.publishedVersionId == null ? null : f.publishedVersionId.toString()))
				.toList();
		return ResponseEntity.ok(new ListFormsResponse(items));
	}

	@PostMapping("/forms/{formId}/versions")
	public ResponseEntity<CreateVersionResponse> publish(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
			@PathVariable String formId, @RequestBody PublishVersionRequest request, Authentication auth) {
		var tId = parseTenantId(tenantId);
		if (tId == null) {
			return ResponseEntity.badRequest().build();
		}
		UUID userId = (UUID) auth.getPrincipal();
		if (!authz.can(tId, userId, TenantPermission.FORMS_WRITE)) {
			return ResponseEntity.status(403).build();
		}

		UUID fId = UUID.fromString(formId);
		var form = forms.findById(fId).orElse(null);
		if (form == null || !form.tenantId.equals(tId)) {
			return ResponseEntity.status(404).build();
		}

		UUID versionId = ids.newId();
		Instant now = clock.instant();
		versions.save(new FormVersionEntity(versionId, tId, fId, request.schema(), now));
		form.publishedVersionId = versionId;
		forms.save(form);
		return ResponseEntity.status(201).body(new CreateVersionResponse(versionId.toString()));
	}

	@PostMapping("/forms/{formId}/submissions")
	public ResponseEntity<CreateSubmissionResponse> submit(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
			@PathVariable String formId, @RequestBody SubmitRequest request, Authentication auth) {
		var tId = parseTenantId(tenantId);
		if (tId == null) {
			return ResponseEntity.badRequest().build();
		}
		UUID userId = (UUID) auth.getPrincipal();
		if (!authz.can(tId, userId, TenantPermission.SUBMISSIONS_CREATE)) {
			return ResponseEntity.status(403).build();
		}

		UUID fId = UUID.fromString(formId);
		var form = forms.findById(fId).orElse(null);
		if (form == null || !form.tenantId.equals(tId) || form.publishedVersionId == null) {
			return ResponseEntity.status(404).build();
		}

		String payloadJson;
		try {
			payloadJson = new ObjectMapper().writeValueAsString(request.payload());
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}

		UUID submissionId = ids.newId();
		submissions.save(new SubmissionEntity(submissionId, tId, fId, form.publishedVersionId, payloadJson, clock.instant()));
		return ResponseEntity.status(201).body(new CreateSubmissionResponse(submissionId.toString()));
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

	public record CreateFormRequest(@NotBlank String name) {
	}

	public record CreateFormResponse(String formId) {
	}

	public record FormItem(String formId, String name, String publishedVersionId) {
	}

	public record ListFormsResponse(java.util.List<FormItem> items) {
	}

	public record PublishVersionRequest(@NotBlank String schema) {
	}

	public record CreateVersionResponse(String versionId) {
	}

	public record SubmitRequest(Object payload) {
	}

	public record CreateSubmissionResponse(String submissionId) {
	}
}

