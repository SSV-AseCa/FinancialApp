package com.ssv;

import org.springframework.boot.SpringApplication;

public class TestSsvApplication {

	public static void main(String[] args) {
		SpringApplication.from(SsvApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
