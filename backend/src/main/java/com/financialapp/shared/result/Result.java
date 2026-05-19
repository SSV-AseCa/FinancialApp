package com.financialapp.shared.result;

public interface Result<T> {
	T data();
	boolean isCorrect();
	String message(String message);
}
