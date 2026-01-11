package com.crawler.ecommerce.application.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class ScrapedProduct {
    String sku, name, sourceUrl, source;
    BigDecimal currentPrice, previousPrice;
    List<String> images;
    boolean available;
}
