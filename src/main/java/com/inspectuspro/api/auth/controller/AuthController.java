package com.inspectuspro.api.auth.controller;

import com.inspectuspro.api.security.service.JwtService;
import com.inspectuspro.api.user.repository.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthController(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@PostMapping("/auth/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
		var user = users.findByEmail(request.email()).orElse(null);
		if (user == null || !passwordEncoder.matches(request.password(), user.passwordHash)) {
			return ResponseEntity.status(401).build();
		}
		return ResponseEntity.ok(new LoginResponse(jwtService.issueAccessToken(user.id)));
	}

	public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
	}

	public record LoginResponse(String accessToken) {
	}
}

