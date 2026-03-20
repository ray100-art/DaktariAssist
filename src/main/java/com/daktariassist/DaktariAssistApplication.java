package com.daktariassist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DaktariAssistApplication {

	public static void main(String[] args) {
		SpringApplication.run(DaktariAssistApplication.class, args);
		System.out.println("========================================");
		System.out.println("  DaktariAssist is running!");
		System.out.println("  Open: http://localhost:8080");
		System.out.println("========================================");
	}
}