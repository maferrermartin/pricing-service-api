package com.github.maferrermartin.pricing.infrastructure.in.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.github.maferrermartin.pricing.domain.model.ApplicablePrice;

record PriceResponse(
		@Schema(description = "openapi.schema.price-response.product-id", example = "35455") Long productId,
		@Schema(description = "openapi.schema.price-response.brand-id", example = "1") Long brandId,
		@Schema(description = "openapi.schema.price-response.price-list", example = "1") Long priceList,
		@Schema(description = "openapi.schema.price-response.start-date") LocalDateTime startDate,
		@Schema(description = "openapi.schema.price-response.end-date") LocalDateTime endDate,
		@Schema(description = "openapi.schema.price-response.price", example = "35.50") BigDecimal price,
		@Schema(description = "openapi.schema.price-response.currency", example = "EUR") String currency) {

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
