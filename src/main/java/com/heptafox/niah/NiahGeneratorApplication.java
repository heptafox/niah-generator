package com.heptafox.niah;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NiahGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(NiahGeneratorApplication.class, args);
	}

}
