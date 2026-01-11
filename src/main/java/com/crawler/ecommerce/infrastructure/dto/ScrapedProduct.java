package com.crawler.ecommerce.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ScrapedProduct {
    String sku;
    String name;
    BigDecimal currentPrice;
    BigDecimal previousPrice;
    List<String> images;
    boolean available;
    String sourceUrl;
    String source;
}
