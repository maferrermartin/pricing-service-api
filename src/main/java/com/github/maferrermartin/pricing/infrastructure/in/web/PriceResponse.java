package com.github.maferrermartin.pricing.infrastructure.in.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.github.maferrermartin.pricing.domain.model.ApplicablePrice;

record PriceResponse(
		Long productId,
		Long brandId,
		Long priceList,
		LocalDateTime startDate,
		LocalDateTime endDate,
		BigDecimal price,
		String currency) {

	static PriceResponse from(ApplicablePrice applicablePrice) {
		return new PriceResponse(
				applicablePrice.productId(),
				applicablePrice.brandId(),
				applicablePrice.priceList(),
				applicablePrice.startDate(),
				applicablePrice.endDate(),
				applicablePrice.price(),
				applicablePrice.currency().getCurrencyCode());
	}

}
