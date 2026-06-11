package com.ssv.company.exceptions;

import java.util.UUID;

public class CompanyNotFoundException extends RuntimeException {

	public CompanyNotFoundException(String cik) {
		super("Company not found: " + cik);
	}
}
