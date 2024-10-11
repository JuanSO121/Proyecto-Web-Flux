package com.sanchez.jj.WebHook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WebHookEnigmaApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebHookEnigmaApplication.class, args);
	}

}
