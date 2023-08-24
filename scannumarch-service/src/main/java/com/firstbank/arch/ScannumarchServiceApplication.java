package com.firstbank.arch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class ScannumarchServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScannumarchServiceApplication.class, args);
	}

}
