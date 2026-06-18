package com.ssv.cache.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A single cached upstream response, keyed by (provider, cacheKey) and valid
 * until {@code expiresAt}. Stored in the DB so the cache is durable and shared.
 */
@Entity
@Table(name = "cached_response", uniqueConstraints = @UniqueConstraint(name = "uq_cached_response_provider_key", columnNames = {
		"provider", "cache_key"}), indexes = @Index(name = "idx_cached_response_expires_at", columnList = "expires_at"))
@Getter
@NoArgsConstructor
public class CachedResponse {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 20)
	private String provider;

	@Column(name = "cache_key", nullable = false, length = 512)
	private String cacheKey;

	@Column(nullable = false, columnDefinition = "text")
	private String payload;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "created_at", updatable = false)
	private Instant createdAt = Instant.now();

	public CachedResponse(String provider, String cacheKey, String payload, Instant expiresAt) {
		this.provider = provider;
		this.cacheKey = cacheKey;
		this.payload = payload;
		this.expiresAt = expiresAt;
	}

	public boolean isFresh(Instant now) {
		return expiresAt.isAfter(now);
	}

	public void refresh(String newPayload, Instant newExpiresAt) {
		this.payload = newPayload;
		this.expiresAt = newExpiresAt;
	}
}
