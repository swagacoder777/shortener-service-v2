package com.example.shortener_service_v2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
@SpringBootApplication
@EnableCaching
public class ShortenerServiceV2Application {

	public static void main(String[] args) {
		SpringApplication.run(ShortenerServiceV2Application.class, args);
	}

}
