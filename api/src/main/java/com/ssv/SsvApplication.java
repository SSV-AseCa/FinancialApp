package com.ssv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SsvApplication {

	public static void main(String[] args) {
		SpringApplication.run(SsvApplication.class, args);
	}

}
