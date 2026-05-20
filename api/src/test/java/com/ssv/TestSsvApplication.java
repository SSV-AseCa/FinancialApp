package com.ssv;

import com.financialapp.TestcontainersConfiguration;
import org.springframework.boot.SpringApplication;

public class TestSsvApplication {

	public static void main(String[] args) {
		SpringApplication.from(com.financialapp.SsvApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
