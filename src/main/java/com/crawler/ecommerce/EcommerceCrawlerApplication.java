package com.crawler.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@EnableJpaAuditing
@SpringBootApplication
public class EcommerceCrawlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceCrawlerApplication.class, args);
	}

}
