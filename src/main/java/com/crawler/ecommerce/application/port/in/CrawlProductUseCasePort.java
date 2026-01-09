package com.crawler.ecommerce.application.port.in;

import java.util.List;

/**
 * Use case crawling producto individual.
 * Orquesta scraper → dedup → persistencia.
 */
public interface CrawlProductUseCasePort {

    /**
     * Crawlea producto desde URL.
     * scraper → validación → repository.save()
     */
    void crawlProduct(String productUrl);

    /**
     * Crawlea múltiples productos (test/debug).
     */
    void crawlProducts(List<String> productUrls);

}
