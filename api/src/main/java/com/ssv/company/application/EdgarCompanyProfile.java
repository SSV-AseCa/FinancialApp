package com.ssv.company.application;

/**
 * Company identity extracted from an EDGAR submissions document: the legal
 * {@code name} and primary trading {@code symbol} (ticker). Used to materialize
 * a {@link com.ssv.company.domain.Company} the first time a CIK is requested.
 */
public record EdgarCompanyProfile(String name, String symbol) {
}
