package com.ssv.watchlist.domain;

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
@Table(name = "watchlist")
@Getter
@Setter
@NoArgsConstructor
public class WatchlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "investor_id", nullable = false)
    private UUID investorId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;
}
