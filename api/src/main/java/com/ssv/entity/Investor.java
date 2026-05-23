package com.ssv.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "investor")
@Getter
@Setter
@NoArgsConstructor
public class Investor {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "auth0_sub", unique = true, nullable = false)
	private String auth0Sub;

	@Column(name = "created_at", updatable = false)
	private Instant createdAt = Instant.now();
}
