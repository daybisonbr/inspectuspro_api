package com.inspectuspro.api.security.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final SecretKey key;
	private final Clock clock;

	public JwtService(@Value("${app.security.jwt.secret}") String secret, Clock clock) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.clock = clock;
	}

	public String issueAccessToken(UUID userId) {
		Instant now = clock.instant();
		return Jwts.builder()
				.subject(userId.toString())
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(Duration.ofHours(6))))
				.signWith(key)
				.compact();
	}

	public UUID verifyAndGetUserId(String token) {
		String subject = Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
		return UUID.fromString(subject);
	}
}

