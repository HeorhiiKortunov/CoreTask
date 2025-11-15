package com.example.maven;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CoreTaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoreTaskApplication.class, args);
	}

}
