package com.ssv.cache.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssv.cache.domain.CachedResponse;

public interface CachedResponseRepository extends JpaRepository<CachedResponse, UUID> {

	Optional<CachedResponse> findByProviderAndCacheKey(String provider, String cacheKey);
}
