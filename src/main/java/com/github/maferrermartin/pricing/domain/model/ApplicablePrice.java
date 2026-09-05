package com.github.maferrermartin.pricing.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

public record ApplicablePrice(
		Long productId,
		Long brandId,
		Long priceList,
		LocalDateTime startDate,
		LocalDateTime endDate,
		BigDecimal price,
		Currency currency) {
}
