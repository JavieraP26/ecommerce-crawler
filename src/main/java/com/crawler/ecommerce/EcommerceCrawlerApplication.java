package com.crawler.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.sql.DataSource;

@EnableJpaAuditing
@SpringBootApplication
public class EcommerceCrawlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceCrawlerApplication.class, args);
	}

}
