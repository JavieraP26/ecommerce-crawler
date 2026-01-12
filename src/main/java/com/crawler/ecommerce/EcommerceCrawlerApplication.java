package com.crawler.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Clase principal de la aplicación E-commerce Crawler.
 * Habilita auditoría JPA para uso de @CreatedDate y @LastModifiedDate en entidades.
 */
@SpringBootApplication
@EnableJpaAuditing
public class EcommerceCrawlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceCrawlerApplication.class, args);
	}

}
