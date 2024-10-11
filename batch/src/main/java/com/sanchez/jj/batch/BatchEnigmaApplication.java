package com.sanchez.jj.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class BatchEnigmaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchEnigmaApplication.class, args);
	}

}
