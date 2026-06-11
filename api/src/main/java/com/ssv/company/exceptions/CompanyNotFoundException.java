package com.ssv.company.exceptions;

public class CompanyNotFoundException extends RuntimeException {

	public CompanyNotFoundException(String cik) {
		super("Company not found: " + cik);
	}
}
