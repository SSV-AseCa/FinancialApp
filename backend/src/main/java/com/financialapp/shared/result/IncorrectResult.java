package com.financialapp.shared.result;

public class IncorrectResult<T> implements Result<T> {
	@Override
	public T data() {
		return null;
	}

	@Override
	public boolean isCorrect() {
		return false;
	}

	@Override
	public String message(String message) {
		return message;
	}
}
