package com.ssv.company.application.fake;

import java.util.List;

import com.ssv.company.application.EdgarSecFiling;
import com.ssv.edgar.application.EdgarCompanyFilingsParser;

public class FakeEdgarCompanyFilingsParser extends EdgarCompanyFilingsParser {

	public FakeEdgarCompanyFilingsParser() {
		super(null);
	}

	@Override
	public List<EdgarSecFiling> parse(String payload) {
		return List.of();
	}
}
